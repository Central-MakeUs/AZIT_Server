package com.youthexpedition.azit.modules.crew.application.port.in;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.command.*;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleMonthlyListResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.ParticipantResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.query.CrewScheduleMonthlyQuery;
import com.youthexpedition.azit.modules.crew.application.port.in.query.CrewScheduleQuery;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CheckInStatusResponse;

import java.util.List;

public interface CrewScheduleUseCase {
    void createSchedule(CreateScheduleCommand command);
    void updateSchedule(UpdateScheduleCommand command);
    void cancelSchedule(CancelScheduleCommand command);
    void participateSchedule(CrewScheduleCommand command);
    void cancelParticipation(CrewScheduleCommand command);
    CrewScheduleDetailResponse getScheduleDetail(CrewScheduleCommand command);
    SliceResponse<ParticipantResponse> getScheduleParticipants(CrewScheduleCommand command, CursorPageQuery query);
    List<CrewScheduleListResponse> getSchedules(CrewScheduleQuery query);
    List<CrewScheduleMonthlyListResponse> getMonthlySchedulesForCalendar(CrewScheduleMonthlyQuery query);
    List<CrewScheduleListResponse> getMySchedules(Long memberId);
    CheckInStatusResponse getCheckInStatus(Long memberId);
    void checkInSchedule(CheckInCommand command);
}
