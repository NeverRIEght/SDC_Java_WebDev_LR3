package com.mkomarov.filter;

import com.mkomarov.entity.UserEntity;
import com.mkomarov.repository.UserRepository;
import com.mkomarov.service.PasswordService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@WebFilter("/api/*")
public class AuthFilter implements Filter {
    private static final String USER_EMAIL_ATTR = "userEmail";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String ctx = req.getContextPath();
        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }

        // Allow only public API endpoints (register/login)
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // For all other /api/* paths require authentication
        // 1) If user is already authenticated in session, set attribute and continue
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object emailObj = session.getAttribute(USER_EMAIL_ATTR);
            if (emailObj instanceof String emailStr) {
                req.setAttribute(USER_EMAIL_ATTR, emailStr);
                chain.doFilter(request, response);
                return;
            }
        }

        // 2) Try HTTP Basic auth header (Authorization: Basic base64(email:password))
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Basic ")) {
            String base64 = auth.substring(6).trim();
            try {
                byte[] decoded = Base64.getDecoder().decode(base64);
                String cred = new String(decoded, StandardCharsets.UTF_8);
                int idx = cred.indexOf(':');
                if (idx > 0) {
                    String email = cred.substring(0, idx);
                    String password = cred.substring(idx + 1);

                    UserRepository repo = new UserRepository("users");
                    Optional<UserEntity> userOpt = repo.findByEmail(email);
                    if (userOpt.isPresent()) {
                        UserEntity user = userOpt.get();
                        String hashedProvided = PasswordService.hashPassword(password);
                        if (hashedProvided.equals(user.getPasswordHash())) {
                            // Auth successful: set session attribute and request attribute
                            HttpSession s = req.getSession(true);
                            s.setAttribute(USER_EMAIL_ATTR, email);
                            req.setAttribute(USER_EMAIL_ATTR, email);
                            chain.doFilter(request, response);
                            return;
                        }
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // ignore decode errors
            }
        }

        // If we reach here, authentication failed -> respond 401
        resp.setHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty()) return false;
        String lower = path.toLowerCase();
        // Allow exact register/login API endpoints (and their subpaths if any)
        return lower.equals("/api/register");
    }

    @Override
    public void destroy() {
    }
}
