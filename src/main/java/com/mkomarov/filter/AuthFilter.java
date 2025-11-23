package com.mkomarov.filter;

import com.mkomarov.utils.AuthUtils;
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

import static com.mkomarov.utils.AuthUtils.USER_EMAIL_ATTRIBUTE;

@WebFilter("/api/*")
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // Allow only public API endpoints (register/login)
        if (AuthUtils.isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // For all other /api/* paths require authentication
        // 1) If user is already authenticated in session, set attribute and continue
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object emailObj = session.getAttribute(USER_EMAIL_ATTRIBUTE);
            if (emailObj instanceof String emailStr) {
                req.setAttribute(USER_EMAIL_ATTRIBUTE, emailStr);
                chain.doFilter(request, response);
                return;
            }
        }

        // TODO: JSessionID???

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
                            HttpSession s = req.getSession(true);
                            s.setAttribute(USER_EMAIL_ATTRIBUTE, email);
                            req.setAttribute(USER_EMAIL_ATTRIBUTE, email);
                            chain.doFilter(request, response);
                            return;
                        }
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // ignore decode errors
            }
        }

        resp.setHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public void destroy() {
    }
}
