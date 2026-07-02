-- Criar extensões
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Criar tabela de usuários
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     password_hash VARCHAR(255) NOT NULL,
                                     role VARCHAR(20) NOT NULL,
                                     enabled BOOLEAN DEFAULT TRUE,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criar tabela de cobertura de analistas
CREATE TABLE IF NOT EXISTS analyst_coverage (
                                                id BIGSERIAL PRIMARY KEY,
                                                user_id BIGINT NOT NULL,
                                                state VARCHAR(2) NOT NULL,
                                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Criar tabela de solicitações
CREATE TABLE IF NOT EXISTS solicitations (
                                             id BIGSERIAL PRIMARY KEY,
                                             client_id BIGINT NOT NULL,
                                             status VARCHAR(20) NOT NULL,
                                             current_step INTEGER NOT NULL DEFAULT 0,
                                             service_type VARCHAR(20),
                                             title VARCHAR(80),
                                             description VARCHAR(1000),
                                             cep VARCHAR(8),
                                             street VARCHAR(100),
                                             number VARCHAR(20),
                                             complement VARCHAR(50),
                                             neighborhood VARCHAR(50),
                                             city VARCHAR(50),
                                             state VARCHAR(2),
                                             priority VARCHAR(10),
                                             preferred_date DATE,
                                             estimated_value DECIMAL(15,2),
                                             terms_accepted BOOLEAN,
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             submitted_at TIMESTAMP,
                                             analyzed_at TIMESTAMP,
                                             analyzed_by BIGINT,
                                             analysis_comment VARCHAR(1000),
                                             FOREIGN KEY (client_id) REFERENCES users(id),
                                             FOREIGN KEY (analyzed_by) REFERENCES users(id)
);

-- Criar tabela de auditoria (NOVA!)
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id BIGSERIAL PRIMARY KEY,
                                          action VARCHAR(100) NOT NULL,
                                          user_id VARCHAR(100) NOT NULL,
                                          user_role VARCHAR(50),
                                          entity_id BIGINT,
                                          duration_ms BIGINT,
                                          success BOOLEAN NOT NULL,
                                          error_message VARCHAR(1000),
                                          timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criar índices
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_solicitations_client_id ON solicitations(client_id);
CREATE INDEX IF NOT EXISTS idx_solicitations_status ON solicitations(status);
CREATE INDEX IF NOT EXISTS idx_solicitations_state ON solicitations(state);
CREATE INDEX IF NOT EXISTS idx_solicitations_created_at ON solicitations(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);

-- Inserir usuário ADMIN inicial (senha: Admin@123)
INSERT INTO users (name, email, password_hash, role, enabled, created_at)
VALUES (
           'Admin System',
           'admin@system.com',
           '$2a$10$N.K4hTBRsDxH.J5PgVasJujKzr9QuIMm7nK3sLmFQPPZ5lVWl5vKW',
           'ADMIN',
           true,
           CURRENT_TIMESTAMP
       ) ON CONFLICT (email) DO NOTHING;

-- Inserir analista de exemplo
INSERT INTO users (name, email, password_hash, role, enabled, created_at)
VALUES (
           'Analyst SP',
           'analyst.sp@system.com',
           '$2a$10$N.K4hTBRsDxH.J5PgVasJujKzr9QuIMm7nK3sLmFQPPZ5lVWl5vKW',
           'ANALYST',
           true,
           CURRENT_TIMESTAMP
       ) ON CONFLICT (email) DO NOTHING;

-- Inserir cobertura do analista
INSERT INTO analyst_coverage (user_id, state)
SELECT id, 'SP' FROM users WHERE email = 'analyst.sp@system.com'
ON CONFLICT DO NOTHING;

-- Inserir cliente de exemplo
INSERT INTO users (name, email, password_hash, role, enabled, created_at)
VALUES (
           'Test Client',
           'client@test.com',
           '$2a$10$N.K4hTBRsDxH.J5PgVasJujKzr9QuIMm7nK3sLmFQPPZ5lVWl5vKW',
           'CLIENT',
           true,
           CURRENT_TIMESTAMP
       ) ON CONFLICT (email) DO NOTHING;