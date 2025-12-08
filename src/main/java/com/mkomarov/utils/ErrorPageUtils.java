package com.mkomarov.utils;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ErrorPageUtils {
    private static final Logger log = LoggerFactory.getLogger(ErrorPageUtils.class);

    public static void sendBadRequestError(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorMessage
    ) throws ServletException, IOException {
        log.warn("Bad Request: {}", errorMessage);

        request.setAttribute("errorMessage", errorMessage);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        RequestDispatcher dispatcher = request.getRequestDispatcher(PagesConstants.ERROR_400_PAGE);
        dispatcher.forward(request, response);
    }

    public static void sendNotFoundError(
            HttpServletRequest request,
            HttpServletResponse response,
            String resourcePath
    ) throws ServletException, IOException {
        log.warn("Resource not found: {}", resourcePath);

        request.setAttribute("resourcePath", resourcePath);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        RequestDispatcher dispatcher = request.getRequestDispatcher(PagesConstants.ERROR_404_PAGE);
        dispatcher.forward(request, response);
    }

    public static void sendInternalServerError(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws ServletException, IOException {
        log.error("Internal server error", exception);

        request.setAttribute("exception", exception);
        request.setAttribute("jakarta.servlet.error.exception", exception);
        request.setAttribute("jakarta.servlet.error.exception_type", exception.getClass().getName());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        RequestDispatcher dispatcher = request.getRequestDispatcher(PagesConstants.ERROR_500_PAGE);
        dispatcher.forward(request, response);
    }

    public static void handleException(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws ServletException, IOException {
        if (exception instanceof IllegalArgumentException) {
            sendBadRequestError(request, response, exception.getMessage());
        } else {
            sendInternalServerError(request, response, exception);
        }
    }
}
