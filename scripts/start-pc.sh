	pkill kubectl -9
	docker login
	kubectl rollout status -n openfaas deploy/gateway
	kubectl port-forward -n openfaas svc/gateway 8080:8080 &
	sleep 4
	PASSWORD=$(kubectl get secret -n openfaas basic-auth -o jsonpath="{.data.basic-auth-password}" | base64 --decode; echo)
	echo -n $PASSWORD | faas-cli login --username admin --password-stdin
