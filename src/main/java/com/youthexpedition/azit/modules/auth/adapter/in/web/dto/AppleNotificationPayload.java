package com.youthexpedition.azit.modules.auth.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleNotificationPayload(
        String iss,
        String aud,
        Long iat,
        String jti,
        @JsonProperty("events")
        String eventsJson // JSON 문자열 또는 객체로 올 수 있으므로 내부에서 파싱
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Event(
            String type,
            String sub,
            @JsonProperty("event_time")
            Long eventTime
    ) {}
}
