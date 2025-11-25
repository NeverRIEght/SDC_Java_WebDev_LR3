package com.mkomarov.controller;

import com.mkomarov.utils.AuthUtils;
import com.mkomarov.dto.ContactDto;
import com.mkomarov.entity.ContactEntity;
import com.mkomarov.service.ContactService;
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

@WebServlet("/api/contacts/*")
public class ContactController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String UTF8_ENCODING = "UTF-8";
    private static final String ERROR_JSON = "{\"error\": \"";

    private final ContactService contactService = new ContactService();
    private final ObjectMapper jsonToObjectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        log.info("Received GET request: /api/contacts/*. PathInfo: {}", pathInfo);

        setJsonResponseType(resp);

        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetAll(req, resp);
        } else {
            handleGet(pathInfo, resp);
        }
    }

    private void handleGetAll(HttpServletRequest req, HttpServletResponse resp) {
        log.info("Dispatching GET request to: getAll");

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);

        List<ContactEntity> contacts = contactService.getAllContacts(ownerEmail);
        contacts.forEach(contact -> contact.getOwner().setPasswordHash(null));
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

    private void handleGet(String pathInfo, HttpServletResponse resp) {
        String userIdStr = pathInfo.replace("/", "");

        try {
            int userId = Integer.parseInt(userIdStr);
            log.info("Dispatching GET request to: getSpecific, id: {}", userId);

            Optional<ContactEntity> foundEntity = contactService.getContactById(userId);

            if (foundEntity.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Contact not found");
                return;
            }

            setJsonResponseType(resp);

            ContactEntity contact = foundEntity.get();
            contact.setOwner(null);
            String contactJson = jsonToObjectMapper.writeValueAsString(contact);
            resp.getWriter().write(contactJson);

            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException _) {
            log.error("Invalid id format: {}", userIdStr);
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
            contacts.forEach(contact -> contact.getOwner().setPasswordHash(null));
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

        Optional<Long> pathId = parseIdPathVariable(pathInfo, resp);
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

        ContactDto contactDto = new ContactDto(
                pathId.get(),
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

        Optional<Long> pathId = parseIdPathVariable(pathInfo, resp);
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

    private Optional<Long> parseIdPathVariable(String pathInfo, HttpServletResponse resp) {
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write("{\"error\": \"Missing contact ID in URL\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
            return Optional.empty();
        }

        String idStr = pathInfo.replace("/", "");
        try {
            long id = Long.parseLong(idStr);
            return Optional.of(id);
        } catch (NumberFormatException _) {
            log.error("Invalid id format in URL: {}", idStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write("{\"error\": \"Invalid id format in URL\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        }

        return Optional.empty();
    }

    private String reduceRequestBody(HttpServletRequest req) {
        String requestBody = "";
        try {
            requestBody = req.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);
        } catch (IOException ex) {
            log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
        }

        return requestBody;
    }

    private void setJsonResponseType(HttpServletResponse resp) {
        resp.setContentType(JSON_CONTENT_TYPE);
        resp.setCharacterEncoding(UTF8_ENCODING);
    }
}
