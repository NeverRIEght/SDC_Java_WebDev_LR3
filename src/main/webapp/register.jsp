<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Register</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 2rem;
        }

        .container {
            max-width: 420px;
            margin: 0 auto;
        }

        label {
            display: block;
            margin-top: 0.75rem;
        }

        input[type="email"], input[type="password"] {
            width: 100%;
            padding: 0.5rem;
            box-sizing: border-box;
        }

        .actions {
            margin-top: 1rem;
        }

        .hint {
            font-size: 0.9rem;
            color: #555;
            margin-top: 0.5rem;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Register</h1>

    <form method="post" action="${pageContext.request.contextPath}/api/register">
        <label for="email">Email</label>
        <input id="email" name="email" type="email" required autocomplete="email"/>

        <label for="password">Password</label>
        <input id="password" name="password" type="password" required autocomplete="new-password"/>

        <label for="passwordConfirm">Confirm password</label>
        <input id="passwordConfirm" name="passwordConfirm" type="password" required autocomplete="new-password"/>

        <div class="actions">
            <button type="submit">Register</button>
        </div>
    </form>
</div>

</body>
</html>
