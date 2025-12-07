CREATE TABLE users
(
    id            SERIAL PRIMARY KEY,
    email         TEXT UNIQUE NOT NULL,
    password_hash TEXT        NOT NULL
);

CREATE TABLE mediafiles
(
    id       SERIAL PRIMARY KEY,
    fileName TEXT NOT NULL,
    hash     TEXT NOT NULL
);

CREATE TABLE contacts
(
    id           SERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users (id),
    name         TEXT   NOT NULL,
    surname      TEXT,
    phone_number TEXT   NOT NULL,
    mediafile_id BIGINT REFERENCES mediafiles (id),
    UNIQUE (user_id, phone_number),
    UNIQUE (mediafile_id)
);
