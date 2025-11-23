package com.mkomarov.utils;

import java.util.List;

public final class AuthUtils {
    public static final String USER_EMAIL_ATTRIBUTE = "userEmail";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/register",
            "/api/login"
    );

    public static boolean isPublicPath(String path) {
        if (path == null || path.isEmpty()) return false;
        String lower = path.toLowerCase();
        return PUBLIC_PATHS.contains(lower);
    }
}
