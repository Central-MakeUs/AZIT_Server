package com.youthexpedition.azit.infrastructure.config.swagger;

import com.youthexpedition.azit.infrastructure.common.response.CommonErrorResponse;
import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApiErrorCodeExampleCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiErrorCodeExamples annotation = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);

        if (annotation != null) {
            generateErrorCodeResponseExample(operation, annotation.value());
        }

        return operation;
    }

    private void generateErrorCodeResponseExample(Operation operation, String[] errorNames) {
        ApiResponses responses = operation.getResponses();

        // CommonErrorCode 전역 에러 코드들 먼저 담기
        List<BaseErrorCode> selectedErrorCodes = new ArrayList<>(Arrays.asList(CommonErrorCode.values()));

        // 검색 대상이 될 도메인별 에러 Enum 리스트
        List<Class<? extends BaseErrorCode>> errorCodeEnums = List.of(
                 MemberErrorCode.class, AuthErrorCode.class, CrewErrorCode.class, StoreErrorCode.class
        );

        // 어노테이션에 명시된 에러 코드들을 찾아서 추가
        if (errorNames.length > 0) {
            List<String> specificNames = Arrays.asList(errorNames);

            Set<String> existingErrorNames = selectedErrorCodes.stream()
                    .map(BaseErrorCode::toString)
                    .collect(Collectors.toSet());

            errorCodeEnums.stream()
                    .flatMap(enumClass -> Arrays.stream(enumClass.getEnumConstants()))
                    .filter(errorCode -> specificNames.contains(errorCode.toString()))
                    .filter(errorCode -> !existingErrorNames.contains(errorCode.toString()))
                    .forEach(selectedErrorCodes::add);
        }

        // HttpStatus 별로 그룹화하여 스웨거에 주입
        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = selectedErrorCodes.stream()
                .map(errorCode -> ExampleHolder.builder()
                        .holder(getSwaggerExample(errorCode))
                        .name(errorCode.toString())
                        .code(errorCode.getStatus().value())
                        .build())
                .collect(Collectors.groupingBy(ExampleHolder::getCode));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private Example getSwaggerExample(BaseErrorCode errorCode) {
        CommonErrorResponse errorResponse = CommonErrorResponse.of(errorCode);
        Example example = new Example();
        example.setValue(errorResponse);
        return example;
    }

    private void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((status, v) -> {
            ApiResponse apiResponse = responses.computeIfAbsent(String.valueOf(status), k -> new ApiResponse());
            Content content = apiResponse.getContent();
            if (content == null) {
                content = new Content();
                apiResponse.setContent(content);
            }

            MediaType mediaType = content.computeIfAbsent("application/json", k -> new MediaType());
            v.forEach(exampleHolder -> mediaType.addExamples(exampleHolder.getName(), exampleHolder.getHolder()));
        });
    }
}