package com.mkomarov.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletDiskFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.mkomarov.utils.ApiConstants.JSON_CONTENT_TYPE;
import static com.mkomarov.utils.ApiConstants.UTF8_ENCODING;

public class ApiUtils {
    private static final Logger log = LoggerFactory.getLogger(ApiUtils.class);

    public static Optional<Long> extractIdFromPath(String pathInfo, HttpServletResponse resp) {
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                resp.getWriter().write("{\"error\": \"Missing ID in URL\"}");
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

    public static String reduceRequestBody(HttpServletRequest req) {
        String requestBody = "";
        try {
            requestBody = req.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);
        } catch (IOException ex) {
            log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
        }

        return requestBody;
    }

    public static void setJsonResponseType(HttpServletResponse resp) {
        resp.setContentType(JSON_CONTENT_TYPE);
        resp.setCharacterEncoding(UTF8_ENCODING);
    }

    public static List<DiskFileItem> parseMultipartRequestData(HttpServletRequest req, HttpServletResponse resp) {
        DiskFileItemFactory factory = DiskFileItemFactory.builder()
                .setPath(System.getProperty("java.io.tmpdir"))
                .get();
        JakartaServletDiskFileUpload upload = new JakartaServletDiskFileUpload(factory);

        try {
            return upload.parseRequest(req);
        } catch (FileUploadException e) {
            log.error("Failed to parse multipart request: {}", e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                resp.getWriter().write("{\"error\": \"Server failed to process multipart data\"}");
            } catch (IOException ex) {
                log.error("{} {}", ErrorMessages.IOErrors.NETWORK_ERROR, ex);
            }
            return Collections.emptyList();
        }
    }

    public static byte[] getStreamBytes(InputStream stream) {
        byte[] fileBytes;
        try {
            fileBytes = org.apache.commons.io.IOUtils.toByteArray(stream);
        } catch (IOException e) {
            log.error("Error reading mediafile stream: {}", e.getMessage());
            throw new RuntimeException("Error processing mediafile data.", e);
        }

        return fileBytes;
    }
}
