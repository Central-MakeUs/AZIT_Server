package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrewRepository extends JpaRepository<CrewEntity, Long> {
    Optional<CrewEntity> findByInvitationCode(String invitationCode);
}
