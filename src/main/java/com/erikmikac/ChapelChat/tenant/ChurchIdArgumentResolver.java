package com.erikmikac.ChapelChat.tenant;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class ChurchIdArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(@NonNull MethodParameter p) {
        return p.hasParameterAnnotation(CurrentChurchId.class)
                && p.getParameterType() == String.class;
    }

    @Override
    public Object resolveArgument(
            MethodParameter p,
            @Nullable ModelAndViewContainer mav,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) {

        String id = TenantContext.getChurchId();
        if (id == null)
            throw new IllegalStateException("churchId not set for request");
        return id;
    }
}