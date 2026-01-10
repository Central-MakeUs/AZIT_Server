package com.youthexpedition.azit.infrastructure.common.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 파라미터에만 사용하도록 지정
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 정보 유지
// MemberDetails 객체 내의 getMember().getId() 값 가져오기
@AuthenticationPrincipal(expression = "member.id")
public @interface CurrentMemberId {
}