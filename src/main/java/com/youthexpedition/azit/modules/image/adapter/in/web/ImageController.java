package com.youthexpedition.azit.modules.image.adapter.in.web;

import com.youthexpedition.azit.infrastructure.common.annotation.CurrentMemberId;
import com.youthexpedition.azit.infrastructure.common.response.CommonResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonSuccessCode;
import com.youthexpedition.azit.modules.image.adapter.in.web.docs.ImageControllerDocs;
import com.youthexpedition.azit.modules.image.adapter.in.web.dto.PresignedUrlRequest;
import com.youthexpedition.azit.modules.image.application.port.in.ImageUseCase;
import com.youthexpedition.azit.modules.image.application.port.in.dto.PresignedUrlResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController implements ImageControllerDocs {

    private final ImageUseCase imageUseCase;

    @PostMapping("/presigned-url")
    public CommonResponse<PresignedUrlResponse> generatePresignedUrl(@CurrentMemberId Long memberId, @Valid @RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = imageUseCase.generatePresignedUrl(request.toCommand(memberId));
        return CommonResponse.of(CommonSuccessCode.SUCCESS, response);
    }
}
