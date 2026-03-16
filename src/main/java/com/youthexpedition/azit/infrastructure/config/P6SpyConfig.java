package com.youthexpedition.azit.infrastructure.config;

import com.p6spy.engine.spy.P6SpyOptions;
import com.youthexpedition.azit.infrastructure.config.formatter.P6SpyFormatter;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class P6SpyConfig {
    @PostConstruct
    public void setLogMessageFormat() {
        // p6spy 기본 출력 방식으로 설정
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6SpyFormatter.class.getName());
    }
}
