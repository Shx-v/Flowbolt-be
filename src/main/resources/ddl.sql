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

CREATE TABLE IF NOT EXISTS project_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_project_member_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,

    CONSTRAINT fk_project_member_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT uk_project_members_project_user
        UNIQUE (project_id, user_id)
);

CREATE TABLE IF NOT EXISTS project_member_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_member_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pmp_project_member
        FOREIGN KEY (project_member_id)
        REFERENCES project_members(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pmp_permission
        FOREIGN KEY (permission_id)
        REFERENCES project_permissions(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_pmp_member_permission
        UNIQUE (project_member_id, permission_id)
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
    completed_at TIMESTAMP,
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

//PREFILL

INSERT INTO ticket_statuses (key, description, terminal)
VALUES
('OPEN', 'Ticket has been created and is awaiting action.', FALSE),
('ACKNOWLEDGED', 'Ticket has been acknowledged by the responsible team.', FALSE),
('INVESTIGATING', 'Issue is currently under investigation.', FALSE),
('ANALYSIS', 'Ticket is under technical or business analysis.', FALSE),
('IN PROGRESS', 'Work has started on resolving the ticket.', FALSE),
('REVIEW', 'Work is completed and pending review.', FALSE),
('APPROVAL PENDING', 'Awaiting approval before further action.', FALSE),
('APPROVED', 'Ticket has been approved for further processing or deployment.', FALSE),
('ON HOLD', 'Work on the ticket is temporarily paused.', FALSE),
('BLOCKED', 'Progress is blocked due to external dependency.', FALSE),

('RESOLVED', 'Issue has been resolved successfully.', TRUE),
('MITIGATED', 'Issue impact has been reduced or controlled.', TRUE),
('REJECTED', 'Ticket has been rejected after evaluation.', TRUE),
('WONT FIX', 'Issue will not be fixed as per decision.', TRUE),
('CANCELLED', 'Ticket has been cancelled before completion.', TRUE),
('DUPLICATE', 'Ticket is a duplicate of another existing ticket.', TRUE),
('CLOSED', 'Ticket is fully closed and no further action will be taken.', TRUE);

INSERT INTO ticket_types (key, description)
VALUES
('SUPPORT', 'General support request requiring assistance or clarification.'),
('TASK', 'A defined unit of work that needs to be completed.'),
('IMPROVEMENT', 'Enhancement to an existing feature or process.'),
('SPIKE', 'Time-boxed research or investigation to reduce uncertainty.'),
('CHANGE REQUEST', 'Formal request to modify an existing system or requirement.'),
('FEATURE', 'New functionality to be developed and delivered.'),
('INCIDENT', 'Unplanned interruption or degradation of service.'),
('BUG', 'Defect causing incorrect or unexpected behavior in the system.');

INSERT INTO ticket_type_status_mapping (ticket_type_id, ticket_status_id)
SELECT tt.id, ts.id
FROM (
    VALUES
    -- SPIKE
    ('SPIKE','OPEN'),
    ('SPIKE','CLOSED'),
    ('SPIKE','CANCELLED'),
    ('SPIKE','ANALYSIS'),

    -- CHANGE REQUEST
    ('CHANGE REQUEST','OPEN'),
    ('CHANGE REQUEST','IN PROGRESS'),
    ('CHANGE REQUEST','REVIEW'),
    ('CHANGE REQUEST','RESOLVED'),
    ('CHANGE REQUEST','CLOSED'),
    ('CHANGE REQUEST','CANCELLED'),
    ('CHANGE REQUEST','REJECTED'),
    ('CHANGE REQUEST','ANALYSIS'),
    ('CHANGE REQUEST','APPROVAL PENDING'),
    ('CHANGE REQUEST','APPROVED'),

    -- FEATURE
    ('FEATURE','OPEN'),
    ('FEATURE','IN PROGRESS'),
    ('FEATURE','REVIEW'),
    ('FEATURE','RESOLVED'),
    ('FEATURE','CLOSED'),
    ('FEATURE','CANCELLED'),
    ('FEATURE','WONT FIX'),
    ('FEATURE','ANALYSIS'),
    ('FEATURE','APPROVED'),

    -- IMPROVEMENT
    ('IMPROVEMENT','OPEN'),
    ('IMPROVEMENT','IN PROGRESS'),
    ('IMPROVEMENT','REVIEW'),
    ('IMPROVEMENT','RESOLVED'),
    ('IMPROVEMENT','CLOSED'),
    ('IMPROVEMENT','CANCELLED'),
    ('IMPROVEMENT','WONT FIX'),
    ('IMPROVEMENT','ANALYSIS'),

    -- TASK
    ('TASK','OPEN'),
    ('TASK','IN PROGRESS'),
    ('TASK','BLOCKED'),
    ('TASK','REVIEW'),
    ('TASK','CLOSED'),
    ('TASK','CANCELLED'),

    -- BUG
    ('BUG','OPEN'),
    ('BUG','IN PROGRESS'),
    ('BUG','BLOCKED'),
    ('BUG','REVIEW'),
    ('BUG','RESOLVED'),
    ('BUG','CLOSED'),
    ('BUG','CANCELLED'),
    ('BUG','DUPLICATE'),
    ('BUG','WONT FIX'),

    -- SUPPORT
    ('SUPPORT','OPEN'),
    ('SUPPORT','IN PROGRESS'),
    ('SUPPORT','ON HOLD'),
    ('SUPPORT','RESOLVED'),
    ('SUPPORT','CLOSED'),
    ('SUPPORT','CANCELLED'),

    -- INCIDENT
    ('INCIDENT','OPEN'),
    ('INCIDENT','RESOLVED'),
    ('INCIDENT','CLOSED'),
    ('INCIDENT','CANCELLED'),
    ('INCIDENT','ACKNOWLEDGED'),
    ('INCIDENT','INVESTIGATING'),
    ('INCIDENT','MITIGATED')

) AS mapping(type_key, status_key)
JOIN ticket_types tt ON tt.key = mapping.type_key
JOIN ticket_statuses ts ON ts.key = mapping.status_key
ON CONFLICT (ticket_type_id, ticket_status_id) DO NOTHING;

INSERT INTO ticket_status_transitions (
    ticket_type_id,
    from_status_id,
    to_status_id
)
SELECT
    tt.id,
    fs.id,
    ts.id
FROM (
    VALUES
    -- BUG
    ('BUG','BLOCKED','IN PROGRESS'),
    ('BUG','IN PROGRESS','REVIEW'),
    ('BUG','IN PROGRESS','CANCELLED'),
    ('BUG','IN PROGRESS','BLOCKED'),
    ('BUG','OPEN','IN PROGRESS'),
    ('BUG','OPEN','WONT FIX'),
    ('BUG','OPEN','DUPLICATE'),
    ('BUG','RESOLVED','CLOSED'),
    ('BUG','REVIEW','RESOLVED'),

    -- CHANGE REQUEST
    ('CHANGE REQUEST','ANALYSIS','APPROVAL PENDING'),
    ('CHANGE REQUEST','APPROVAL PENDING','APPROVED'),
    ('CHANGE REQUEST','APPROVED','IN PROGRESS'),
    ('CHANGE REQUEST','IN PROGRESS','REVIEW'),
    ('CHANGE REQUEST','OPEN','ANALYSIS'),
    ('CHANGE REQUEST','RESOLVED','CLOSED'),
    ('CHANGE REQUEST','REVIEW','RESOLVED'),

    -- FEATURE
    ('FEATURE','ANALYSIS','APPROVAL PENDING'),
    ('FEATURE','APPROVAL PENDING','APPROVED'),
    ('FEATURE','APPROVED','IN PROGRESS'),
    ('FEATURE','IN PROGRESS','REVIEW'),
    ('FEATURE','OPEN','ANALYSIS'),
    ('FEATURE','RESOLVED','CLOSED'),
    ('FEATURE','REVIEW','RESOLVED'),

    -- IMPROVEMENT
    ('IMPROVEMENT','ANALYSIS','APPROVAL PENDING'),
    ('IMPROVEMENT','APPROVAL PENDING','APPROVED'),
    ('IMPROVEMENT','APPROVED','IN PROGRESS'),
    ('IMPROVEMENT','IN PROGRESS','REVIEW'),
    ('IMPROVEMENT','OPEN','ANALYSIS'),
    ('IMPROVEMENT','RESOLVED','CLOSED'),
    ('IMPROVEMENT','REVIEW','RESOLVED'),

    -- INCIDENT
    ('INCIDENT','OPEN','ACKNOWLEDGED'),
    ('INCIDENT','ACKNOWLEDGED','INVESTIGATING'),
    ('INCIDENT','INVESTIGATING','MITIGATED'),
    ('INCIDENT','MITIGATED','RESOLVED'),
    ('INCIDENT','RESOLVED','CLOSED'),

    -- SPIKE
    ('SPIKE','OPEN','ANALYSIS'),
    ('SPIKE','ANALYSIS','CLOSED'),
    ('SPIKE','ANALYSIS','CANCELLED'),

    -- SUPPORT
    ('SUPPORT','OPEN','IN PROGRESS'),
    ('SUPPORT','IN PROGRESS','RESOLVED'),
    ('SUPPORT','IN PROGRESS','ON HOLD'),
    ('SUPPORT','ON HOLD','IN PROGRESS'),
    ('SUPPORT','RESOLVED','CLOSED'),

    -- TASK
    ('TASK','OPEN','IN PROGRESS'),
    ('TASK','IN PROGRESS','BLOCKED'),
    ('TASK','BLOCKED','IN PROGRESS'),
    ('TASK','IN PROGRESS','CLOSED')

) AS t(type_key, from_key, to_key)
JOIN ticket_types tt ON tt.key = t.type_key
JOIN ticket_statuses fs ON fs.key = t.from_key
JOIN ticket_statuses ts ON ts.key = t.to_key
ON CONFLICT (ticket_type_id, from_status_id, to_status_id) DO NOTHING;

