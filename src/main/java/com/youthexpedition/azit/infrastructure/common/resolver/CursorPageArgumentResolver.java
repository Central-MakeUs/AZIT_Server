package com.youthexpedition.azit.infrastructure.common.resolver;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
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

        Long cursorId = (cursorIdParam != null) ? Long.valueOf(cursorIdParam) : null;
        Integer size = (sizeParam != null) ? Integer.valueOf(sizeParam) : null;

        return CursorPageQuery.of(cursorId, size);
    }
}
