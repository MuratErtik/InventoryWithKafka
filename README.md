# InventoryWithKafka

A microservices project with three Spring Boot services that communicate through **Apache Kafka**. The project includes a full CI/CD pipeline using **GitHub Actions**, **Helm**, and **ArgoCD**.

---

## Architecture Overview

```
                  ┌──────────────────┐
  POST /orders    │  Order Service   │──── sync HTTP call ───▶┌───────────────────┐
  ───────────────▶│    (port 8091)   │                        │ Inventory Service │
                  └────────┬─────────┘◀───── exists? ─────────│    (port 8093)    │
                           │                                  └───────────────────┘
                           │ publish
                           ▼
                  ┌──────────────────┐
                  │      Kafka       │
                  │ prod.orders.placed│
                  └────────┬─────────┘
                           │ consume
                           ▼
                  ┌──────────────────┐
                  │ Shipping Service │
                  │    (port 8092)   │
                  └────────┬─────────┘
                           │ publish
                           ▼
                  ┌──────────────────┐
                  │      Kafka       │
                  │prod.orders.shipped│
                  └────────┬─────────┘
                           │ consume
                           ▼
                  ┌──────────────────┐
                  │  Order Service   │
                  │  (updates status)│
                  └──────────────────┘

         All services share a PostgreSQL database
```

---

## Main Logic

### How an Order Flows Through the System

1. **A user places an order** by sending a `POST /orders` request to the **Order Service** with a product ID and price.

2. **Inventory check** — The Order Service calls the **Inventory Service** over HTTP (using OpenFeign) to verify the product exists.

3. **Order saved** — If the product exists, the order is saved to **PostgreSQL** with an initial status.

4. **Kafka event published** — The Order Service publishes an `OrderPlacedEvent` to the `prod.orders.placed` Kafka topic.

5. **Shipping picks it up** — The **Shipping Service** listens on that topic, creates a shipping record in the database, and publishes the order ID to the `prod.orders.shipped` topic.

6. **Order status updated** — The Order Service listens on `prod.orders.shipped` and updates the order status accordingly.

### Services at a Glance

| Service   | Port | Role                                             |
|-----------|------|--------------------------------------------------|
| Order     | 8091 | Accepts orders, checks inventory, publishes events |
| Shipping  | 8092 | Listens for orders, creates shipments, confirms   |
| Inventory | 8093 | Provides product availability lookups              |

### Tech Stack

- **Java 21** with **Spring Boot**
- **Spring Kafka** for event-driven messaging
- **Spring Data JPA** + **PostgreSQL** for persistence
- **Spring Cloud OpenFeign** for synchronous service-to-service calls
- **Apache Kafka** as the message broker

---

## CI/CD Pipeline

### Overview

```
  Push to main
       │
       ▼
  GitHub Actions          ArgoCD (GitOps)
  ┌────────────┐         ┌──────────────────┐
  │ Build JARs │         │ Watches repo for  │
  │ Build Docker│────────▶│ Helm chart changes│
  │ Push to GHCR│         │ Syncs to cluster  │
  └────────────┘         └──────────────────┘
```

### Step 1 — GitHub Actions (CI)

A push to the `main` branch triggers the workflow in `.github/workflows/build.yaml`. It uses a **matrix strategy** to build all three services in parallel:

1. **Checkout** the code
2. **Set up Java 21** (Amazon Corretto)
3. **Build** the Spring Boot JAR with `./gradlew bootJar` and rename it to `app.jar`
4. **Log in** to GitHub Container Registry (GHCR)
5. **Build and push** a Docker image for each service to `ghcr.io/muratertik/<service>:main`

### Step 2 — Docker Images

Each service has the same slim Dockerfile:

- Base image: `amazoncorretto:21-alpine`
- Copies the pre-built `app.jar` into the container
- Runs it with `java -jar app.jar`

### Step 3 — ArgoCD (CD)

ArgoCD watches this repository and automatically syncs Kubernetes resources:

- An **AppProject** scopes the deployment to the `argocd` namespace
- Five **Applications** are defined — one for each service (order, shipping, inventory) plus Kafka and PostgreSQL
- Each microservice is deployed using a **Helm chart** (under `charts/`) that defines Deployments, Services, health probes, and environment variables
- Kafka and PostgreSQL are deployed using plain manifests under `charts/kafka/` and `charts/postgres/`
- Auto-sync with **pruning** is enabled, so removed resources are cleaned up automatically

### Secrets Management

Database credentials are pulled from **HashiCorp Vault** using the **External Secrets Operator**:

- A `ClusterSecretStore` connects to Vault at `http://vault.vault.svc.cluster.local:8200`
- An `ExternalSecret` fetches the database username and password from Vault's `cubbyhole/database` path and creates a Kubernetes secret called `database`
- The Helm charts reference this secret for the `SPRING_DATASOURCE_PASSWORD` environment variable

---


## Project Structure

```
├── .github/workflows/build.yaml   # CI pipeline (GitHub Actions)
├── argocd/manifests/              # ArgoCD Application and Project definitions
├── charts/                        # Helm charts for Kubernetes deployment
│   ├── inventory/
│   ├── order/
│   ├── shipping/
│   ├── kafka/
│   └── postgres/
├── inventory/                     # Inventory microservice (Spring Boot)
├── order/                         # Order microservice (Spring Boot)
├── shipping/                      # Shipping microservice (Spring Boot)
├── docker-compose.yml             # Local dev infrastructure
├── external-secret.yaml           # ExternalSecret for Vault integration
└── secret-store.yaml              # ClusterSecretStore for Vault
```
