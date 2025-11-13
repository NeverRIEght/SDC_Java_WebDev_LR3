package com.mkomarov.controller;

import com.mkomarov.entity.ContactEntity;
import com.mkomarov.entity.UserEntity;
import com.mkomarov.service.ContactService;
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

@WebServlet("/api/contacts/*")
public class ContactController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService = new ContactService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        log.info("Received GET request: /api/contacts/*. PathInfo: {}", pathInfo);

        resp.setContentType("application/json;charset=UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAll(req, resp);
        } else {
            handleGetSpecific(pathInfo, req, resp);
        }
    }

    private void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Dispatching GET request to: getAll");
        resp.getWriter().write("multiple contacts example");
    }

    private void handleGetSpecific(String pathInfo, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userIdStr = pathInfo.replaceAll("/", "");

        try {
            int userId = Integer.parseInt(userIdStr);
            log.info("Dispatching GET request to: getSpecific, id: {}", userId);

            Optional<ContactEntity> foundEntity = contactService.getContactById(userId);

            if (foundEntity.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Contact not found");
                return;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid id format: {}", userIdStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid id format\"}");
        }
    }
}
