package com.youthexpedition.azit.adapter.in;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.youthexpedition.azit.global.common.response.CommonResponse;
import com.youthexpedition.azit.global.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.global.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.global.exception.BusinessException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    // 1. 성공 응답 테스트
    @GetMapping("/success")
    public CommonResponse<Map<String, String>> success() {
        return CommonResponse.of(CommonSuccessCode.SUCCESS, Map.of("data", "테스트 성공!"));
    }

    // 2. 의도된 비즈니스 예외 테스트 (404 Not Found)
    @GetMapping("/business-error")
    public void businessError() {
        throw new BusinessException(CommonErrorCode.USER_NOT_FOUND);
    }

    // 3. 검증 에러 테스트 (@Valid 미통과 시)
    @PostMapping("/validation-error")
    public CommonResponse<String> validationError(@Valid @RequestBody TestRequest request) {
        return CommonResponse.of(CommonSuccessCode.SUCCESS, "성공: " + request.getName());
    }

    // 4. 알 수 없는 서버 에러 테스트 (NullPointerException 등)
    @GetMapping("/unhandled-error")
    public void unhandledError() {
        throw new RuntimeException("예상치 못한 서버 에러 발생!");
    }
}