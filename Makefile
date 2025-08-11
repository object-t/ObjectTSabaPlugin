.PHONY: init build up down

init:
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
	fi
	@export $$(grep -v '^#' .env | xargs) && echo "$$VELOCITY_SECRET" > docker/velocity/forwarding.secret

build: init
	rm -rf build/libs
	./gradlew build
	rm -rf docker/{survival,lobby,}/plugins/ObjectTSabaPlugin-*.jar || true
	mkdir -p docker/plugins
	cp build/libs/*.jar docker/plugins/

up: build
	docker compose --env-file .env up -d
	@echo "   Velocity Proxy: localhost:25577"
	@echo "   MySQL Database: localhost:3306"

reload: build
	rm -rf docker/{survival,lobby}/plugins/ObjectTSabaPlugin-*.jar
	docker compose restart lobby survival

down:
	docker compose down

cmd:
	@read -p "> " cmd; \
	docker exec --user 1000 objecttsabaplugin-lobby-1 mc-send-to-console "$$cmd" && \
	docker exec --user 1000 objecttsabaplugin-survival-1 mc-send-to-console "$$cmd"
