package com.youthexpedition.azit.modules.crew.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LocationEntity {

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "meeting_spot", nullable = false, length = 15)
    private String meetingSpot;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;
}
