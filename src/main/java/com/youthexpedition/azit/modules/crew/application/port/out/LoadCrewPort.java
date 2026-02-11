package com.youthexpedition.azit.modules.crew.application.port.out;

import com.youthexpedition.azit.modules.crew.domain.model.Crew;

import java.util.Optional;

public interface LoadCrewPort {
    Optional<Crew> findById(Long id);
    Optional<Crew> findByInvitationCode(String invitationCode);
    boolean existsByInvitationCode(String invitationCode);
    boolean existsById(Long crewId);
}
