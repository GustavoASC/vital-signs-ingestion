FN_NAME ?=$

start-pc:
	./scripts/start-pc.sh
	make collect-cpu

start-note:
	@sudo cat /var/lib/faasd/secrets/basic-auth-password | faas-cli login --password-stdin
	@docker login

build-and-deployfn:
	cd functions && faas-cli up -f body-temperature-monitor.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f duration-offloading.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f predictor.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f ranking-offloading.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f service-executor.yml --build-arg TEST_ENABLED=false -e THRESHOLD_CRITICAL_CPU_USAGE=$$THRESHOLD_CRITICAL_CPU_USAGE -e THRESHOLD_WARNING_CPU_USAGE=$$THRESHOLD_WARNING_CPU_USAGE
	cd functions && faas-cli up -f topology-mapping.yml --build-arg TEST_ENABLED=false -e ALIAS_CURRENT_MACHINE=$$ALIAS_CURRENT_MACHINE

deployfn:
	cd functions && faas-cli deploy -f body-temperature-monitor.yml
	cd functions && faas-cli deploy -f duration-offloading.yml
	cd functions && faas-cli deploy -f predictor.yml
	cd functions && faas-cli deploy -f ranking-offloading.yml
	cd functions && faas-cli deploy -f service-executor.yml -e THRESHOLD_CRITICAL_CPU_USAGE=$$THRESHOLD_CRITICAL_CPU_USAGE -e THRESHOLD_WARNING_CPU_USAGE=$$THRESHOLD_WARNING_CPU_USAGE
	cd functions && faas-cli deploy -f topology-mapping.yml -e ALIAS_CURRENT_MACHINE=$$ALIAS_CURRENT_MACHINE

run:
	@./mvnw -Dquarkus.http.port=8097 quarkus:dev &

collect-cpu:
	@python3 scripts/cpu-provider/cpu-provider.py &

eval:
	@python3 scripts/evaluation.py &

logs:
	@journalctl -t openfaas-fn:$$FN_NAME -f