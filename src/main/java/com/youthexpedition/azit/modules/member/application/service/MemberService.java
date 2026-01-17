package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.application.port.in.command.SocialRevokeCommand;
import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import com.youthexpedition.azit.modules.auth.application.port.out.SocialAuthPort;
import com.youthexpedition.azit.modules.member.application.port.in.MemberUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements MemberUseCase {
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final SocialAuthPort socialAuthPort;
    private final TokenPort tokenPort;

    @Override
    public void agreeToTerms(Long memberId, AgreeToTermsCommand command) {
        command.validateRequired(); // 필수 약관 동의 여부 검증

        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.completeTermsAgreement(command.marketingTermsAgreed()); // 멤버 상태 업데이트 (약관 동의)
        saveMemberPort.save(member);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 소셜 연동 해제
        socialAuthPort.revoke(SocialRevokeCommand.from(member));

        member.withdraw();
        tokenPort.deleteByMemberId(memberId); // 리프레시 토큰 삭제
        saveMemberPort.save(member);
    }
}
