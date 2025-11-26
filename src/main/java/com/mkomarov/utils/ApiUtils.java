package com.mkomarov.utils;

import com.mkomarov.controller.ContactController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static com.mkomarov.utils.ApiConstants.JSON_CONTENT_TYPE;
import static com.mkomarov.utils.ApiConstants.UTF8_ENCODING;

public class ApiUtils {
    private static final Logger log = LoggerFactory.getLogger(ApiUtils.class);

    public static Optional<Long> parseIdPathVariable(String pathInfo, HttpServletResponse resp) {
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
}
