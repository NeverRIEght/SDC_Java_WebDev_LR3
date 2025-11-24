<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Contacts Manager</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #f5f5f5;
        }

        .container {
            max-width: 800px;
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        h1 {
            text-align: center;
            color: #333;
        }

        .form-container {
            margin-bottom: 30px;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
            background-color: #f9f9f9;
        }

        .form-group {
            margin-bottom: 15px;
        }

        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }

        input[type="text"] {
            width: 100%;
            padding: 8px;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box;
        }

        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            margin-right: 5px;
        }

        button:hover {
            background-color: #45a049;
        }

        .delete-btn {
            background-color: #f44336;
        }

        .delete-btn:hover {
            background-color: #da190b;
        }

        .save-btn {
            background-color: #2196F3;
        }

        .save-btn:hover {
            background-color: #0b7dda;
        }

        .contacts-container {
            margin-top: 20px;
        }

        .contact-list {
            max-height: 500px;
            overflow-y: auto;
            border: 1px solid #ddd;
            border-radius: 5px;
        }

        .contact-item {
            padding: 15px;
            border-bottom: 1px solid #eee;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .contact-item:last-child {
            border-bottom: none;
        }

        .contact-info {
            flex: 1;
        }

        .contact-info div {
            margin-bottom: 5px;
        }

        .contact-actions {
            display: none;
            margin-left: 15px;
        }

        .contact-item:hover .contact-actions {
            display: block;
        }

        .error {
            color: red;
            font-size: 14px;
            margin-top: 5px;
        }

        .hidden {
            display: none;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Contacts Manager</h1>
    <h2>Add new Contact</h2>
    <div class="form-container">
        <h2>Add New Contact</h2>
        <form id="addContactForm">
            <div class="form-group">
                <label for="name">Name *</label>
                <input type="text" id="name" name="name" required>
            </div>

            <div class="form-group">
                <label for="surname">Surname</label>
                <input type="text" id="surname" name="surname">
            </div>

            <div class="form-group">
                <label for="phoneNumber">Phone Number *</label>
                <input type="text" id="phoneNumber" name="phoneNumber" required>
            </div>

            <button type="submit">Add Contact</button>
        </form>
        <div id="formError" class="error hidden"></div>
    </div>
    <h2>Contacts List</h2>
    <div id="contactList" class="contact-list">
        <!-- Contacts will be loaded here dynamically -->
    </div>
</div>

<script>
    let contacts = [];

    document.addEventListener('DOMContentLoaded', async function () {
        try {
            contacts = await loadContactsList();
            renderContacts(contacts)
            console.log("Contacts initialized.")
        } catch (error) {
            console.error("Error initializing contacts:", error.message);
            showError("Error initializing contacts:")
        }
    });

    async function loadContactsList() {
        const url = "/api/contacts";
        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                let errorDetails = `HTTP error! Status: ${response.status}`;

                try {
                    const errorBody = await response.text();
                    if (errorBody) {
                        errorDetails += `, Response Body: ${errorBody.substring(0, 150)}...`;
                    }
                } catch (e) {
                }

                console.error("Failed to fetch contacts list:", errorDetails);
                throw new Error(errorDetails);
            }

            return await response.json();
        } catch (error) {
            console.error("Critical network or parsing error:", error.message);
            throw error;
        }
    }

    function renderContacts(contactEntities) {
        const contactListElement = document.getElementById('contactList');

        if (contactEntities.length === 0) {
            contactListElement.innerHTML = '<p>No contacts found.</p>';
            return;
        }

        contactListElement.innerHTML = '';

        console.log(contactEntities)

        contactEntities.forEach(contact => {
            console.log("Name:", contact.name, "Surname:", contact.surname, "Phone:", contact.phoneNumber);

            console.log("Keys:", Object.keys(contact));
            console.log("Entry example:", contact);

            const id = contact.id;
            const name = contact.name;
            const surname = contact.surname;
            const phoneNumber = contact.phoneNumber;


            const contactElement = document.createElement('div');
            contactElement.className = 'contact-item';
            contactElement.id = id;

            const nameDiv = document.createElement("div");
            nameDiv.className = "name-field";
            nameDiv.contentEditable = "true";
            nameDiv.textContent = name;

            const surnameDiv = document.createElement("div");
            surnameDiv.className = "surname-field";
            surnameDiv.contentEditable = "true";
            surnameDiv.textContent = surname;

            const phoneDiv = document.createElement("div");
            phoneDiv.className = "phone-field";
            phoneDiv.contentEditable = "true";
            phoneDiv.textContent = phoneNumber;

            const info = document.createElement("div");
            info.className = "contact-info";
            info.appendChild(nameDiv);
            info.appendChild(surnameDiv);
            info.appendChild(phoneDiv);

            const actions = document.createElement("div");
            actions.className = "contact-actions";

            const updateBtn = document.createElement("button");
            updateBtn.className = "save-btn";
            updateBtn.textContent = "Update";
            updateBtn.addEventListener("click", () => updateContact(id));

            const deleteBtn = document.createElement("button");
            deleteBtn.className = "delete-btn";
            deleteBtn.textContent = "Delete";
            deleteBtn.addEventListener("click", () => deleteContact(id));

            actions.appendChild(updateBtn);
            actions.appendChild(deleteBtn);

            contactElement.appendChild(info);
            contactElement.appendChild(actions);

            contactListElement.appendChild(contactElement);
        });
    }

    function updateContact(id) {
        console.log("update called");
    }

    function deleteContact(id) {
        console.log("delete called");
    }

    function showError(message) {
        alert(message)
    }
</script>
</body>
</html>
