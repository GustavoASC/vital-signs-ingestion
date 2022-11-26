FN_NAME ?=$

build-with-dependencies:
	rm -rf ./dist/dependencies
	rm  -f ./dist/function.zip
	pip3 install --target ./dist/dependencies -r $$FN_NAME/requirements.txt
	cd ./dist/dependencies && zip -r ../function.zip .
	cd $$FN_NAME && zip ../dist/function.zip *.py