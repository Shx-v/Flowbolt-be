CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role UUID NOT NULL,
    CONSTRAINT fk_users_role
        FOREIGN KEY (role)
        REFERENCES roles(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
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
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS global_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_global_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_rgp_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_rgp_permission
        FOREIGN KEY (permission_id)
        REFERENCES global_permissions(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    project_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_by UUID NOT NULL,
    owner UUID,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_projects_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    leader UUID NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_groups_leader
        FOREIGN KEY (leader)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_groups_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS group_member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    member_id UUID NOT NULL,
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_group_member_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_group_member_member
        FOREIGN KEY (member_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_group_member_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT uq_group_member_unique
        UNIQUE (group_id, member_id)
);

CREATE TABLE IF NOT EXISTS project_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_user_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_project_user_member_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,

    CONSTRAINT fk_project_user_member_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT uk_project_user_members_project_user
        UNIQUE (project_id, user_id)
);

CREATE TABLE IF NOT EXISTS project_group_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    group_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_project_group_member_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,

    CONSTRAINT fk_project_group_member_group
        FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,

    CONSTRAINT uk_project_group_members_project_group
        UNIQUE (project_id, group_id)
);

CREATE TABLE IF NOT EXISTS project_user_member_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_user_member_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pump_project_user_member
        FOREIGN KEY (project_user_member_id)
        REFERENCES project_user_members(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pump_permission
        FOREIGN KEY (permission_id)
        REFERENCES project_permissions(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_pump_member_permission
        UNIQUE (project_user_member_id, permission_id)
);

CREATE TABLE IF NOT EXISTS project_group_member_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_group_member_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pgmp_project_group_member
        FOREIGN KEY (project_group_member_id)
        REFERENCES project_group_members(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pgmp_permission
        FOREIGN KEY (permission_id)
        REFERENCES project_permissions(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_pgmp_member_permission
        UNIQUE (project_group_member_id, permission_id)
);

CREATE TABLE IF NOT EXISTS project_group_permission_delegations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_group_member_permission_id UUID NOT NULL,
    delegated_to_user_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pgpd_group_permission
        FOREIGN KEY (project_group_member_permission_id)
        REFERENCES project_group_member_permissions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pgpd_user
        FOREIGN KEY (delegated_to_user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_pgpd_unique
        UNIQUE (project_group_member_permission_id, delegated_to_user_id)
);

CREATE TABLE IF NOT EXISTS ticket_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ticket_statuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    terminal BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ticket_type_status_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_type_id UUID NOT NULL,
    ticket_status_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_ticket_type_status UNIQUE (ticket_type_id, ticket_status_id),

    CONSTRAINT fk_ttsm_ticket_type
        FOREIGN KEY (ticket_type_id)
        REFERENCES ticket_types(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ttsm_ticket_status
        FOREIGN KEY (ticket_status_id)
        REFERENCES ticket_statuses(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ticket_status_transitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_type_id UUID NOT NULL,
    from_status_id UUID NOT NULL,
    to_status_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_ticket_status_transition UNIQUE (
        ticket_type_id,
        from_status_id,
        to_status_id
    ),

    CONSTRAINT fk_tst_ticket_type
        FOREIGN KEY (ticket_type_id)
        REFERENCES ticket_types(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tst_from_status
        FOREIGN KEY (from_status_id)
        REFERENCES ticket_statuses(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tst_to_status
        FOREIGN KEY (to_status_id)
        REFERENCES ticket_statuses(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    ticket_number INTEGER NOT NULL,
    status UUID NOT NULL,
    priority VARCHAR(20) NOT NULL,
    type UUID NOT NULL,
    parent_ticket UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deadline TIMESTAMP NOT NULL,
    created_by UUID NOT NULL,
    assigned_to UUID,
    assigned_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_ticket_project_number UNIQUE (project, ticket_number),

    CONSTRAINT fk_ticket_project
        FOREIGN KEY (project)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ticket_status
        FOREIGN KEY (status)
        REFERENCES ticket_statuses(id),

    CONSTRAINT fk_ticket_type
        FOREIGN KEY (type)
        REFERENCES ticket_types(id),

    CONSTRAINT fk_ticket_parent
        FOREIGN KEY (parent_ticket)
        REFERENCES tickets(id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ticket_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    content VARCHAR NOT NULL,
    created_by UUID NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ticket_comments_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets(id)
);


//OLD
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


