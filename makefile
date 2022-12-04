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

collect-results:
	@python3.8 scripts/results/results.py &

collect-cpu:
	@python3.8 scripts/cpu-provider/cpu-provider.py &
	@python3.8 scripts/metrics/metrics.py &

listen-test-executor:
	cd scripts/test-executor && python3.8 test_executor.py &

eval:
	@python3.8 scripts/evaluation.py &

logs:
	@journalctl -t openfaas-fn:$$FN_NAME -f

install-software-fog-node:
	sudo yum update
	sudo yum install git
	git clone https://github.com/openfaas/faasd
	cd faasd
	./hack/install.sh
	cd ..
	sudo vim /var/lib/faasd/secrets/basic-auth-password
	# Paste a secret that will be the same on all fog nodes
	git clone https://github.com/GustavoASC/vital-signs-ingestion
	cd vital-signs-ingestion
	sudo amazon-linux-extras install python3.8
	pip3.8 install psutil
	sudo yum install docker
	sudo usermod -a -G docker ec2-user
	id ec2-user
	newgrp docker
	sudo systemctl enable docker.service
	sudo systemctl start docker.service
	crontab -e
	# Paste this: @reboot /home/ec2-user/vital-signs-ingestion/startup-fog-node.sh

install-software-edge-node:
	sudo yum update
	sudo yum install git
	sudo amazon-linux-extras install python3.8
	git clone https://github.com/GustavoASC/vital-signs-ingestion
	crontab -e
	# Paste this: @reboot /home/ec2-user/vital-signs-ingestion/startup-edge-node.sh

install-software-results-node:
	sudo yum update
	sudo yum install git
	sudo amazon-linux-extras install python3.8
	git clone https://github.com/GustavoASC/vital-signs-ingestion
	crontab -e
	# Paste this: @reboot /home/ec2-user/vital-signs-ingestion/startup-results-node.sh
