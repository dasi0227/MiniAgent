#!/usr/bin/env bash
set -euo pipefail
set -x

cd ~/MiniAgent
git pull

cd ~/MiniAgent/frontend
npm ci
npm run build

cd ~/MiniAgent/backend/docs/docker
docker compose -f docker-compose.yml down
docker compose -f docker-compose.yml up -d

cd ~/MiniAgent/backend/docs/mysql
docker exec -i mysql mysql -u root -pjason2004 ai-agent < table.sql
docker exec -i mysql mysql -u root -pjason2004 ai-agent < init.sql

cd ~/MiniAgent/backend/ai-agent-app/src/main/resources
nano .env

cd ~/MiniAgent/backend
mvn -DskipTests clean package

cd ~/MiniAgent/backend
java -jar ai-agent-app/target/*.jar --server.address=127.0.0.1 --server.port=8066
