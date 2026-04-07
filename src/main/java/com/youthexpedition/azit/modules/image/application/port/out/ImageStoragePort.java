package com.youthexpedition.azit.modules.image.application.port.out;

import java.time.Duration;

public interface ImageStoragePort {
    String generatePresignedUrl(String s3Key, String contentType, Duration expiry);
    boolean exists(String s3Key);
    void delete(String s3Key);
    void move(String sourceKey, String destinationKey);
}
