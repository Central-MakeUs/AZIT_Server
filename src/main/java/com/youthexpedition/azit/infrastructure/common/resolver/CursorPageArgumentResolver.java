package com.youthexpedition.azit.infrastructure.common.resolver;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CursorPageArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터 타입이 CursorPageQuery인 경우에만 동작
        return parameter.getParameterType().equals(CursorPageQuery.class);
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @Nonnull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        String cursorIdParam = webRequest.getParameter("cursorId");
        String sizeParam = webRequest.getParameter("size");

        try {
            // 숫자로 변환 시도
            Long cursorId = (StringUtils.hasText(cursorIdParam)) ? Long.valueOf(cursorIdParam) : null;
            Integer size = (StringUtils.hasText(sizeParam)) ? Integer.valueOf(sizeParam) : null;

            return CursorPageQuery.of(cursorId, size);
        } catch (NumberFormatException e) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
