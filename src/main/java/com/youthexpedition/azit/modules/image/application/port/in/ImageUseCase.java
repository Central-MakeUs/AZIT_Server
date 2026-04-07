package com.youthexpedition.azit.modules.image.application.port.in;

import com.youthexpedition.azit.modules.image.application.port.in.command.GeneratePresignedUrlCommand;
import com.youthexpedition.azit.modules.image.application.port.in.dto.PresignedUrlResponse;

public interface ImageUseCase {
    PresignedUrlResponse generatePresignedUrl(GeneratePresignedUrlCommand command);
}
