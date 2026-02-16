CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone_number VARCHAR(20),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role UUID NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    account_status INT DEFAULT 1,
    account_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    last_login_at TIMESTAMP,
    logo_path TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID,
    permission_id UUID
);

CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    project_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_by UUID NOT NULL,
    owner UUID,
    isActive BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_count (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project UUID NOT NULL UNIQUE,
    ticket_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_project_count_project
        FOREIGN KEY (project) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS project_member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project UUID NOT NULL,
    member UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_project_member_project
        FOREIGN KEY (project) REFERENCES projects(id) ON DELETE CASCADE,

    CONSTRAINT fk_project_member_user
        FOREIGN KEY (member) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT unique_project_member UNIQUE (project, member)
);

CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    ticket_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    --is_base BOOLEAN GENERATED ALWAYS AS (parent_ticket IS NULL) STORED,
    parent_ticket UUID,
    created_by UUID NOT NULL,
    assigned_to UUID,
    assigned_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- OPTIONAL but recommended indexes:
    CONSTRAINT fk_ticket_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,

    CONSTRAINT fk_ticket_parent
        FOREIGN KEY (parent_ticket) REFERENCES tickets(id) ON DELETE CASCADE,

    CONSTRAINT fk_ticket_created_by
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_ticket_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_ticket_assigned_by
        FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE CASCADE
);


