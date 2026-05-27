package com.youthexpedition.azit.modules.test.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialRevokeCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.crew.application.port.out.*;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SavePointHistoryPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.store.application.port.out.SaveCartItemPort;
import com.youthexpedition.azit.modules.test.application.port.in.TestMemberUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestMemberService implements TestMemberUseCase {

    private static final String BLACKLIST_REASON_TEST_WITHDRAWN = "test-force-withdrawn";

    private final LoadMemberPort loadMemberPort;
    private final SocialAuthPort socialAuthPort;
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadCrewPort loadCrewPort;
    private final SaveCrewPort saveCrewPort;
    private final SaveCrewScheduleMemberPort saveCrewScheduleMemberPort;
    private final SaveCrewMemberPort saveCrewMemberPort;
    private final SavePointHistoryPort savePointHistoryPort;
    private final SaveCartItemPort saveCartItemPort;
    private final SaveDeliveryAddressPort saveDeliveryAddressPort;
    private final SaveMemberPort saveMemberPort;
    private final TokenPort tokenPort;

    @Override
    @Transactional
    public void forceWithdraw(Long memberId, String accessToken) {
        log.warn("[TEST] memberId: {} 강제 탈퇴 처리를 시작합니다.", memberId);

        // 탈퇴 가능한지 확인
        validateWithdrawal(memberId);

        // JOINED 상태인 크루의 인원수 차감
        decreaseJoinedCrewMemberCounts(memberId);

        // crew_schedule_member 완전 삭제
        saveCrewScheduleMemberPort.deleteByMemberId(memberId);

        // crew_member 완전 삭제
        saveCrewMemberPort.deleteByMemberId(memberId);

        // point_history 완전 삭제
        savePointHistoryPort.deleteByMemberId(memberId);

        // cart_item 완전 삭제
        saveCartItemPort.deleteByMemberId(memberId);

        // delivery_address 완전 삭제
        saveDeliveryAddressPort.deleteByMemberId(memberId);

        // 소셜 연동 해제
        Member member = getMember(memberId);
        socialAuthPort.revoke(SocialRevokeCommand.from(member));

        // member 완전 삭제
        saveMemberPort.deleteById(memberId);

        // 토큰 처리
        tokenPort.deleteByMemberId(memberId);
        tokenPort.addToBlacklist(accessToken, BLACKLIST_REASON_TEST_WITHDRAWN);

        log.warn("[TEST] memberId: {} 강제 탈퇴 처리가 완료되었습니다.", memberId);
    }

    private void decreaseJoinedCrewMemberCounts(Long memberId) {
        List<CrewMember> crewMembers = loadCrewMemberPort.findAllByMemberId(memberId);
        if (crewMembers.isEmpty()) return;

        List<Long> joinedCrewIds = crewMembers.stream()
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .map(CrewMember::getCrewId)
                .toList();

        if (!joinedCrewIds.isEmpty()) {
            log.info("[TEST] memberId: {}, crewIds: {} 탈퇴로 인해 인원수가 차감됩니다.", memberId, joinedCrewIds);
            List<Crew> joinedCrews = loadCrewPort.findAllByIds(joinedCrewIds);
            joinedCrews.forEach(Crew::decreaseMemberCount);
            saveCrewPort.saveAll(joinedCrews);
        }
    }

    private Member getMember(Long memberId) {
        return loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    // 본인이 리더인 크루가 있으면 앱 탈퇴 불가
    private void validateWithdrawal(Long memberId) {
        // 사용자가 JOINED 상태이면서 리더인 크루 조회
        List<CrewMember> crewMembersAsLeader = loadCrewMemberPort.findAllActiveByMemberId(memberId).stream()
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .filter(cm -> cm.getRole() == CrewMemberRole.LEADER)
                .toList();

        if (crewMembersAsLeader.isEmpty()) return;

        List<Long> crewIds = crewMembersAsLeader.stream()
                .map(CrewMember::getCrewId)
                .toList();

        log.warn("[TEST] memberId: {}, 리더로서 가입되어 있는 크루(crewIds: {})가 있어 앱 탈퇴가 불가능합니다.", memberId, crewIds);
        throw new BusinessException(CrewErrorCode.CANNOT_SERVICE_WITHDRAW_AS_LEADER);
    }
}
