package com.youthexpedition.azit.modules.crew.application.port.in.dto;

public record CreateCrewResponse(
        String invitationCode
) {
    public static CreateCrewResponse from(String invitationCode) {
        return new CreateCrewResponse(invitationCode);
    }
}