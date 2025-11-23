package com.mkomarov.controller;

import com.mkomarov.entity.UserEntity;
import com.mkomarov.service.PasswordService;
import com.mkomarov.service.UserService;
import com.mkomarov.utils.PagesConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/api/register")
public class RegisterController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    private static final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Received POST request: /api/register");

        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String passwordConfirm = req.getParameter("passwordConfirm");

        if (email == null || email.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email is required");
            return;
        }

        email = email.trim();

        Optional<UserEntity> existingUser = userService.getUserByEmail(email);
        if (existingUser.isPresent()) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "User with this email already exists");
            return;
        }

        if (password == null || password.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Password is required");
            return;
        }

        password = password.trim();

        if (passwordConfirm == null || passwordConfirm.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Password confirmation is required");
            return;
        }

        passwordConfirm = passwordConfirm.trim();

        boolean passwordsMatch = password.equals(passwordConfirm);

        if (!passwordsMatch) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Passwords do not match");
            return;
        }

        String hashedPassword = PasswordService.hashPassword(password);

        req.setAttribute("email", email);
        req.setAttribute("hashedPassword", hashedPassword);

        userService.registerUser(req);
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.sendRedirect(PagesConstants.LOGIN_PAGE);
        log.info("User registered with email: {}", email);
    }
}
