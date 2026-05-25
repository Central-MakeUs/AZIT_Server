package com.youthexpedition.azit.modules.crew.adapter.out.persistence;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity.CrewMemberEntity;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.mapper.CrewMemberMapper;
import com.youthexpedition.azit.modules.crew.adapter.out.persistence.repository.CrewMemberRepository;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.query.CrewMemberInfoDto;
import com.youthexpedition.azit.modules.crew.application.port.out.query.JoinRequestDto;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CrewMemberPersistenceAdapter implements LoadCrewMemberPort, SaveCrewMemberPort {
    private final CrewMemberRepository crewMemberRepository;
    private final CrewMemberMapper crewMemberMapper;

    private static final List<CrewMemberStatus> ACTIVE_STATUSES = List.of(CrewMemberStatus.JOINED, CrewMemberStatus.REQUESTED, CrewMemberStatus.REJECTED);
    private static final List<CrewMemberStatus> PARTICIPATING_STATUSES = List.of(CrewMemberStatus.JOINED, CrewMemberStatus.REQUESTED);

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
    public Optional<CrewMemberStatus> findStatusByCrewIdAndMemberId(Long crewId, Long memberId) {
        return crewMemberRepository.findByCrewIdAndMemberId(crewId, memberId)
                .map(CrewMemberEntity::getStatus);
    }

    @Override
    public List<JoinRequestDto> findJoinRequestsByCrewId(Long crewId) {
        return crewMemberRepository.findJoinRequestsByCrewId(crewId);
    }

    @Override
    public Optional<CrewMember> findRecentJoinedCrewMember(Long memberId) {
        return crewMemberRepository.findFirstByMemberIdAndStatusInOrderByIdDesc(memberId, ACTIVE_STATUSES)
                .map(crewMemberMapper::toDomain);
    }

    @Override
    public SliceResponse<CrewMemberInfoDto> findAllJoinedMembersByCrewId(Long crewId, CursorPageQuery query) {
        return crewMemberRepository.findAllJoinedMembersByCrewId(crewId, query);
    }

    @Override
    public long countJoinedCrewsByMemberId(Long memberId) {
        return crewMemberRepository.countByMemberIdAndStatus(memberId, CrewMemberStatus.JOINED);
    }

    @Override
    public long countActiveCrewsByMemberId(Long memberId) {
        return crewMemberRepository.countByMemberIdAndStatusIn(memberId, List.of(CrewMemberStatus.JOINED, CrewMemberStatus.REQUESTED));
    }

    @Override
    public List<CrewMember> findAllByMemberId(Long memberId) {
        return crewMemberRepository.findAllByMemberId(memberId).stream()
                .map(crewMemberMapper::toDomain)
                .toList();
    }

    @Override
    public List<CrewMember> findAllActiveByMemberId(Long memberId) {
        return crewMemberRepository.findAllByMemberIdAndStatusIn(memberId, PARTICIPATING_STATUSES).stream()
                .map(crewMemberMapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<CrewMember> crews) {
        List<CrewMemberEntity> entities = crews.stream()
                .map(crewMemberMapper::toEntity)
                .toList();
        crewMemberRepository.saveAll(entities);
    }

    @Override
    public Map<Long, CrewMember> findAllByCrewIdAndMemberIds(Long crewId, List<Long> memberIds) {
        return crewMemberRepository.findByCrew_IdAndMemberIdIn(crewId, memberIds).stream()
                .map(crewMemberMapper::toDomain)
                .collect(Collectors.toMap(CrewMember::getMemberId, cm -> cm));
    }

    @Override
    public List<CrewMember> findAllJoinedByCrewId(Long crewId) {
        return crewMemberRepository.findByCrew_IdAndStatus(crewId, CrewMemberStatus.JOINED).stream()
                .map(crewMemberMapper::toDomain)
                .toList();
    }
}
