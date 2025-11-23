package com.mkomarov.controller;

import com.mkomarov.utils.AuthUtils;
import com.mkomarov.dto.ContactDto;
import com.mkomarov.entity.ContactEntity;
import com.mkomarov.service.ContactService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/contacts/*")
public class ContactController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService = new ContactService();
    private final ObjectMapper jsonToObjectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);

        List<ContactEntity> contacts = contactService.getAllContacts(ownerEmail);
        StringBuilder response = new StringBuilder();
        contacts.forEach(contact -> {
            contact.setOwner(null);
            String contactJson = jsonToObjectMapper.writeValueAsString(contact);
            response.append(contactJson).append("\n");
        });
        resp.getWriter().write(response.toString());
        resp.setStatus(HttpServletResponse.SC_OK);
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

            ContactEntity contact = foundEntity.get();
            contact.setOwner(null);
            String contactJson = jsonToObjectMapper.writeValueAsString(contact);
            resp.getWriter().write(contactJson);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException e) {
            log.error("Invalid id format: {}", userIdStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid id format\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Received POST request: /api/contacts");

        String name = req.getParameter("name");
        String surname = req.getParameter("surname");
        String phoneNumber = req.getParameter("phoneNumber");

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);

        ContactDto contactDto = new ContactDto(
                null,
                ownerEmail,
                name,
                surname,
                phoneNumber
        );

        try {
            contactService.createContact(contactDto);

            List<ContactEntity> contacts = contactService.getAllContacts(ownerEmail);

            StringBuilder response = new StringBuilder();

            contacts.forEach(contact -> {
                contact.setOwner(null);
                String contactJson = jsonToObjectMapper.writeValueAsString(contact);
                response.append(contactJson).append("\n");
            });

            resp.getWriter().write(response.toString());

            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Error creating contact: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Received PUT request: /api/contacts");

        long id;
        try {
            id = Long.parseLong(req.getParameter("id"));
        } catch (NumberFormatException e) {
            log.error("Invalid id format: {}", req.getParameter("id"));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid id format\"}");
            return;
        }

        String name = req.getParameter("name");
        String surname = req.getParameter("surname");
        String phoneNumber = req.getParameter("phoneNumber");

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);

        ContactDto contactDto = new ContactDto(
                id,
                ownerEmail,
                name,
                surname,
                phoneNumber
        );

        try {
            contactService.updateContact(contactDto);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            log.error("Error updating contact: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Received DELETE request: /api/contacts");

        long id;
        try {
            id = Long.parseLong(req.getParameter("id"));
        } catch (NumberFormatException e) {
            log.error("Invalid id format: {}", req.getParameter("id"));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid id format\"}");
            return;
        }

        try {
            contactService.deleteContact(id);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting contact: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
