package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.Member;

public interface SaveMemberPort {
    Member save(Member member);
}
