package com.youthexpedition.azit.modules.crew.application.port.in.command;

import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;

import java.time.LocalDateTime;
import java.util.List;

public record CreateScheduleCommand(
        Long crewId,
        Long creatorId,
        String title,
        RunType runType,
        LocalDateTime meetingAt,
        String placeName,
        String address,
        String meetingSpot,
        Double latitude,
        Double longitude,
        Double distance,
        Double pace,
        Integer maxParticipants,
        String description,
        List<String> supplies
) {
    public static CreateScheduleCommand of(
            Long crewId, Long creatorId, String title, RunType runType, LocalDateTime meetingAt,
            String placeName, String address, String meetingSpot, Double latitude, Double longitude,
            Double distance, Double pace, Integer maxParticipants, String description, List<String> supplies
    ) {
        return new CreateScheduleCommand(
                crewId, creatorId, title, runType, meetingAt,
                placeName, address, meetingSpot, latitude, longitude,
                distance, pace, maxParticipants, description, supplies
        );
    }
}
