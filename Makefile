IMAGE=phelger/vefa-validator
tag=2.4.1

package:
	@mvn clean package

release:
	@mvn clean release:prepare release:perform

docker_build:
	@DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform=linux/amd64,linux/arm64 --progress plain --tag $(IMAGE):$(tag) .

docker_push:
	@DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform=linux/amd64,linux/arm64 --progress plain --tag $(IMAGE):$(tag) --tag $(IMAGE):latest --push .
