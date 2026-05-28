# Inventory Service

## Overview

Manages product stock levels. Listens for order events on Kafka and decrements stock when an order is placed. Registers with Consul for service discovery.

## Tech Stack

- Java 21
- Spring Boot 3
- PostgreSQL
- Apache Kafka
- Spring Cloud Consul
