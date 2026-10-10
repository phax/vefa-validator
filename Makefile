IMAGE=phelger/vefa-validator
tag=2.4.5

install:
	@mvn clean install

release:
	@mvn clean release:prepare release:perform

docker_build:
	docker buildx build --platform=linux/amd64,linux/arm64 --pull --progress plain --tag $(IMAGE):$(tag) --tag $(IMAGE):latest .

docker_push:
	docker buildx build --platform=linux/amd64,linux/arm64 --progress plain --tag $(IMAGE):$(tag) --tag $(IMAGE):latest --push .
