package com.youthexpedition.azit.modules.member.adapter.in.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateMemberProfileRequest 유효성 검증 테스트")
class UpdateMemberProfileRequestTest {

    private static Validator validator;

    private static final String VALID_IMAGE_URL = "https://images.azitcrew.com/temp/profile/1/2026-04-22_uuid.jpg";

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    @DisplayName("닉네임 검증")
    class NicknameValidation {

        @Test
        @DisplayName("성공: 한글, 영문, 숫자 조합 닉네임은 유효하다.")
        void validate_nickname_success() {
            UpdateMemberProfileRequest request = new UpdateMemberProfileRequest("닉네임123", VALID_IMAGE_URL);

            Set<ConstraintViolation<UpdateMemberProfileRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @DisplayName("실패: 특수문자가 포함된 닉네임은 유효하지 않다.")
        @ValueSource(strings = {"nick!", "닉네임@", "nick#name", "닉_네임", "nick name"})
        void validate_nickname_fail_specialCharacter(String nickname) {
            UpdateMemberProfileRequest request = new UpdateMemberProfileRequest(nickname, VALID_IMAGE_URL);

            Set<ConstraintViolation<UpdateMemberProfileRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("닉네임은 특수문자를 포함할 수 없습니다."));
        }

        @Test
        @DisplayName("성공: 정확히 10자인 닉네임은 유효하다.")
        void validate_nickname_success_exactMaxLength() {
            UpdateMemberProfileRequest request = new UpdateMemberProfileRequest("가나다라마바사아자차", VALID_IMAGE_URL); // 10자

            Set<ConstraintViolation<UpdateMemberProfileRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("실패: 10자를 초과하는 닉네임은 유효하지 않다.")
        void validate_nickname_fail_exceedsMaxLength() {
            UpdateMemberProfileRequest request = new UpdateMemberProfileRequest("가나다라마바사아자차카", VALID_IMAGE_URL); // 11자

            Set<ConstraintViolation<UpdateMemberProfileRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("닉네임은 최대 10자까지 입력 가능합니다."));
        }

        @Test
        @DisplayName("실패: 빈 닉네임은 유효하지 않다.")
        void validate_nickname_fail_blank() {
            UpdateMemberProfileRequest request = new UpdateMemberProfileRequest("", VALID_IMAGE_URL);

            Set<ConstraintViolation<UpdateMemberProfileRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("닉네임은 필수입니다."));
        }
    }

    @Nested
    @DisplayName("이미지 URL 검증")
    class ImageUrlValidation {

        @Test
        @DisplayName("성공: 유효한 이미지 URL은 통과한다.")
        void validate_imageUrl_success() {
            UpdateMemberProfileRequest request = new UpdateMemberProfileRequest("닉네임", VALID_IMAGE_URL);

            Set<ConstraintViolation<UpdateMemberProfileRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("실패: 이미지 URL이 빈 값이면 유효하지 않다.")
        void validate_imageUrl_fail_blank() {
            UpdateMemberProfileRequest request = new UpdateMemberProfileRequest("닉네임", "");

            Set<ConstraintViolation<UpdateMemberProfileRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("이미지 URL은 필수입니다."));
        }

        @Test
        @DisplayName("실패: 이미지 URL이 null이면 유효하지 않다.")
        void validate_imageUrl_fail_null() {
            UpdateMemberProfileRequest request = new UpdateMemberProfileRequest("닉네임", null);

            Set<ConstraintViolation<UpdateMemberProfileRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .isNotEmpty()
                    .anyMatch(v -> v.getMessage().equals("이미지 URL은 필수입니다."));
        }
    }
}
