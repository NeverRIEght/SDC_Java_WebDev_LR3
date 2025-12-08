<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>500 - Internal Server Error</title>
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
            color: #dc3545;
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

        .exception-details {
            margin-top: 1rem;
            padding: 1rem;
            background-color: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 4px;
            text-align: left;
            font-family: monospace;
            font-size: 0.8rem;
            color: #856404;
            max-height: 200px;
            overflow-y: auto;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="error-code">500</div>
        <div class="error-title">Internal Server Error</div>
        <div class="error-description">
            Something went wrong on our end. We're working to fix this issue.
            Please try again later or contact support if the problem persists.
        </div>

        <a href="<%= request.getContextPath() %>/contacts.jsp" class="back-link">
            Back to App
        </a>

        <div class="error-details">
            <strong>Request URI:</strong> <%= request.getAttribute("jakarta.servlet.error.request_uri") %><br>
            <strong>Status Code:</strong> <%= request.getAttribute("jakarta.servlet.error.status_code") %><br>
            <strong>Exception Type:</strong> <%= request.getAttribute("jakarta.servlet.error.exception_type") %><br>
            <strong>Timestamp:</strong> <%= new java.util.Date() %>
        </div>
    </div>
</body>
</html>
