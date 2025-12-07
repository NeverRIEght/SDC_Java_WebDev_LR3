package com.mkomarov.controller;

import com.mkomarov.config.ServiceRegistry;
import com.mkomarov.dto.MediafileDto;
import com.mkomarov.entity.MediafileEntity;
import com.mkomarov.service.MediafileService;
import com.mkomarov.utils.AuthUtils;
import com.mkomarov.utils.ErrorMessages;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.mkomarov.utils.ApiConstants.ERROR_JSON;
import static com.mkomarov.utils.ApiUtils.*;

@WebServlet("/api/mediafiles/*")
public class MediafileController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(MediafileController.class);

    private final ObjectMapper jsonToObjectMapper = new ObjectMapper();

    private final MediafileService mediafileService = ServiceRegistry.MEDIAFILE_SERVICE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        log.info("Received GET request: /api/mediafiles/*. PathInfo: {}", pathInfo);

        setJsonResponseType(resp);

        if (pathInfo != null && !pathInfo.equals("/")) {
            handleGet(pathInfo, resp);
        } else {
            log.error("Invalid GET request path: {}", pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write("{\"error\": \"Invalid request path\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }

        }
    }

    private void handleGet(String pathInfo, HttpServletResponse resp) {
        Optional<Long> pathId = extractIdFromPath(pathInfo, resp);
        if (pathId.isEmpty()) {
            return;
        }

        long id = pathId.get();

        log.info("Dispatching GET request to: getSpecific, id: {}", id);

        try {
            Optional<MediafileEntity> foundEntity = mediafileService.getById(id);

            if (foundEntity.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Entity not found");
                return;
            }

            setJsonResponseType(resp);

            MediafileEntity entity = foundEntity.get();
            String jsonResponseString = jsonToObjectMapper.writeValueAsString(entity);
            resp.getWriter().write(jsonResponseString);

            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (NumberFormatException _) {
            log.error("Invalid id format: {}", id);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Received POST request: /api/mediafiles");

        boolean isMultipart = JakartaServletFileUpload.isMultipartContent(req);

        if (!isMultipart) {
            log.error("Multipart content expected");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Multipart content expected\"}");
            return;
        } else {
            log.info("Multipart content detected, parsing...");
        }

        List<DiskFileItem> requestItems = parseMultipartRequestData(req, resp);
        log.info("Extracted {} parts from the request", requestItems.size());

        DiskFileItem filePart = null;
        for (DiskFileItem item : requestItems) {
            if (!item.isFormField() && "mediaFile".equals(item.getFieldName())) {
                log.info("Found mediaFile part in the request");
                filePart = item;
                break;
            }
        }

        if (filePart == null) {
            log.error("Missing mediaFile part in the request (client error)");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing mediaFile part in the request\"}");
            return;
        }

        Optional<Long> pathId = extractIdFromPath(req.getPathInfo(), resp);
        if (pathId.isEmpty()) {
            return;
        }
        long contactId = pathId.get();

        String ownerEmail = (String) req.getAttribute(AuthUtils.USER_EMAIL_ATTRIBUTE);

        try {
            String fileName = filePart.getName();
            if (fileName == null || fileName.isEmpty()) {
                log.error("Invalid file name");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid file name\"}");
                return;
            }

            byte[] fileContent = getStreamBytes(filePart.getInputStream());

            MediafileDto mediafileDto = MediafileDto.builder()
                    .id(null)
                    .associatedContactId(contactId)
                    .ownerEmail(ownerEmail)
                    .filename(fileName)
                    .hash(null)
                    .fileData(fileContent)
                    .contentType(filePart.getContentType())
                    .fileSize(filePart.getSize())
                    .build();

            mediafileService.create(mediafileDto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Error creating entity: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write(ERROR_JSON + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
        } catch (IOException ex) {
            log.error("{} {}", ErrorMessages.IOErrors.IO_ERROR, ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        log.info("Received DELETE request: /api/mediafiles/*. PathInfo: {}", pathInfo);

        Optional<Long> pathId = extractIdFromPath(pathInfo, resp);
        if (pathId.isEmpty()) {
            return;
        }

        try {
            mediafileService.deleteById(pathId.get());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            log.error("Error deleting entity: {}", e.getMessage());
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
