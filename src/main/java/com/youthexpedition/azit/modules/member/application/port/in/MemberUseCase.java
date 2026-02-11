package com.youthexpedition.azit.modules.member.application.port.in;

import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyPageResponse;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;

public interface MemberUseCase {
    void agreeToTerms(Long memberId, AgreeToTermsCommand command);
    void withdraw(Long memberId, String accessToken);
    void withdrawBySocialInfo(String socialProviderId, SocialProvider socialProvider);
    void updateEmailSharingStatus(String socialProviderId, SocialProvider socialProvider, boolean isEnabled);
    void confirmMemberStatus(Long memberId);
    MyPageResponse getMyInfo(Long memberId);
}
