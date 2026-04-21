package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewScheduleUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewUseCase;
import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.JoinCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.ProcessJoinCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateCrewImageCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.command.UpdateCrewInfoCommand;
import com.youthexpedition.azit.modules.image.application.port.out.ImageStoragePort;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageErrorCode;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageUploadType;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.*;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewInfoResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.application.port.out.query.CrewMemberInfoDto;
import com.youthexpedition.azit.modules.crew.application.service.mapper.CrewMemberResponseMapper;
import com.youthexpedition.azit.modules.crew.application.service.mapper.CrewResponseMapper;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.crew.domain.model.provider.CrewImageProvider;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CrewService implements CrewUseCase {
    private final SaveCrewPort saveCrewPort;
    private final LoadCrewPort loadCrewPort;
    private final SaveCrewMemberPort saveCrewMemberPort;
    private final LoadCrewMemberPort loadCrewMemberPort;
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final CrewScheduleUseCase crewScheduleUseCase;
    private final CrewMemberResponseMapper crewMemberResponseMapper;
    private final CrewResponseMapper crewResponseMapper;
    private final CrewImageProvider crewImageProvider;
    private final ImageStoragePort imageStoragePort;
    private final ImageUrlFormatUtil imageUrlFormatUtil;

    private static final String DEFAULT_S3_PREFIX = "default/";

    @Override
    @Retryable(
            retryFor = {DataIntegrityViolationException.class}, // db 에러 시 재시도
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public CreateCrewResponse createCrew(CreateCrewCommand command) {
        // 초대 코드 생성
        String invitationCode = generateUniqueInvitationCode();

        // 크루 기본 이미지
        String defaultImageUrl = crewImageProvider.getCrewDefaultImage();

        // 크루 생성
        Crew crew = Crew.create(command.name(), command.category(), command.region(), defaultImageUrl, invitationCode);
        Crew savedCrew = saveCrewPort.save(crew);

        // 리더 등록
        CrewMember leader = CrewMember.createAsLeader(savedCrew.getId(), command.leaderId());
        saveCrewMemberPort.save(leader);

        // 온보딩 완료했으므로 ACTIVE로 멤버 상태 변경
        Member member = loadMemberPort.findById(command.leaderId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 멤버 상태 확인
        if (!member.isJoinable()) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
        member.completeOnboarding();

        saveMemberPort.save(member);

        return crewResponseMapper.toCreateResponse(savedCrew);
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

        log.error("[CREW] 크루 초대코드 생성 최대 재시도 횟수({})를 초과했습니다.", MAX_RETRIES);
        throw new BusinessException(CrewErrorCode.INVITATION_CODE_GENERATION_FAILED);
    }

    @Override
    public void joinCrew(JoinCrewCommand command) {
        Crew crew = loadCrewPort.findByInvitationCode(command.invitationCode())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        // 이미 가입된 멤버인지 확인
        loadCrewMemberPort.findByCrewIdAndMemberId(crew.getId(), command.memberId())
                .ifPresentOrElse(
                        existingMember -> {
                            // 이미 대기 중이거나 가입된 상태일 경우 재가입 요청 불가
                            if (existingMember.getStatus() == CrewMemberStatus.REQUESTED || existingMember.getStatus() == CrewMemberStatus.JOINED) {
                                throw new BusinessException(CrewErrorCode.ALREADY_JOINED_CREW);
                            }

                            // 방출 후 24시간 이내 재가입 차단
                            if (existingMember.getStatus() == CrewMemberStatus.EXPELLED && existingMember.isRejoinCooldownActive(now)) {
                                throw new BusinessException(CrewErrorCode.EXPELLED_REJOINING_COOLDOWN);
                            }

                            // 자진 탈퇴 후 24시간 이내 재가입 차단
                            if (existingMember.getStatus() == CrewMemberStatus.EXITED && existingMember.isExitCooldownActive(now)) {
                                throw new BusinessException(CrewErrorCode.EXIT_REJOINING_COOLDOWN);
                            }

                            // 탈퇴, 방출(쿨다운 지남), 거절 상태일 경우 재신청
                            existingMember.reJoin();
                            saveCrewMemberPort.save(existingMember);
                        },
                        () -> {
                            // 첫 신청일 경우 새로 생성
                            CrewMember newMember = CrewMember.createAsMember(crew.getId(), command.memberId());
                            saveCrewMemberPort.save(newMember);
                        }
                );

        Member member = loadMemberPort.findById(command.memberId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 멤버 상태 확인
        if (!member.isJoinable()) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
        // WAITING_FOR_APPROVE 으로 상태 변경
        member.applyForJoin();
        saveMemberPort.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public CrewInvitationResponse getCrewInfoByInvitationCode(String invitationCode) {
        Crew crew = loadCrewPort.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        return crewResponseMapper.toInvitationResponse(crew);
    }

    @Override
    @Transactional(readOnly = true)
    public CrewJoinStatusResponse getCrewJoinStatus(Long crewId, Long memberId) {
        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        // 가입 내역 조회
        CrewMemberStatus status = loadCrewMemberPort.findStatusByCrewIdAndMemberId(crewId, memberId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.JOIN_REQUEST_NOT_FOUND));

        return crewResponseMapper.toJoinStatusResponse(crew, status);
    }

    @Override
    @Transactional
    public void approveJoinRequest(ProcessJoinCommand command) {
        // 승인 요청자가 해당 크루의 리더인지 확인
        validateLeader(command.crewId(), command.leaderId());

        // 가입 대기 중인 대상자 조회
        CrewMember targetCrewMember = loadCrewMemberPort.findByCrewIdAndMemberId(command.crewId(), command.targetMemberId())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.JOIN_REQUEST_NOT_FOUND));

        // 가입 신청 상태인지 확인
        if (!targetCrewMember.isJoinRequested()) {
            throw new BusinessException(CrewErrorCode.ALREADY_PROCESSED_JOIN_REQUEST);
        }

        // 가입 승인
        targetCrewMember.approve();
        saveCrewMemberPort.save(targetCrewMember);

        // 크루 인원 수 증가
        // TODO: 원자성 체크 필요
        Crew crew = loadCrewPort.findById(command.crewId())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        log.info("[CREW] crewId: {} 에서 memberId: {} 가 가입되어 크루 인원 수가 증가합니다.", crew.getId(), command.targetMemberId());
        crew.increaseMemberCount(); // 인원 수 +1
        saveCrewPort.save(crew);

        // 해당 유저의 회원 상태를 APPROVED_PENDING_CONFIRM 로 변경 (온보딩 완료 처리)
        Member member = loadMemberPort.findById(command.targetMemberId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 멤버 상태 확인
        if (member.getStatus() != MemberStatus.WAITING_FOR_APPROVE) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
        member.approveJoin();
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void rejectJoinRequest(ProcessJoinCommand command) {
        // 승인 요청자가 해당 크루의 리더인지 확인
        validateLeader(command.crewId(), command.leaderId());

        CrewMember targetCrewMember = loadCrewMemberPort.findByCrewIdAndMemberId(command.crewId(), command.targetMemberId())
                .orElseThrow(() -> new BusinessException(CrewErrorCode.JOIN_REQUEST_NOT_FOUND));

        // 가입 신청 상태인지 확인
        if (!targetCrewMember.isJoinRequested()) {
            throw new BusinessException(CrewErrorCode.ALREADY_PROCESSED_JOIN_REQUEST);
        }

        targetCrewMember.reject();
        saveCrewMemberPort.save(targetCrewMember);

        Member member = loadMemberPort.findById(command.targetMemberId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 해당 유저의 회원 상태를 REJECTED_PENDING_CONFIRM 으로 변경
        if (member.getStatus() != MemberStatus.WAITING_FOR_APPROVE) {
            throw new BusinessException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
        member.rejectJoin();
        saveMemberPort.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoinRequestMemberResponse> getJoinRequests(Long crewId, Long memberId) {
        validateLeader(crewId, memberId);

        return loadCrewMemberPort.findJoinRequestsByCrewId(crewId).stream()
                .map(crewMemberResponseMapper::toJoinRequestResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CrewMemberListResponse getCrewMembers(Long crewId, Long memberId, CursorPageQuery query) {
        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        validateMember(crewId, memberId);

        SliceResponse<CrewMemberInfoDto> crewMemberSlice = loadCrewMemberPort.findAllJoinedMembersByCrewId(crewId, query);

        return crewMemberResponseMapper.toCrewMemberListResponse(crew.getMemberCount(), crewMemberSlice);
    }

    @Override
    @Transactional
    public void expelCrewMember(Long crewId, Long leaderId, Long targetMemberId) {
        validateLeader(crewId, leaderId);

        // 리더 본인은 방출 불가
        if (leaderId.equals(targetMemberId)) {
            throw new BusinessException(CrewErrorCode.CANNOT_KICK_SELF);
        }

        // 방출 대상이 현재 해당 크루의 정회원(JOINED)인지 확인
        CrewMember targetMember = validateMember(crewId, targetMemberId);

        // 멤버 스케줄 삭제
        crewScheduleUseCase.cleanupForExpelledMemberSchedules(crewId, targetMemberId);

        // 멤버 상태 EXPELLED 변경
        targetMember.expel(LocalDateTime.now());
        saveCrewMemberPort.save(targetMember);

        // 크루 인원 수 1명 감소
        // TODO: 동시성 이슈 고려 필요
        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        log.info("[CREW] crewId: {} 에서 memberId: {} 가 방출되어 크루 인원 수가 감소합니다.", crewId, targetMemberId);
        crew.decreaseMemberCount();
        saveCrewPort.save(crew);

        // 가입된 잔여 크루 확인 멤버 상태 변경
        long joinedCrewCount = loadCrewMemberPort.countJoinedCrewsByMemberId(targetMemberId);

        Member member = loadMemberPort.findById(targetMemberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (joinedCrewCount == 0) {
            // 탈퇴한 회원인지 확인
            if (member.isWithdrawn()) {
                throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
            }

            log.info("[CREW] memberId: {}, 가입된 크루가 없으므로 온보딩 상태로 변경합니다.", member.getId());
            member.resetToOnboarding();
        }

        member.expel();
        saveMemberPort.save(member);
    }

    @Override
    @Transactional
    public void exitCrew(Long crewId, Long memberId) {
        LocalDateTime now = LocalDateTime.now();

        CrewMember crewMember = validateMember(crewId, memberId);

        // 리더는 크루 나가기 불가
        if (crewMember.getRole() == CrewMemberRole.LEADER) {
            throw new BusinessException(CrewErrorCode.CANNOT_WITHDRAW_AS_LEADER);
        }

        // 미래 일정 정리 (생성한 일정 취소, 참여 명단 제거)
        crewScheduleUseCase.cleanupForExpelledMemberSchedules(crewId, memberId);

        // 크루 멤버 상태 EXITED 변경
        crewMember.exit(now);
        saveCrewMemberPort.save(crewMember);

        // 크루 인원 수 1명 감소
        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        log.info("[CREW] crewId: {} 에서 memberId: {} 가 자진 탈퇴하여 크루 인원 수가 감소합니다.", crewId, memberId);
        crew.decreaseMemberCount();
        saveCrewPort.save(crew);

        // 가입된 잔여 크루 확인 후 멤버 상태 변경
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        long joinedCrewCount = loadCrewMemberPort.countJoinedCrewsByMemberId(memberId);

        if (joinedCrewCount == 0) {
            log.info("[CREW] memberId: {}, 가입된 크루가 없으므로 온보딩 상태로 변경합니다.", memberId);
            member.resetToOnboarding();
            saveMemberPort.save(member);
        }
    }

    @Override
    @Retryable(
            retryFor = {DataIntegrityViolationException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public InvitationCodeResponse regenerateInvitationCode(Long crewId, Long memberId) {
        validateLeader(crewId, memberId);

        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        String newCode = generateUniqueInvitationCode();
        crew.updateInvitationCode(newCode);
        saveCrewPort.save(crew);

        return InvitationCodeResponse.of(newCode);
    }

    @Override
    public void updateCrewImage(Long crewId, Long memberId, UpdateCrewImageCommand command) {
        // 리더 권한 검증
        validateLeader(crewId, memberId);

        // temp 경로 파일 존재 여부 검증
        String tempS3Key = imageUrlFormatUtil.extractS3Key(command.imageUrl());
        if (tempS3Key == null || !tempS3Key.startsWith(ImageUploadType.TEMP_PREFIX) || !imageStoragePort.exists(tempS3Key)) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
        }

        // temp 경로의 crewId가 요청 crewId와 일치하는지 검증
        Long pathCrewId = ImageUploadType.extractEntityIdFromTempKey(tempS3Key);
        if (!crewId.equals(pathCrewId)) {
            throw new BusinessException(ImageErrorCode.IMAGE_OWNERSHIP_MISMATCH);
        }

        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        // 기존 이미지가 커스텀 업로드 이미지인 경우에만 S3에서 삭제
        String oldS3Key = imageUrlFormatUtil.extractS3Key(crew.getImageUrl());
        if (oldS3Key != null && !oldS3Key.startsWith(DEFAULT_S3_PREFIX)) {
            imageStoragePort.delete(oldS3Key);
        }

        // temp → 실제 경로로 이동
        String finalS3Key = tempS3Key.substring(ImageUploadType.TEMP_PREFIX.length());
        imageStoragePort.move(tempS3Key, finalS3Key);

        // 상대 경로 저장
        crew.updateImageUrl("/" + finalS3Key);
        saveCrewPort.save(crew);
    }

    @Override
    @Transactional(readOnly = true)
    public CrewInfoResponse getCrewInfo(Long crewId, Long memberId) {
        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        validateMember(crewId, memberId);

        return crewResponseMapper.toCrewInfoResponse(crew);
    }

    @Override
    public void updateCrewInfo(Long crewId, Long memberId, UpdateCrewInfoCommand command) {
        Crew crew = loadCrewPort.findById(crewId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.CREW_NOT_FOUND));

        validateLeader(crewId, memberId);

        crew.updateInfo(command.name(), command.description());
        saveCrewPort.save(crew);
    }

    // 리더 여부 체크
    private void validateLeader(Long crewId, Long memberId) {
        CrewMember crewMember = validateMember(crewId, memberId);

        if (crewMember.getRole() != CrewMemberRole.LEADER) {
            log.warn("memberId: {}, crewId: {}, 해당 사용자가 리더 전용 로직을 호출했으나 권한 부족으로 거절되었습니다.", memberId, crewId);
            throw new BusinessException(CrewErrorCode.NOT_CREW_LEADER);
        }
    }

    // 가입한 크루인지 체크
    private CrewMember validateMember(Long crewId, Long memberId) {
        CrewMember crewMember = loadCrewMemberPort.findByCrewIdAndMemberId(crewId, memberId)
                .orElseThrow(() -> new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER));

        if (crewMember.getStatus() != CrewMemberStatus.JOINED) {
            throw new BusinessException(CrewErrorCode.NOT_A_CREW_MEMBER);
        }

        return crewMember;
    }
}
