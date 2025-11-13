CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL
);

CREATE TABLE contacts (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name TEXT NOT NULL,
    surname TEXT,
    phone_number TEXT NOT NULL
);