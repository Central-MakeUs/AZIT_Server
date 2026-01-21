package com.youthexpedition.azit.modules.crew.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.in.CrewUseCase;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateCrewCommand;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CreateCrewResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.SaveCrewPort;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
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
    private final SaveCrewMemberPort saveCrewMemberPort;
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;

    @Override
    public CreateCrewResponse createCrew(CreateCrewCommand command) {
        // 크루 생성
        Crew crew = Crew.create(command.name(), command.category(), command.region());
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
}
