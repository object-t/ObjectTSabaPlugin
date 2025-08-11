.PHONY: init build up down

init:
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
	fi
	@export $$(grep -v '^#' .env | xargs) && echo "$$VELOCITY_SECRET" > docker/velocity/forwarding.secret

build: init
	./gradlew build
	mkdir -p docker/plugins
	cp build/libs/*.jar docker/plugins/

up: build
	docker compose --env-file .env up -d
	@echo "   Velocity Proxy: localhost:25577"
	@echo "   MySQL Database: localhost:3306"

reload: build
	docker compose restart lobby survival

down:
	docker compose down
