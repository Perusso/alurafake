CREATE TABLE Task (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    createdAt datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    course_id BIGINT NOT NULL,
    statement VARCHAR(255) NOT NULL,
    `order` INT NOT NULL,
    options JSON DEFAULT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE,
    CONSTRAINT uk_task_course_statement UNIQUE (course_id, statement),
    CONSTRAINT uk_task_course_order UNIQUE (course_id, `order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;)