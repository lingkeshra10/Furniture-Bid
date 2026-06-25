package com.furniturebid.mockapi.security;

import com.furniturebid.mockapi.dto.response.ApiErrorResponse;
import com.furniturebid.mockapi.exception.TokenExpiredException;
import com.furniturebid.mockapi.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;
    private final JsonMapper jsonMapper;

    private static final List<SkipRule> SKIP_RULES = List.of(
            new SkipRule("POST", "/api/auth/login"),
            new SkipRule("POST", "/api/auth/register"),
            new SkipRule("POST", "/api/auth/reset-password"),
            new SkipRule("POST", "/api/auth/social-login"),
            new SkipRule("GET", "/api/furniture"),
            new SkipRule("GET", "/api/auctions/", "/bids")
    );

    private static final String REFRESH_TOKEN_PATH = "/api/auth/refresh-token";

    public JwtAuthFilter(JwtUtility jwtUtility, JsonMapper jsonMapper) {
        this.jwtUtility = jwtUtility;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (shouldSkip(method, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeErrorResponse(response, 401, "UNAUTHORIZED",
                    "Missing or malformed Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims;

            if (path.equals(REFRESH_TOKEN_PATH)) {
                claims = jwtUtility.parseTokenAllowExpired(token);
            } else {
                claims = jwtUtility.parseToken(token);
            }

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            request.setAttribute("authenticatedUser", new AuthenticatedUser(userId, role));

            filterChain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            writeErrorResponse(response, 401, "TOKEN_EXPIRED", e.getMessage());
        } catch (UnauthorizedException e) {
            writeErrorResponse(response, 401, "UNAUTHORIZED", e.getMessage());
        } catch (Exception e) {
            writeErrorResponse(response, 401, "UNAUTHORIZED", "Invalid token");
        }
    }

    private boolean shouldSkip(String method, String path) {
        for (SkipRule rule : SKIP_RULES) {
            if (rule.matches(method, path)) {
                return true;
            }
        }
        return false;
    }

    private void writeErrorResponse(HttpServletResponse response,
                                    int statusCode,
                                    String errorCode,
                                    String message) throws IOException {

        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse errorResponse = new ApiErrorResponse(statusCode, errorCode, message);
        response.getWriter().write(jsonMapper.writeValueAsString(errorResponse));
    }

    private static class SkipRule {
        private final String method;
        private final String pathPrefix;
        private final String pathSuffix;

        SkipRule(String method, String pathPrefix) {
            this.method = method;
            this.pathPrefix = pathPrefix;
            this.pathSuffix = null;
        }

        SkipRule(String method, String pathPrefix, String pathSuffix) {
            this.method = method;
            this.pathPrefix = pathPrefix;
            this.pathSuffix = pathSuffix;
        }

        boolean matches(String reqMethod, String reqPath) {
            if (!method.equalsIgnoreCase(reqMethod)) {
                return false;
            }

            if (pathSuffix == null) {
                return reqPath.equals(pathPrefix)
                        || reqPath.startsWith(pathPrefix + "/")
                        || reqPath.startsWith(pathPrefix + "?");
            }

            return reqPath.startsWith(pathPrefix) && reqPath.endsWith(pathSuffix);
        }
    }
}