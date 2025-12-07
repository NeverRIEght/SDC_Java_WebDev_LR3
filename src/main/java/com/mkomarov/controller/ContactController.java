package com.mkomarov.controller;

import com.mkomarov.config.ServiceRegistry;
import com.mkomarov.dto.ContactDto;
import com.mkomarov.entity.ContactEntity;
import com.mkomarov.service.ContactService;
import com.mkomarov.utils.AuthUtils;
import com.mkomarov.utils.ErrorMessages;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static com.mkomarov.utils.ApiConstants.ERROR_JSON;
import static com.mkomarov.utils.ApiUtils.*;

@WebServlet("/api/contacts/*")
public class ContactController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private static final ContactService contactService = ServiceRegistry.CONTACT_SERVICE;
    private final ObjectMapper jsonToObjectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        log.info("Received GET request: /api/contacts/*. PathInfo: {}", pathInfo);

        setJsonResponseType(resp);

        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAll(req, resp);
        } else {
            handleGet(pathInfo, req, resp);
        }
    }

    private void handleGetAll(HttpServletRequest req, HttpServletResponse resp) {
        log.info("Dispatching GET request to: getAll");

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);

        List<ContactEntity> contacts = contactService.getAllContacts(ownerEmail);
        setJsonResponseType(resp);
        String responseJson = jsonToObjectMapper.writeValueAsString(contacts);

        try {
            resp.getWriter().write(responseJson);
        } catch (IOException ex) {
            log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
        }

        log.info("contacts/getAll request fulfilled. Returning contacts list: {}", responseJson);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleGet(String pathInfo, HttpServletRequest req, HttpServletResponse resp) {
        String idStr = pathInfo.replace("/", "");

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);

        try {
            long id = Long.parseLong(idStr);
            log.info("Dispatching GET request to: getSpecific, id: {}", id);

            Optional<ContactEntity> foundEntity = contactService.getContactById(ownerEmail, id);

            if (foundEntity.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Contact not found");
                return;
            }

            setJsonResponseType(resp);

            ContactEntity contact = foundEntity.get();
            String contactJson = jsonToObjectMapper.writeValueAsString(contact);
            resp.getWriter().write(contactJson);

            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException _) {
            log.error("Invalid id format: {}", idStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write("{\"error\": \"Invalid id format\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        } catch (IOException ex) {
            log.error("{} {}", ErrorMessages.IOErrors.IO_ERROR, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        log.info("Received POST request: /api/contacts");

        String name = req.getParameter("name");
        String surname = req.getParameter("surname");
        String phoneNumber = req.getParameter("phoneNumber");

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);


        ContactDto contactDto = ContactDto.builder()
                .id(null)
                .ownerEmail(ownerEmail)
                .name(name)
                .surname(surname)
                .phoneNumber(phoneNumber)
                .build();

        try {
            contactService.createContact(contactDto);

            List<ContactEntity> contacts = contactService.getAllContacts(ownerEmail);
            String responseJson = jsonToObjectMapper.writeValueAsString(contacts);
            setJsonResponseType(resp);
            resp.getWriter().write(responseJson);

            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Error creating contact: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write(ERROR_JSON + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                resp.getWriter().write(ERROR_JSON + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        log.info("Received PUT request: /api/contacts/*. PathInfo: {}", pathInfo);

        Optional<Long> pathId = extractIdFromPath(pathInfo, resp);
        if (pathId.isEmpty()) {
            return;
        }

        String requestBody = reduceRequestBody(req);
        if (requestBody.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write("{\"error\": \"Missing request body\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
            return;
        }

        String name = null;
        String surname = null;
        String phoneNumber = null;

        for (String pair : requestBody.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);

                if ("name".equals(key)) name = value;
                else if ("surname".equals(key)) surname = value;
                else if ("phoneNumber".equals(key)) phoneNumber = value;
            }
        }

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);

        ContactDto contactDto = ContactDto.builder()
                .id(pathId.get())
                .ownerEmail(ownerEmail)
                .name(name)
                .surname(surname)
                .phoneNumber(phoneNumber)
                .build();

        try {
            contactService.updateContact(contactDto);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            log.error("Error updating contact: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write(ERROR_JSON + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                resp.getWriter().write(ERROR_JSON + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        log.info("Received DELETE request: /api/contacts/*. PathInfo: {}", pathInfo);

        Optional<Long> pathId = extractIdFromPath(pathInfo, resp);
        if (pathId.isEmpty()) {
            return;
        }

        try {
            contactService.deleteContact(pathId.get());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting contact: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write(ERROR_JSON + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                resp.getWriter().write(ERROR_JSON + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        }
    }
}
