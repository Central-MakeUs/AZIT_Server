package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialRevokeCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.member.application.port.in.MemberUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.application.service.mapper.MemberResponseMapper;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements MemberUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final SaveCrewMemberPort saveCrewMemberPort;
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadCrewPort loadCrewPort;
    private final SaveCrewPort saveCrewPort;
    private final SocialAuthPort socialAuthPort;
    private final TokenPort tokenPort;
    private final MemberResponseMapper memberResponseMapper;

    private static final String BLACKLIST_REASON_WITHDRAWN = "withdrawn";

    @Override
    public void agreeToTerms(Long memberId, AgreeToTermsCommand command) {
        command.validateRequired(); // 필수 약관 동의 여부 검증

        Member member = getMember(memberId);
        member.completeTermsAgreement(command.marketingTermsAgreed(), command.notificationTermsAgreed()); // 멤버 상태 업데이트 (약관 동의)
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void withdraw(Long memberId, String accessToken) {
        Member member = getMember(memberId);

        // 탈퇴 가능한지 확인
        validateWithdrawal(memberId);

        // 소셜 연동 해제
        socialAuthPort.revoke(SocialRevokeCommand.from(member));

        // 가입한 크루 인원 수 차감 및 상태 변경
        processCrewWithdrawal(memberId);

        member.withdraw();
        tokenPort.deleteByMemberId(memberId); // 리프레시 토큰 삭제
        tokenPort.addToBlacklist(accessToken, BLACKLIST_REASON_WITHDRAWN); // 블랙리스트에 액세스 토큰 추가
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void withdrawBySocialInfo(String socialProviderId, SocialProvider socialProvider) {
        Member member = loadMemberPort.findBySocialInfo(socialProvider, socialProviderId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 탈퇴 가능한지 확인
        validateWithdrawal(member.getId());

        // 가입한 크루 인원 수 차감 및 상태 변경
        processCrewWithdrawal(member.getId());

        member.withdraw();
        tokenPort.deleteByMemberId(member.getId()); // 리프레시 토큰 삭제
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void updateEmailSharingStatus(String socialProviderId, SocialProvider socialProvider, boolean isEnabled) {
        Member member = loadMemberPort.findBySocialInfo(socialProvider, socialProviderId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updateEmailSharingStatus(isEnabled);
        saveMemberPort.save(member);
    }

    // 멤버 상태 변경
    @Override
    @Transactional
    public void confirmMemberStatus(Long memberId) {
        Member member = getMember(memberId);

        // 가입되어 있는 나머지 크루가 있는지 확인
        boolean hasJoinedCrews = loadCrewMemberPort.countJoinedCrewsByMemberId(memberId) > 0;
        member.confirmStatus(hasJoinedCrews);

        saveMemberPort.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long memberId) {
        Member member = getMember(memberId);

        if (!member.getStatus().isCrewInfoRequired()) {
            return memberResponseMapper.toMyPageResponse(member, null, null);
        }

        CrewMemberStatus targetStatus = switch (member.getStatus()) {
            case KICKED_PENDING_CONFIRM -> CrewMemberStatus.EXPELLED;   // 방출 확인 대기 시 EXPELLED 조회
            case REJECTED_PENDING_CONFIRM -> CrewMemberStatus.REJECTED; // 가입 거절 확인 대기 시 REJECTED 조회
            case WAITING_FOR_APPROVE -> CrewMemberStatus.REQUESTED;    // 가입 승인 대기 시 REQUESTED 조회
            case WITHDRAWN -> CrewMemberStatus.EXITED;
            default -> CrewMemberStatus.JOINED;                        // 그 외(ACTIVE 등) JOINED 조회
        };

        CrewMember crewMember = loadCrewMemberPort.findLatestByMemberIdAndStatus(memberId, targetStatus)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_MEMBER_NOT_FOUND));

        Crew crew = loadCrewPort.findById(crewMember.getCrewId())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        return memberResponseMapper.toMyPageResponse(member, crewMember, crew);
    }

    private Member getMember(Long memberId) {
        return loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void processCrewWithdrawal(Long memberId) {
        List<CrewMember> crewMembers = loadCrewMemberPort.findAllByMemberId(memberId);
        if (crewMembers.isEmpty()) return;

        // 가입 상태인 크루 ID 추출
        List<Long> joinedCrewIds = crewMembers.stream()
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .map(CrewMember::getCrewId)
                .toList();

        // 인원수 일괄 차감
        if (!joinedCrewIds.isEmpty()) {
            List<Crew> joinedCrews = loadCrewPort.findAllByIds(joinedCrewIds);
            joinedCrews.forEach(Crew::decreaseMemberCount);
            saveCrewPort.saveAll(joinedCrews);
        }

        // 가입한 모든 크루 상태를 EXITED로 변경 및 저장
        crewMembers.forEach(CrewMember::exit);
        saveCrewMemberPort.saveAll(crewMembers);
    }

    // 본인이 리더인 크루에 다른 멤버가 남아있는지 확인
    private void validateWithdrawal(Long memberId) {
        // 사용자가 JOINED 상태이면서 리더인 크루 조회
        List<CrewMember> crewMembersAsLeader = loadCrewMemberPort.findAllByMemberId(memberId).stream()
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .filter(cm -> cm.getRole() == CrewMemberRole.LEADER)
                .toList();

        if (crewMembersAsLeader.isEmpty()) return;

        // 리더로 속한 모든 크루 ID 가져오기
        List<Long> crewIds = crewMembersAsLeader.stream()
                .map(CrewMember::getCrewId)
                .toList();

        List<Crew> crews= loadCrewPort.findAllByIds(crewIds);

        // 크루 인원수가 1명보다 많으면 탈퇴 불가
        for (Crew crew : crews) {
            if (crew.getMemberCount() > 1) {
                throw new BusinessException(CrewErrorCode.CANNOT_WITHDRAW_AS_LEADER);
            }
        }
    }
}
