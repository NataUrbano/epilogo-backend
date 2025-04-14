-- Insertar roles
INSERT INTO roles (id_rol, name)
SELECT 1, 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id_rol = 1);

INSERT INTO roles (id_rol, name)
SELECT 2, 'USER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id_rol = 2);

INSERT INTO roles (id_rol, name)
SELECT 3, 'GUEST'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id_rol = 3);

-- Insertar usuarios solo si no hay registros
INSERT INTO users (user_name, email, password)
SELECT 'AdminUser', 'admin@example.com', 'admin123'
WHERE NOT EXISTS (SELECT 1 FROM users);

INSERT INTO users (user_name, email, password)
SELECT 'RegularUser', 'user@example.com', 'user123'
WHERE NOT EXISTS (SELECT 1 FROM users);

INSERT INTO users (user_name, email, password)
SELECT 'GuestUser', 'guest@example.com', 'guest123'
WHERE NOT EXISTS (SELECT 1 FROM users);

INSERT INTO users (user_name, email, password)
SELECT 'AdminAssistant', 'admin2@example.com', 'admin456'
WHERE NOT EXISTS (SELECT 1 FROM users);

INSERT INTO users (user_name, email, password)
SELECT 'GuestAssistant', 'guest2@example.com', 'guest456'
WHERE NOT EXISTS (SELECT 1 FROM users);

-- Asignar roles a usuarios manualmente si existen usuarios
INSERT INTO user_rol (id_user, id_rol) VALUES
                                           (1, 1),  -- AdminUser -> ADMIN
                                           (2, 2),  -- RegularUser -> USER
                                           (3, 3),  -- GuestUser -> GUEST
                                           (4, 1),  -- AdminAssistant -> ADMIN
                                           (5, 3);  -- GuestAssistant -> GUEST

ALTER SEQUENCE users_id_user_seq RESTART WITH 6;