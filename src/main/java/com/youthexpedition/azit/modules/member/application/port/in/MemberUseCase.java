package com.youthexpedition.azit.modules.member.application.port.in;

import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;

public interface MemberUseCase {
    void agreeToTerms(Long memberId, AgreeToTermsCommand command);
    void withdraw(Long memberId, String accessToken);
}
