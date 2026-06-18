package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CrewRepository extends JpaRepository<CrewEntity, Long> {
    Optional<CrewEntity> findByInvitationCode(String invitationCode);
    boolean existsByInvitationCode(String invitationCode);

    @Modifying
    @Query("UPDATE CrewEntity c SET c.memberCount = c.memberCount + 1 WHERE c.id = :crewId")
    void incrementMemberCount(@Param("crewId") Long crewId);

    @Modifying
    @Query("UPDATE CrewEntity c SET c.memberCount = c.memberCount - 1 WHERE c.id = :crewId")
    void decrementMemberCount(@Param("crewId") Long crewId);
}
