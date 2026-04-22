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
import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.image.application.port.out.ImageStoragePort;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageErrorCode;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageUploadType;
import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateMemberProfileCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.application.service.mapper.MemberResponseMapper;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
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
    private final ImageStoragePort imageStoragePort;
    private final ImageUrlFormatUtil imageUrlFormatUtil;

    private static final String BLACKLIST_REASON_WITHDRAWN = "withdrawn";
    private static final String DEFAULT_S3_PREFIX = "default/";
    private static final String DEFAULT_SLASH = "/";

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

        // 이미 탈퇴한 회원이 아닌 경우에만 상태 변경
        if (member.isWithdrawn()) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
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

        // 이미 탈퇴한 회원이 아닌 경우에만 상태 변경
        if (member.isWithdrawn()) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
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

        // 멤버 상태 확인
        if (!member.canUpdateStatusAfterConfirm()) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }

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
            log.info("[MEMBER] memberId: {}, crewIds : {} 탈퇴하여 인원수가 차감됩니다.", memberId, joinedCrewIds);

            List<Crew> joinedCrews = loadCrewPort.findAllByIds(joinedCrewIds);
            joinedCrews.forEach(Crew::decreaseMemberCount);
            saveCrewPort.saveAll(joinedCrews);
        }

        // 가입한 모든 크루 상태를 EXITED로 변경 및 저장
        LocalDateTime now = LocalDateTime.now();
        crewMembers.forEach(cm -> cm.exit(now));
        saveCrewMemberPort.saveAll(crewMembers);
    }

    @Override
    public void updateMemberProfile(Long memberId, UpdateMemberProfileCommand command) {
        Member member = getMember(memberId);

        // 닉네임 업데이트
        member.updateNickname(command.nickname());

        // 이미지 업데이트
        String incomingS3Key = imageUrlFormatUtil.extractS3Key(command.imageUrl());
        String currentS3Key = imageUrlFormatUtil.extractS3Key(member.getProfileImageUrl());

        // 이미지 변경 여부 판단
        // - S3 이미지: S3 키 기준으로 비교 (CloudFront URL과 상대경로를 동일하게 처리)
        // - 외부 URL(카카오 프로필 등, S3 키 = null): URL 문자열 직접 비교
        boolean imageChanged = !Objects.equals(incomingS3Key, currentS3Key) // s3 이미지
                || (incomingS3Key == null && !command.imageUrl().equals(member.getProfileImageUrl())); // 외부 이미지

        if (imageChanged) {
            if (incomingS3Key == null) {
                // 외부 URL로의 변경은 지원하지 않음
                throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
            }
            if (incomingS3Key.startsWith(ImageUploadType.TEMP_PREFIX)) {
                // 새 커스텀 이미지: temp 존재 여부 및 소유권 검증 → 이동
                if (!imageStoragePort.exists(incomingS3Key)) {
                    throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
                }
                validateImageOwnership(incomingS3Key, memberId);
                deleteOldCustomImage(currentS3Key);

                String finalS3Key = incomingS3Key.substring(ImageUploadType.TEMP_PREFIX.length());
                imageStoragePort.move(incomingS3Key, finalS3Key);
                member.updateProfileImageUrl(DEFAULT_SLASH + finalS3Key);
            } else if (incomingS3Key.startsWith(DEFAULT_S3_PREFIX)) {
                // 기본 이미지 선택 (프론트에서 랜덤 선택 후 URL 전달)
                deleteOldCustomImage(currentS3Key);
                member.updateProfileImageUrl(DEFAULT_SLASH + incomingS3Key);
            } else {
                throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
            }
        }

        saveMemberPort.save(member);
    }

    private void deleteOldCustomImage(String oldS3Key) {
        if (oldS3Key != null && !oldS3Key.startsWith(DEFAULT_S3_PREFIX)) {
            imageStoragePort.delete(oldS3Key);
        }
    }

    // 본인이 리더인 크루가 있으면 앱 탈퇴 불가
    private void validateWithdrawal(Long memberId) {
        // 사용자가 JOINED 상태이면서 리더인 크루 조회
        List<CrewMember> crewMembersAsLeader = loadCrewMemberPort.findAllByMemberId(memberId).stream()
                .filter(cm -> cm.getStatus() == CrewMemberStatus.JOINED)
                .filter(cm -> cm.getRole() == CrewMemberRole.LEADER)
                .toList();

        if (crewMembersAsLeader.isEmpty()) return;

        List<Long> crewIds = crewMembersAsLeader.stream()
                .map(CrewMember::getCrewId)
                .toList();

        log.warn("[MEMBER] memberId: {}, 리더로서 가입되어 있는 크루(crewIds: {})가 있어 앱 탈퇴가 불가능합니다.", memberId, crewIds);
        throw new BusinessException(CrewErrorCode.CANNOT_SERVICE_WITHDRAW_AS_LEADER);
    }

    private void validateImageOwnership(String tempS3Key, Long memberId) {
        Long imageOwnerMemberId = ImageUploadType.extractEntityIdFromTempKey(tempS3Key);
        if (imageOwnerMemberId == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
        }
        if (!imageOwnerMemberId.equals(memberId)) {
            throw new BusinessException(ImageErrorCode.IMAGE_OWNERSHIP_MISMATCH);
        }
    }
}
