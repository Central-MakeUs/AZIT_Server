package com.youthexpedition.azit.modules.member.application.port.out.query;

public record MemberProfileDto(
        Long memberId,
        String nickname,
        String profileImageUrl
) {
    public static MemberProfileDto of(Long memberId, String nickname, String profileImageUrl) {
        return new MemberProfileDto(memberId, nickname, profileImageUrl);
    }
}