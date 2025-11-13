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
}
