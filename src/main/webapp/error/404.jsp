<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>404 - Page Not Found</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 2rem;
            background-color: #f8f9fa;
            color: #333;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            background-color: white;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            text-align: center;
        }

        .error-code {
            font-size: 4rem;
            font-weight: bold;
            color: #ffc107;
            margin-bottom: 1rem;
        }

        .error-title {
            font-size: 1.5rem;
            margin-bottom: 1rem;
            color: #495057;
        }

        .error-description {
            margin-bottom: 2rem;
            color: #6c757d;
            line-height: 1.6;
        }

        .back-link {
            display: inline-block;
            padding: 0.75rem 1.5rem;
            background-color: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            transition: background-color 0.3s;
            margin-right: 1rem;
        }

        .back-link:hover {
            background-color: #0056b3;
        }

        .error-details {
            margin-top: 2rem;
            padding: 1rem;
            background-color: #f8f9fa;
            border-radius: 4px;
            text-align: left;
            font-size: 0.9rem;
            color: #6c757d;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="error-code">404</div>
        <div class="error-title">Page Not Found</div>
        <div class="error-description">
            The page you are looking for might have been removed, had its name changed,
            or is temporarily unavailable.
        </div>

        <a href="<%= request.getContextPath() %>/contacts.jsp" class="back-link">
            Back to App
        </a>
        <a href="<%= request.getContextPath() %>/register.jsp" class="back-link">
            Create an Account
        </a>
        <a href="<%= request.getContextPath() %>/login.jsp" class="back-link">
            Go to Login
        </a>

        <div class="error-details">
            <strong>Requested URL:</strong> <%= request.getAttribute("jakarta.servlet.error.request_uri") %><br>
            <strong>Status Code:</strong> <%= request.getAttribute("jakarta.servlet.error.status_code") %><br>
            <strong>Timestamp:</strong> <%= new java.util.Date() %>
        </div>
    </div>
</body>
</html>
