login:
	@sudo cat /var/lib/faasd/secrets/basic-auth-password | faas-cli login --password-stdin
	@docker login

deployfn:
	cd functions && faas-cli up -f body-temperature-monitor.yml