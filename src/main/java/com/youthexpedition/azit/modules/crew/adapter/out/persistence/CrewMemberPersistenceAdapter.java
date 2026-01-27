package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper.CrewMemberMapper;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewMemberRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.model.JoinRequestQueryResult;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CrewMemberPersistenceAdapter implements LoadCrewMemberPort, SaveCrewMemberPort {
    private final CrewMemberRepository crewMemberRepository;
    private final CrewMemberMapper crewMemberMapper;

    @Override
    public CrewMember save(CrewMember crewMember) {
        CrewMemberEntity entity = crewMemberMapper.toEntity(crewMember);
        CrewMemberEntity savedEntity = crewMemberRepository.save(entity);
        return crewMemberMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CrewMember> findByCrewIdAndMemberId(Long crewId, Long memberId) {
        return crewMemberRepository.findByCrewIdAndMemberId(crewId, memberId)
                .map(crewMemberMapper::toDomain);
    }

    @Override
    public long countJoinedMembersByCrewId(Long crewId) {
        return crewMemberRepository.countByCrewIdAndStatus(crewId, CrewMemberStatus.JOINED);
    }

    @Override
    public Optional<CrewMemberStatus> findStatusByCrewIdAndMemberId(Long crewId, Long memberId) {
        return crewMemberRepository.findByCrewIdAndMemberId(crewId, memberId)
                .map(CrewMemberEntity::getStatus);
    }

    @Override
    public void updateAllStatusByMemberId(Long memberId, CrewMemberStatus status) {
        crewMemberRepository.updateAllStatusByMemberId(memberId, status);
    }

    @Override
    public List<JoinRequestQueryResult> findJoinRequestsByCrewId(Long crewId) {
        return crewMemberRepository.findJoinRequestsByCrewId(crewId);
    }

    @Override
    public Optional<Long> findRecentCrewIdByMemberId(Long memberId) {
        // CrewMemberEntity에서 JOINED 상태인 것 중 ID가 가장 큰 것(최근 가입)을 조회
        return crewMemberRepository.findFirstByMemberIdAndStatusOrderByIdDesc(memberId, CrewMemberStatus.JOINED)
                .map(CrewMemberEntity::getCrewId);
    }
}
