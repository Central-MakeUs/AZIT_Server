package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewScheduleMemberRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewScheduleMemberPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrewScheduleMemberPersistenceAdapter implements SaveCrewScheduleMemberPort {
    private final CrewScheduleMemberRepository crewScheduleMemberRepository;

    @Override
    public void deleteByMemberId(Long memberId) {
        crewScheduleMemberRepository.deleteByMemberId(memberId);
    }
}
