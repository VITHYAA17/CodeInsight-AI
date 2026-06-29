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

Coding_account table :

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

Statistics Table :

| Column          | Data Type    | Constraints             | Description                |
| --------------- | ------------ | ----------------------- | -------------------------- |
| id              | BIGSERIAL    | Primary Key             | Statistics ID              |
| user_id         | BIGINT       | Foreign Key → users(id) | Owner of the statistics    |
| total_solved    | INTEGER      | NOT NULL                | Total problems solved      |
| easy_solved     | INTEGER      | DEFAULT 0               | Easy problems solved       |
| medium_solved   | INTEGER      | DEFAULT 0               | Medium problems solved     |
| hard_solved     | INTEGER      | DEFAULT 0               | Hard problems solved       |
| acceptance_rate | DECIMAL(5,2) | NULL                    | Acceptance percentage      |
| contest_rating  | INTEGER      | NULL                    | Current contest rating     |
| current_streak  | INTEGER      | DEFAULT 0               | Current coding streak      |
| last_synced     | TIMESTAMP    | NOT NULL                | Last time data was fetched |

topic_scores Table
| Column          | Data Type    | Constraints             | Description                           |
| --------------- | ------------ | ----------------------- | ------------------------------------- |
| id              | BIGSERIAL    | Primary Key             | Topic record ID                       |
| user_id         | BIGINT       | Foreign Key → users(id) | Owner of the topic data               |
| topic_name      | VARCHAR(100) | NOT NULL                | Topic name (Arrays, Graphs, DP, etc.) |
| problems_solved | INTEGER      | DEFAULT 0               | Problems solved in this topic         |
| strength_score  | DECIMAL(5,2) | DEFAULT 0               | Calculated strength (0–100)           |
| last_updated    | TIMESTAMP    | NOT NULL                | Last update time                      |

ContentHistory Table

| Column          | Data Type    | Constraints             | Description                    |
| --------------- | ------------ | ----------------------- | ------------------------------ |
| id              | BIGSERIAL    | Primary Key             | Contest record ID              |
| user_id         | BIGINT       | Foreign Key → users(id) | Owner of the contest           |
| platform        | VARCHAR(50)  | NOT NULL                | LeetCode, Codeforces, CodeChef |
| contest_name    | VARCHAR(255) | NOT NULL                | Contest name                   |
| contest_date    | DATE         | NOT NULL                | Contest date                   |
| rating_before   | INTEGER      | NULL                    | Rating before contest          |
| rating_after    | INTEGER      | NULL                    | Rating after contest           |
| rank            | INTEGER      | NULL                    | Contest rank                   |
| problems_solved | INTEGER      | DEFAULT 0               | Problems solved in contest     |
