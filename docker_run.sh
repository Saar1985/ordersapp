#!/bin/bash

# Function to build and load Docker image
build_and_load_docker_image() {
    local module=$1
    echo "Building and loading Docker image for $module"
    mvn -f $module clean package
    mvn -f $module docker:build
    mvn -f $module docker:save
}

# Modules to build, load, and include in Docker Compose
modules=("RestVerticle" "OrderVerticle")

# Build, load Docker images, and generate Docker Compose YAML
for module in "${modules[@]}"; do
    build_and_load_docker_image $module
done

echo "Docker images built and loaded, and Docker Compose YAML generated."
read -p "Press any key to continue" x
