#!/bin/bash
docker exec -i challenge-postgres psql -U admin -d solicitations_db -c "SELECT id, action, user_id, user_role, entity_id, timestamp FROM challenge.audit_logs ORDER BY timestamp DESC LIMIT 20;"
