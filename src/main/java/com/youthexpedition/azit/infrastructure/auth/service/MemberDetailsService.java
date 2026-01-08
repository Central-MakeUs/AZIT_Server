package com.youthexpedition.azit.infrastructure.auth.service;

import com.youthexpedition.azit.infrastructure.auth.model.MemberDetails;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final LoadMemberPort loadMemberPort;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username(Member ID)으로 유저를 조회
        return loadMemberPort.findById(Long.parseLong(username))
                .map(MemberDetails::new)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}