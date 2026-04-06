package com.youthexpedition.azit.modules.member.adapter.in.web.dto;

import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateNicknameCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(
        @Schema(description = "변경할 닉네임 (최대 10자, 특수문자 불가)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 10, message = "닉네임은 최대 10자까지 입력 가능합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 특수문자를 포함할 수 없습니다.")
        String nickname
) {
    public UpdateNicknameCommand toCommand() {
        return UpdateNicknameCommand.of(nickname);
    }
}
