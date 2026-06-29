# CodeInsight AI Database Design
| Column Name | Data Type    | Constraints      | Description           |
| ----------- | ------------ | ---------------- | --------------------- |
| id          | BIGSERIAL    | Primary Key      | Unique user ID        |
| name        | VARCHAR(100) | NOT NULL         | User's full name      |
| email       | VARCHAR(100) | UNIQUE, NOT NULL | User login email      |
| password    | VARCHAR(255) | NOT NULL         | Encrypted password    |
| created_at  | TIMESTAMP    | NOT NULL         | Account creation time |
| updated_at  | TIMESTAMP    | NULL             | Last profile update   |


Primary Key : id

Unique Key : email

Passwords are never stored in plain text. Spring Security will hash them using BCrypt before saving them to the database, so the column must be long enough to store the hashed value.

Coding_account table

| Column Name         | Data Type    | Constraints                       | Description                    |
| ------------------- | ------------ | --------------------------------- | ------------------------------ |
| id                  | BIGSERIAL    | Primary Key                       | Unique record ID               |
| user_id             | BIGINT       | Foreign Key → users(id), NOT NULL | Owner of these coding accounts |
| leetcode_username   | VARCHAR(100) | NULL                              | LeetCode username              |
| codeforces_username | VARCHAR(100) | NULL                              | Codeforces username            |
| codechef_username   | VARCHAR(100) | NULL                              | CodeChef username              |
| gfg_username        | VARCHAR(100) | NULL                              | GeeksforGeeks username         |
| github_username     | VARCHAR(100) | NULL                              | GitHub username                |
| created_at          | TIMESTAMP    | NOT NULL                          | Record creation time           |
| updated_at          | TIMESTAMP    | NULL                              | Last updated time              |


