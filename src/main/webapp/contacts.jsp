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
    </div>
    <h2>Contacts List</h2>
    <div id="contactList" class="contact-list">
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', async function () {
        await updateContacts();
        document.getElementById('addContactForm').addEventListener('submit', addContact);
    });

    async function updateContacts() {
        try {
            const contacts = await getAllContacts();
            renderContacts(contacts)
            console.log("Contacts updated.")
        } catch (error) {
            console.error("Error updating contacts:", error.message);
            showError("Error updating contacts" + error.message)
        }
    }

    async function getAllContacts() {
        const url = "/api/contacts";
        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                let errorDetails = 'GET error! Status: ' + response.status;

                try {
                    const errorBody = await response.text();
                    if (errorBody) {
                        errorDetails += ', Response Body: ' + errorBody;
                    }
                } catch (e) {
                }

                showError("Failed to fetch contacts list: ", errorDetails);
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

    async function updateContact(id) {
        const contactElement = document.getElementById(id);
        if (!contactElement) {
            showError('Contact element with ID' + id + ' not found.');
            return;
        }

        const nameElement = contactElement.querySelector('.name-field');
        const surnameElement = contactElement.querySelector('.surname-field');
        const phoneElement = contactElement.querySelector('.phone-field');

        const name = nameElement.textContent.trim();
        const surname = surnameElement.textContent.trim();
        const phoneNumber = phoneElement.textContent.trim();

        if (!name || !phoneNumber) {
            showError("Name and Phone Number are required fields.");
            return;
        }

        const formData = new URLSearchParams();
        formData.append('name', name);
        formData.append('surname', surname);
        formData.append('phoneNumber', phoneNumber);

        const url = '/api/contacts/' + id;

        try {
            const response = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString()
            });

            if (!response.ok) {
                const errorBody = await response.text();
                let errorDetails = 'Update failed! Status:' + response.status + '. Body: ' + errorBody;
                console.error("Update error:", errorDetails);
                showError(errorDetails);
                return;
            }

            console.log('Contact ID ' + id + ' updated successfully.');
        } catch (error) {
            console.error("Network error during update:", error);
            showError("A network error occurred while updating the contact.");
        }
    }

    async function deleteContact(id) {
        const contactElement = document.getElementById(id);
        if (!contactElement) {
            showError('Contact element with ID' + id + ' not found.');
            return;
        }

        const url = '/api/contacts/' + id;

        try {
            const response = await fetch(url, {
                method: 'DELETE'
            });

            if (!response.ok) {
                const errorBody = await response.text();
                let errorDetails = 'Delete failed! Status:' + response.status + '. Body: ' + errorBody;
                console.error("Delete error:", errorDetails);
                showError(errorDetails);
                return;
            }

            console.log('Contact ID ' + id + ' deleted successfully.');
        } catch (error) {
            console.error("Network error during delete:", error);
            showError("A network error occurred while deleting the contact.");
            return;
        }

        contactElement.remove();
    }

    async function addContact(event) {
        event.preventDefault();

        const nameElement = document.getElementById('name');
        const surnameElement = document.getElementById('surname');
        const phoneElement = document.getElementById('phoneNumber');

        const name = nameElement.value.trim();
        const surname = surnameElement.value.trim();
        const phoneNumber = phoneElement.value.trim();

        if (!name || !phoneNumber) {
            showError("Name and Phone Number are required.");
            return;
        }

        const formData = new URLSearchParams();
        formData.append('name', name);
        formData.append('surname', surname);
        formData.append('phoneNumber', phoneNumber);

        const url = "/api/contacts";

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString()
            });

            if (!response.ok) {
                const errorBody = await response.text();
                let errorDetails = 'Add failed! Status:' + response.status + '. Body: ' + errorBody;
                console.error("Add contact error:", errorDetails);
                showError(errorDetails);
                return;
            }

            console.log("Contact added successfully.");

            const updatedContactsList = await response.json();
            renderContacts(updatedContactsList);

            nameElement.value = '';
            surnameElement.value = '';
            phoneElement.value = '';
        } catch (error) {
            console.error("Network error during add contact:", error);
            showError("A network error occurred while adding the contact.");
        }
    }

    function showError(message) {
        alert(message)
    }
</script>
</body>
</html>
