package com.youthexpedition.azit.modules.auth.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleUserInfoResponse(
        Authorization authorization,
        User user
) {
    public record Authorization(
            String code,
            @JsonProperty("id_token") String idToken,
            String state
    ) {}

    public record User(
            String email,
            Name name
    ) {}

    public record Name(
            String firstName,
            String lastName
    ) {}
}
