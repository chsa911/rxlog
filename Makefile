up:
	docker compose up --build -d
down:
	docker compose down -v
logs:
	docker compose logs -f --tail=120
