#!/bin/bash

# Load production environment variables from .env.prod
export $(grep -v '^#' .env.prod | grep -v '^$' | xargs)

# Run Spring Boot application with production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
