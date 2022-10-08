FN_NAME ?=$

start-pc:
	./scripts/start-pc.sh
	make collect-cpu

start-note:
	@sudo cat /var/lib/faasd/secrets/basic-auth-password | faas-cli login --password-stdin
	@docker login

build-and-deployfn:
	cd functions && faas-cli up -f body-temperature-monitor.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f cpu-provider.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f duration-offloading.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f predictor.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f ranking-offloading.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f service-executor.yml --build-arg TEST_ENABLED=false

deployfn:
	cd functions && faas-cli deploy -f body-temperature-monitor.yml
	cd functions && faas-cli deploy -f cpu-provider.yml
	cd functions && faas-cli deploy -f duration-offloading.yml
	cd functions && faas-cli deploy -f predictor.yml
	cd functions && faas-cli deploy -f ranking-offloading.yml
	cd functions && faas-cli deploy -f service-executor.yml

run:
	@./mvnw -Dquarkus.http.port=8097 quarkus:dev &

collect-cpu:
	@python3 scripts/collect-machine-resources-usage.py &

eval:
	@python3 scripts/evaluation.py &

logs:
	@journalctl -t openfaas-fn:$$FN_NAME