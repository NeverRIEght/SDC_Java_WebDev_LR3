package com.mkomarov.controller;

import com.mkomarov.utils.AuthUtils;
import com.mkomarov.entity.UserEntity;
import com.mkomarov.service.PasswordService;
import com.mkomarov.service.UserService;
import com.mkomarov.utils.PagesConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/api/login")
public class LoginController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private static final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Received POST request: /api/login");

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email is required");
            return;
        }

        email = email.trim();
        Optional<UserEntity> existingUser = userService.getUserByEmail(email);
        if (existingUser.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password");
            return;
        }

        if (password == null || password.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Password is required");
            return;
        }

        password = password.trim();
        String existingPasswordHash = existingUser.get().getPasswordHash();
        String hashedPassword = PasswordService.hashPassword(password);

        if (!hashedPassword.equals(existingPasswordHash)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE, email);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.sendRedirect(PagesConstants.LOGIN_PAGE);
        log.info("User logged in with email: {}", email);
    }
}
