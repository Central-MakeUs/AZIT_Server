package com.youthexpedition.azit.modules.member.application.port.in;

import com.youthexpedition.azit.modules.member.application.port.in.command.AgreeToTermsCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateMemberProfileCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.LinkedProviderResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyCrewResponse;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;

import java.util.List;

public interface MemberUseCase {
    void agreeToTerms(Long memberId, AgreeToTermsCommand command);
    void withdraw(Long memberId, String accessToken);
    void withdrawBySocialInfo(String socialProviderId, SocialProvider socialProvider);
    void updateEmailSharingStatus(String socialProviderId, SocialProvider socialProvider, boolean isEnabled);
    MyInfoResponse getMyInfo(Long memberId);
    List<MyCrewResponse> getMyCrews(Long memberId);
    void updateMemberProfile(Long memberId, UpdateMemberProfileCommand command);
    LinkedProviderResponse getLinkedProviders(Long memberId);
    void resetToPendingTerms(Long memberId);
}
