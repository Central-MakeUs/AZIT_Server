package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.ProcessJoinCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewInvitationResponse;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewJoinStatusResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewService implements CrewUseCase {
    private final SaveCrewPort saveCrewPort;
    private final LoadCrewPort loadCrewPort;
    private final SaveCrewMemberPort saveCrewMemberPort;
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;

    @Override
    public CreateCrewResponse createCrew(CreateCrewCommand command) {
        // 초대 코드 생성
        String invitationCode = generateUniqueInvitationCode();

        // 크루 생성
        Crew crew = Crew.create(command.name(), command.category(), command.region(), invitationCode);
        Crew savedCrew = saveCrewPort.save(crew);

        // 리더 등록
        CrewMember leader = CrewMember.createAsLeader(savedCrew.getId(), command.leaderId());
        saveCrewMemberPort.save(leader);

        // 온보딩 완료했으므로 ACTIVE로 멤버 상태 변경
        Member member = loadMemberPort.findById(command.leaderId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.completeOnboarding();

        saveMemberPort.save(member);

        return CreateCrewResponse.from(savedCrew.getInvitationCode());
    }

    // 초대 코드 중복 방어
    private String generateUniqueInvitationCode() {
        final int MAX_RETRIES = 10; // 최대 재시도 횟수 제한

        for (int i = 0; i < MAX_RETRIES; i++) {
            String invitationCode = Crew.generateRandomCode();

            // DB 조회하여 중복 여부 확인
            if (!loadCrewPort.existsByInvitationCode(invitationCode)) {
                return invitationCode;
            }
        }
        throw new BusinessException(CrewErrorCode.INVITATION_CODE_GENERATION_FAILED);
    }

    @Override
    public void joinCrew(JoinCrewCommand command) {
        Crew crew = loadCrewPort.findByInvitationCode(command.invitationCode())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        // 이미 가입된 멤버인지 확인
        loadCrewMemberPort.findByCrewIdAndMemberId(crew.getId(), command.memberId())
                .ifPresentOrElse(
                        existingMember -> {
                            // 이미 대기 중이거나 가입된 상태일 경우 재가입 요청 불가
                            if (existingMember.getStatus() == CrewMemberStatus.REQUESTED || existingMember.getStatus() == CrewMemberStatus.JOINED) {
                                throw new BusinessException(CrewErrorCode.ALREADY_JOINED_CREW);
                            }

                            // 탈퇴나 거절 상태일 경우 재신청
                            existingMember.reJoin();
                            saveCrewMemberPort.save(existingMember);
                        },
                        () -> {
                            // 첫 신청일 경우 새로 생성
                            CrewMember newMember = CrewMember.createAsMember(crew.getId(), command.memberId());
                            saveCrewMemberPort.save(newMember);
                        }
                );
    }

    @Override
    @Transactional(readOnly = true)
    public CrewInvitationResponse getCrewInfoByInvitationCode(String invitationCode) {
        Crew crew = loadCrewPort.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        // 멤버 수 조회
        long memberCount = loadCrewMemberPort.countJoinedMembersByCrewId(crew.getId());

        return CrewInvitationResponse.of(crew, memberCount);
    }

    @Override
    @Transactional(readOnly = true)
    public CrewJoinStatusResponse getCrewJoinStatus(Long crewId, Long memberId) {
        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        // 가입 내역 조회
        CrewMemberStatus status = loadCrewMemberPort.findStatusByCrewIdAndMemberId(crewId, memberId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.JOIN_REQUEST_NOT_FOUND));

        return CrewJoinStatusResponse.of(crew.getId(), crew.getName(), status);
    }

    @Override
    @Transactional
    public void approveJoinRequest(ProcessJoinCommand command) {
        // 승인 요청자가 해당 크루의 리더인지 확인
        validateLeader(command.crewId(), command.leaderId());

        // 가입 대기 중인 대상자 조회
        CrewMember targetCrewMember = loadCrewMemberPort.findByCrewIdAndMemberId(command.crewId(), command.targetMemberId())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.JOIN_REQUEST_NOT_FOUND));

        // 가입 승인
        targetCrewMember.approve();
        saveCrewMemberPort.save(targetCrewMember);

        // 해당 유저의 회원 상태를 ACTIVE로 전환 (온보딩 완료 처리)
        Member member = loadMemberPort.findById(command.targetMemberId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.completeOnboarding();
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void rejectJoinRequest(ProcessJoinCommand command) {
        // 승인 요청자가 해당 크루의 리더인지 확인
        validateLeader(command.crewId(), command.leaderId());

        CrewMember targetCrewMember = loadCrewMemberPort.findByCrewIdAndMemberId(command.crewId(), command.targetMemberId())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.JOIN_REQUEST_NOT_FOUND));

        targetCrewMember.reject();
        saveCrewMemberPort.save(targetCrewMember);
    }

    private void validateLeader(Long crewId, Long leaderId) {
        CrewMember requester = loadCrewMemberPort.findByCrewIdAndMemberId(crewId, leaderId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.NOT_JOINED_CREW));

        if (requester.getRole() != CrewMemberRole.LEADER) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }
    }
}
