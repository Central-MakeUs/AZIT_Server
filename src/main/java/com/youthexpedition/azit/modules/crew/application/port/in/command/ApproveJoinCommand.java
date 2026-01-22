package com.youthexpedition.azit.modules.crew.application.port.in.command;

public record ApproveJoinCommand(
        Long crewId,
        Long targetMemberId, // 승인 대상자
        Long leaderId        // 승인하는 리더 (권한 확인용)
) {
    public static ApproveJoinCommand of(Long crewId, Long targetMemberId, Long leaderId) {
        return new ApproveJoinCommand(crewId, targetMemberId, leaderId);
    }
}