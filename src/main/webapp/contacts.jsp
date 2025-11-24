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
        <form id="contactForm">
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
    // Global variable to store all contacts
    let contacts = [];

    document.addEventListener('DOMContentLoaded', function () {
        loadContacts();

        document.getElementById('contactForm').addEventListener('submit', function (e) {
            e.preventDefault();
            addContact();
        });
    });

    function loadContacts() {
        fetch('/api/contacts/')
            .then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    throw new Error('Failed to load contacts');
                }
            })
            .then(data => {
                // Parse the response which contains one JSON object per line
                const lines = data.trim().split('\n').filter(line => line);
                contacts = lines.map(line => JSON.parse(line));

                renderContacts();
            })
            .catch(error => {
                console.error('Error loading contacts:', error);
                alert('Error loading contacts: ' + error.message);
            });
    }

    // Render contacts to the page
    function renderContacts() {
        const contactListElement = document.getElementById('contactList');

        if (contacts.length === 0) {
            contactListElement.innerHTML = '<p>No contacts found.</p>';
            return;
        }

        contactListElement.innerHTML = '';

        contacts.forEach(contact => {
            const contactElement = document.createElement('div');
            contactElement.className = 'contact-item';
            contactElement.dataset.id = contact.id;

            contactElement.innerHTML = `
                    <div class="contact-info">
                        <div class="name-field" contenteditable="true">${contact.name || ''}</div>
                        <div class="surname-field" contenteditable="true">${contact.surname || ''}</div>
                        <div class="phone-field" contenteditable="true">${contact.phoneNumber || ''}</div>
                    </div>
                    <div class="contact-actions">
                        <button class="save-btn" onclick="saveContact(${contact.id})">Save</button>
                        <button class="delete-btn" onclick="deleteContact(${contact.id})">Delete</button>
                    </div>
                `;

            contactListElement.appendChild(contactElement);
        });
    }

    // Add a new contact
    function addContact() {
        const name = document.getElementById('name').value.trim();
        const surname = document.getElementById('surname').value.trim();
        const phoneNumber = document.getElementById('phoneNumber').value.trim();

        // Form validation
        document.getElementById('formError').classList.add('hidden');

        if (!name) {
            showError('Name is required');
            return;
        }

        if (!phoneNumber) {
            showError('Phone number is required');
            return;
        }

        const newContact = {
            name: name,
            surname: surname,
            phoneNumber: phoneNumber
        };

        fetch('/api/contacts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `name=${encodeURIComponent(name)}&surname=${encodeURIComponent(surname)}&phoneNumber=${encodeURIComponent(phoneNumber)}`
        })
            .then(response => {
                if (response.ok) {
                    document.getElementById('contactForm').reset();
                    loadContacts(); // Reload contacts after successful addition
                } else {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
            })
            .catch(error => {
                console.error('Error adding contact:', error);
                showError('Error adding contact: ' + error.message);
            });
    }

    // Save an existing contact
    function saveContact(contactId) {
        const contactElement = document.querySelector(`.contact-item[data-id="${contactId}"]`);
        const name = contactElement.querySelector('.name-field').textContent.trim();
        const surname = contactElement.querySelector('.surname-field').textContent.trim();
        const phoneNumber = contactElement.querySelector('.phone-field').textContent.trim();

        if (!name) {
            alert('Name is required');
            return;
        }

        if (!phoneNumber) {
            alert('Phone number is required');
            return;
        }

        fetch('/api/contacts', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `id=${contactId}&name=${encodeURIComponent(name)}&surname=${encodeURIComponent(surname)}&phoneNumber=${encodeURIComponent(phoneNumber)}`
        })
            .then(response => {
                if (response.ok) {
                    loadContacts(); // Reload contacts after successful update
                } else {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
            })
            .catch(error => {
                console.error('Error updating contact:', error);
                alert('Error updating contact: ' + error.message);
            });
    }

    // Delete a contact
    function deleteContact(contactId) {
        if (!confirm('Are you sure you want to delete this contact?')) {
            return;
        }

        fetch('/api/contacts', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `id=${contactId}`
        })
            .then(response => {
                if (response.ok) {
                    loadContacts(); // Reload contacts after successful deletion
                } else {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
            })
            .catch(error => {
                console.error('Error deleting contact:', error);
                alert('Error deleting contact: ' + error.message);
            });
    }

    // Show error message
    function showError(message) {
        const errorElement = document.getElementById('formError');
        errorElement.textContent = message;
        errorElement.classList.remove('hidden');
    }
</script>
</body>
</html>
