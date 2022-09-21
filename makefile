start-pc:
	@PASSWORD=$(kubectl get secret -n openfaas basic-auth -o jsonpath="{.data.basic-auth-password}" | base64 --decode; echo)
	@echo -n $PASSWORD | faas-cli login --username admin --password-stdin
	@docker login
	@kubectl rollout status -n openfaas deploy/gateway
	@kubectl port-forward -n openfaas svc/gateway 8080:8080 &

start-note:
	@sudo cat /var/lib/faasd/secrets/basic-auth-password | faas-cli login --password-stdin
	@docker login

deployfn:
	cd functions && faas-cli up -f body-temperature-monitor.yml --build-arg TEST_ENABLED=false
	cd functions && faas-cli up -f predictor.yml --build-arg TEST_ENABLED=false