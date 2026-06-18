package com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CrewRepository extends JpaRepository<CrewEntity, Long> {
    Optional<CrewEntity> findByInvitationCode(String invitationCode);
    boolean existsByInvitationCode(String invitationCode);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CrewEntity c SET c.memberCount = c.memberCount + 1 WHERE c.id = :crewId")
    void incrementMemberCount(@Param("crewId") Long crewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE crew SET member_count = member_count - 1, " +
            "status = CASE WHEN member_count - 1 = 0 THEN 'INACTIVE' ELSE status END " +
            "WHERE id = :crewId AND member_count > 0", nativeQuery = true)
    void decrementMemberCount(@Param("crewId") Long crewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE crew SET member_count = member_count - 1, " +
            "status = CASE WHEN member_count - 1 = 0 THEN 'INACTIVE' ELSE status END " +
            "WHERE id IN :crewIds AND member_count > 0", nativeQuery = true)
    void decrementMemberCountBatch(@Param("crewIds") List<Long> crewIds);
}
