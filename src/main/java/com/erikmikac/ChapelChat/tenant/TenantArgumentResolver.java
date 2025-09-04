package com.erikmikac.ChapelChat.tenant;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.erikmikac.ChapelChat.tenant.web.CurrentChurchId;
import com.erikmikac.ChapelChat.tenant.web.CurrentOrgId;
import com.erikmikac.ChapelChat.tenant.web.CurrentOrgType;
import com.erikmikac.ChapelChat.tenant.web.CurrentTenant;
import com.erikmikac.ChapelChat.tenant.web.CurrentTenantId;
@Component
public class TenantArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(@NonNull MethodParameter p) {
    return p.hasParameterAnnotation(CurrentTenant.class)
        || p.hasParameterAnnotation(CurrentOrgId.class)
        || p.hasParameterAnnotation(CurrentTenantId.class)
        || p.hasParameterAnnotation(CurrentOrgType.class)
        || p.hasParameterAnnotation(CurrentChurchId.class) // back-compat
        || (p.hasParameterAnnotation(CurrentTenant.class)
            && p.getParameterType().isAssignableFrom(TenantContext.Context.class));
  }

  @Override
  public Object resolveArgument(
      @NonNull MethodParameter p,
      @Nullable ModelAndViewContainer mav,
      @NonNull NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) throws Exception {
    TenantContext.Context ctx = TenantContext.get();
    if (ctx == null || ctx.getOrgId() == null) {
      // 403-ish error that plays nicely with Spring MVC exception handling
      throw new MissingRequestValueException("Tenant context not set for request");
    }

    if (p.hasParameterAnnotation(CurrentTenant.class)) {
      // If the param type is the Context itself, return it; otherwise be explicit.
      if (p.getParameterType().isAssignableFrom(TenantContext.Context.class))
        return ctx;
      // For safety you can also support String with @CurrentTenant to mean orgId, but
      // better to be explicit.
    }

    if (p.hasParameterAnnotation(CurrentOrgId.class))
      return ctx.getOrgId();
    if (p.hasParameterAnnotation(CurrentTenantId.class))
      return ctx.getTenantId();
    if (p.hasParameterAnnotation(CurrentOrgType.class))
      return ctx.getOrgType();

    // Backward compatibility
    if (p.hasParameterAnnotation(CurrentChurchId.class))
      return ctx.getOrgId();

    // Should not reach here because supportsParameter gated it.
    throw new IllegalStateException("Unsupported tenant parameter: " + p);
  }
}