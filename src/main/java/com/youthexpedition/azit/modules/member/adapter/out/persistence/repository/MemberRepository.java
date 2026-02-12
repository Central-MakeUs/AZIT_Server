package com.youthexpedition.azit.modules.member.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberEntity;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findBySocialProviderAndSocialProviderId(SocialProvider socialProvider, String socialProviderId);
}
