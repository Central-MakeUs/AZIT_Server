package com.youthexpedition.azit.modules.member.adapter.in.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateNicknameRequest 유효성 검증 테스트")
class UpdateNicknameRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("성공: 한글, 영문, 숫자 조합 닉네임은 유효하다.")
    void validate_success() {
        UpdateNicknameRequest request = new UpdateNicknameRequest("닉네임123");

        Set<ConstraintViolation<UpdateNicknameRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("실패: 특수문자가 포함된 닉네임은 유효하지 않다.")
    @ValueSource(strings = {"nick!", "닉네임@", "nick#name", "닉_네임", "nick name"})
    void validate_fail_specialCharacter(String nickname) {
        UpdateNicknameRequest request = new UpdateNicknameRequest(nickname);

        Set<ConstraintViolation<UpdateNicknameRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("닉네임은 특수문자를 포함할 수 없습니다."));
    }

    @Test
    @DisplayName("성공: 정확히 10자인 닉네임은 유효하다.")
    void validate_success_exactMaxLength() {
        UpdateNicknameRequest request = new UpdateNicknameRequest("가나다라마바사아자차");  // 10자

        Set<ConstraintViolation<UpdateNicknameRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("실패: 10자를 초과하는 닉네임은 유효하지 않다.")
    void validate_fail_exceedsMaxLength() {
        UpdateNicknameRequest request = new UpdateNicknameRequest("가나다라마바사아자차카");  // 11자

        Set<ConstraintViolation<UpdateNicknameRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("닉네임은 최대 10자까지 입력 가능합니다."));
    }

    @Test
    @DisplayName("실패: 빈 닉네임은 유효하지 않다.")
    void validate_fail_blank() {
        UpdateNicknameRequest request = new UpdateNicknameRequest("");

        Set<ConstraintViolation<UpdateNicknameRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("닉네임은 필수입니다."));
    }
}
