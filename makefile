start-pc:
	./scripts/start-pc.sh
	make collect-cpu

start-note:
	@sudo cat /var/lib/faasd/secrets/basic-auth-password | faas-cli login --password-stdin
	@docker login

deployfn:
	cd functions && faas-cli up -f body-temperature-monitor.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f predictor.yml --build-arg TEST_ENABLED=false

run:
	@./mvnw -Dquarkus.http.port=8097 quarkus:dev &

collect-cpu:
	@python3 scripts/collect-machine-resources-usage.py &

eval:
	@python3 scripts/evaluation.py &
