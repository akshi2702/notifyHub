# High Level Design (HLD)

# NotifyHub – Event Driven Notification Platform

Version: 1.0

Author: Akshita Srivastava

Status: Draft

---

# 1. Purpose

## Objective

NotifyHub is a centralized notification platform that enables applications to send Email and SMS notifications through a scalable, asynchronous, event-driven architecture.

The platform decouples notification generation from notification delivery and provides a reusable notification service that can be consumed by multiple applications.

---

# 2. System Architecture

## Architecture Overview

NotifyHub follows an event-driven microservice architecture.

The platform consists of:

* Notification Service
* Kafka Message Broker
* Email Consumer
* SMS Consumer
* MySQL Database
* Template Cache
* External Notification Providers

### High Level Architecture Diagram
![NotifyHub Architecture](images/NotifyHub_System_Architecture.png)

### High Level Flow

1. Client submits notification request.
2. Notification Service validates request.
3. Request is persisted.
4. Notification templates are loaded from cache.
5. Notification messages are rendered.
6. Delivery records are created.
7. Delivery events are published to Kafka.
8. Channel-specific consumers process notifications.
9. Delivery status is updated.

---

# 3. Major Components

## 3.1 Notification Service

### Responsibilities

* Accept notification requests
* Validate incoming requests
* Enforce idempotency using requestId
* Retrieve notification templates
* Generate channel-specific messages
* Create delivery records
* Publish events to Kafka topics

### Technology

* Java 17
* Spring Boot

---

## 3.2 Template Cache

### Responsibilities

* Cache active notification templates
* Reduce database lookups
* Improve request processing performance

### Future Enhancement

* Redis Based Distributed Cache

---

## 3.3 Kafka Message Broker

### Responsibilities

* Decouple notification creation from delivery
* Support asynchronous processing
* Enable horizontal scalability
* Improve fault tolerance

### Topics

* email-topic
* sms-topic

---

## 3.4 Email Consumer

### Responsibilities

* Consume email delivery events
* Send email through SMTP provider
* Update delivery status

---

## 3.5 SMS Consumer

### Responsibilities

* Consume SMS delivery events
* Send SMS through SMS Gateway
* Update delivery status

---

## 3.6 MySQL Database

### Responsibilities

* Store notification requests
* Store notification templates
* Store notification delivery status

---

# 4. Technology Choices
- Java 17
- Spring Boot
- MySQL
- Apache Kafka
- Maven
- Git
- GitHub
- Docker
- Kubernetes (Future)
- Prometheus (Future)
- Grafana (Future)

# 5. Integration Points

## 5.1 Client Applications

### Purpose

Applications integrate with NotifyHub through REST APIs.

### Protocol

HTTP/HTTPS

### Endpoint

POST /v1/notifications

---

## 5.2 Kafka

### Purpose

Internal communication between Notification Service and channel consumers.

### Topics

* email-topic
* sms-topic

---

## 5.3 Email Provider

### Purpose

Actual email delivery.

### Examples

* Gmail SMTP
* Amazon SES
* SendGrid

Phase 1 implementation will use SMTP.

---

## 5.4 SMS Provider

### Purpose

Actual SMS delivery.

### Examples

* Twilio
* MSG91
* TextLocal

Phase 1 implementation may use a mock provider.

---

# 6. Deployment View

## Local Development Environment

### Components

* Notification-Service Spring Boot Application
* MySQL Database
* Kafka (KRaft Mode)
* Email Provider (SMTP)

### Deployment Model

Docker containers will be used for:

* Kafka (KRaft Mode)

MySQL may run locally or inside Docker.

---

## Future Production Deployment

### Platform

Kubernetes

### Deployable Units

* Notification Service Pods
* Email Consumer Pods
* SMS Consumer Pods
* Kafka Cluster
* MySQL Database

### Benefits

* Horizontal Scaling
* High Availability
* Fault Isolation
* Rolling Deployments

---

# 7. Scalability Strategy

## Horizontal Scaling

The following services can be scaled independently:

### Notification Service

Multiple instances can process incoming requests.

---

### Email Consumer

Multiple consumer instances can consume email-topic partitions.

---

### SMS Consumer

Multiple consumer instances can consume sms-topic partitions.

---

## Kafka Based Scalability

Kafka partitions allow parallel processing of notification events.

Benefits:

* Increased throughput
* Consumer group based load balancing
* Improved resilience

---

## Template Caching

Notification templates are cached to avoid database lookups on every request.

Benefits:

* Reduced latency
* Lower database load
* Higher throughput

---

# 8. Reliability Strategy

## Request Persistence

Notification requests are persisted before Kafka publication.

This prevents loss of incoming requests.

---

## Delivery Tracking

Each channel delivery is tracked independently.

Examples:

* Email Delivered
* SMS Failed

---

## Idempotency

Duplicate requests are prevented using requestId.

Benefits:

* No duplicate notifications
* Safe client retries

---

# 9. Security Considerations

Current Scope:

* Input Validation
* Request Validation

Future Enhancements:

* OAuth2
* JWT Authentication
* API Rate Limiting
* Secrets Management
* Encryption of Sensitive Data

---

# 10. Risks and Mitigation

## Duplicate Requests

**Risk**

The same notification request may be submitted multiple times due to client retries, network timeouts, or application errors.

**Mitigation**

Use requestId-based idempotency to ensure duplicate requests are processed only once.

---

## Email Provider Failure

**Risk**

The email provider may be temporarily unavailable, resulting in unsuccessful email delivery.

**Mitigation**

Track delivery status in the notification_delivery table and mark the delivery as FAILED.

---

## SMS Provider Failure

**Risk**

The SMS gateway may be unavailable or reject requests.

**Mitigation**

Track delivery status in the notification_delivery table and mark the delivery as FAILED.

---

## High Database Load

**Risk**

Frequent template lookups may increase database load and impact performance.

**Mitigation**

Use template caching to reduce database access and improve response times.

---

## High Traffic

**Risk**

A sudden increase in notification volume may impact throughput and processing time.

**Mitigation**

The architecture is designed to support horizontal scaling of Notification Service and channel consumers in future deployments. Kafka-based asynchronous processing helps decouple request ingestion from notification delivery.

---

# 11. Future Roadmap

## Phase 2

* Retry Mechanism
* Dead Letter Queue (DLQ)
* Redis Cache

## Phase 3

* Docker Deployment
* Kubernetes Deployment
* Prometheus Monitoring
* Grafana Dashboards

## Phase 4

* Push Notifications
* WhatsApp Notifications
* Multi-Tenancy
* Outbox Pattern
* Distributed Tracing

---

# 12. Conclusion

NotifyHub provides a scalable, event-driven notification platform capable of delivering notifications through multiple channels while maintaining delivery tracking, asynchronous processing, and extensibility for future notification channels.
