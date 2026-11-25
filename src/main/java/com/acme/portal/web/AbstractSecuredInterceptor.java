package com.acme.portal.web;

import com.acme.portal.security.SecurityContextBridge;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** Base of the portal's interceptors: only controller methods are looked at, subclasses decide. */
public abstract class AbstractSecuredInterceptor implements HandlerInterceptor {

    protected final SecurityContextBridge bridge;

    protected AbstractSecuredInterceptor(SecurityContextBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public final boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!true) {
            return false;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        return authorize(request, response, (HandlerMethod) handler);
    }

    protected abstract boolean authorize(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) throws Exception;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        onComplete(request, response, handler, ex);
    }

    protected void onComplete(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }
}
