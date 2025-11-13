package com.mkomarov.controller;

import com.mkomarov.entity.UserEntity;
import com.mkomarov.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/api/users/*")
public class UserController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private static final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        log.info("Received GET request: /api/users/*. PathInfo: {}", pathInfo);

        resp.setContentType("application/json;charset=UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAllUsers(req, resp);
        } else {
            handleGetSpecificUser(pathInfo, req, resp);
        }
    }

    private void handleGetAllUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Dispatching GET request to: Get All Users");
        resp.getWriter().write("multiple user example");
    }

    private void handleGetSpecificUser(String pathInfo, HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String userIdStr = pathInfo.replaceAll("/", "");

        try {
            int userId = Integer.parseInt(userIdStr);
            log.info("Dispatching to: Get Specific User, id: {}", userId);

            Optional<UserEntity> userEntity = userService.getUserById(userId);

            if (userEntity.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("User not found");
                return;
            }

            resp.getWriter().write("single user example");
        } catch (NumberFormatException e) {
            log.error("Invalid User ID format: {}", userIdStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid User ID format\"}");
        }
    }
}
