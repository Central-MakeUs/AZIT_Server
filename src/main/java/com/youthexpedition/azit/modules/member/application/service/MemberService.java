package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialRevokeCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.member.application.port.in.MemberUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements MemberUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final SaveCrewMemberPort saveCrewMemberPort;
    private final SocialAuthPort socialAuthPort;
    private final TokenPort tokenPort;

    private static final String BLACKLIST_REASON_WITHDRAWN = "withdrawn";

    @Override
    public void agreeToTerms(Long memberId, AgreeToTermsCommand command) {
        command.validateRequired(); // 필수 약관 동의 여부 검증

        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.completeTermsAgreement(command.marketingTermsAgreed(), command.notificationTermsAgreed()); // 멤버 상태 업데이트 (약관 동의)
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void withdraw(Long memberId, String accessToken) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 소셜 연동 해제
        socialAuthPort.revoke(SocialRevokeCommand.from(member));

        // 가입한 모든 크루에서 탈퇴 처리
        saveCrewMemberPort.updateAllStatusByMemberId(memberId, CrewMemberStatus.EXITED);

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

        saveCrewMemberPort.updateAllStatusByMemberId(member.getId(), CrewMemberStatus.EXITED);

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
}
