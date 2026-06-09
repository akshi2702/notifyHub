# Software Requirements Specification (SRS)

# NotifyHub – Event Driven Notification Platform

Version: 1.0

Author: Akshita Srivastava

Status: Draft

---

# 1. Introduction

## 1.1 Purpose

NotifyHub is a centralized, event-driven notification platform that enables applications to send notifications through multiple communication channels such as Email and SMS.

The platform provides:

* Template-driven notification generation
* Multi-channel notification support
* Asynchronous processing using Kafka
* Notification delivery tracking
* Idempotent request processing
* Extensible architecture for future channels

The goal is to eliminate notification logic duplication across applications and provide a reusable notification platform.

---

## 1.2 Problem Statement

Organizations often implement notification functionality independently within each application. This leads to:

* Duplicate development effort
* Inconsistent notification formats
* Tight coupling with notification providers
* Lack of centralized monitoring
* Limited scalability
* Difficult maintenance

NotifyHub aims to solve these problems by providing a centralized notification platform.

---

## 1.3 Scope

### In Scope (Phase 1)

* REST API for notification submission
* Email notifications
* SMS notifications
* Template-driven message generation
* Kafka-based asynchronous processing
* Notification delivery tracking
* Idempotent request handling
* Template caching

### Out of Scope (Phase 1)

* Push notifications
* WhatsApp notifications
* Notification scheduling
* User preference management
* Multi-tenancy
* Retry and DLQ implementation
* Distributed tracing
* Analytics dashboard

---

# 2. System Overview

NotifyHub receives notification requests from client applications.

The platform validates the request, generates channel-specific messages using templates, creates delivery records, and publishes delivery events to Kafka topics.

Dedicated consumers process channel-specific notifications and update delivery status.

---

# 3. Functional Requirements

## FR-1 Notification Submission

The system shall provide a REST API to receive notification requests.

### Endpoint

POST /v1/notifications

---

## FR-2 Event Driven Notifications

The system shall support event-based notification generation.

Example:

* PAYMENT_SUCCESS

* ORDER_DELIVERED

* PASSWORD_RESET

* ACCOUNT_CREATED

---

## FR-3 Template Driven Message Generation

The system shall generate notification messages using templates stored in the database.

Example Template:

	Payment of ₹{amount} at {merchant} was successful.

Example Attributes:

	{
	"amount": "1000",
	"merchant": "Amazon"
	}

Generated Message:

	Payment of ₹1000 at Amazon was successful.

---

## FR-4 Channel Specific Templates

The system shall support separate templates for different channels.

Example:

Event Type: PAYMENT_SUCCESS

EMAIL Template:

	Payment of ₹{amount} at {merchant} was successful.

SMS Template:

	Rs {amount} spent at {merchant}.

---

## FR-5 Multiple Channel Support

The system shall support:

* Email
* SMS

for Phase 1.

A single notification request may generate notifications for multiple channels.

---

## FR-6 Notification Delivery Tracking

The system shall track delivery status for every channel delivery attempt.

Supported statuses:

* PENDING

* PROCESSING

* DELIVERED

* FAILED

* RETRYING

---

## FR-7 Idempotency

The system shall prevent duplicate notification processing using a unique request identifier.

Example:

	{
	"requestId": "223abd6"
	}

Multiple requests with the same requestId shall not create duplicate notifications.

---

## FR-8 Required Attribute Validation

The system shall validate mandatory template attributes before processing.

Example:

Template:

	Payment of ₹{amount} at {merchant} was successful.

Required Attributes:

	[amount,merchant]

If any required attribute is missing, the request shall be rejected.

---

## FR-9 Asynchronous Processing

Notification delivery shall be processed asynchronously using Kafka.

The API shall not wait for actual notification delivery before responding.

---

## FR-10 Delivery Status Updates

Channel consumers shall update delivery status after processing.

Example:

PENDING → PROCESSING → DELIVERED

PENDING → PROCESSING → FAILED

---

# 4. Non Functional Requirements

## NFR-1 Scalability

The system shall support horizontal scaling of:

* Notification Service
* Email Consumer
* SMS Consumer

---

## NFR-2 Availability

The system should continue accepting requests even if notification providers are temporarily unavailable.

---

## NFR-3 Performance

The system should acknowledge requests within 500 milliseconds under normal load.

---

## NFR-4 Reliability

Notification requests shall be persisted before asynchronous processing begins.

---

## NFR-5 Maintainability

New event types and templates shall be configurable without requiring application code changes.

---

## NFR-6 Extensibility

The platform shall support future integration with:

* Push Notifications
* WhatsApp
* Slack
* Microsoft Teams

without significant architectural changes.

---

# 5. API Specification

## Request

POST /v1/notifications

Request Body:

	{
	"requestId": "223abd6",
	"eventType": "PAYMENT_SUCCESS",
	"recipient": {
		"email": "john@test.com",
		"phone": "9999999999"
		},
	"attributes": {
		"amount": "1000",
		"merchant": "Amazon"
		}
	}

---

## Success Response

HTTP 202 Accepted

	{
	"notificationId": 101,
	"status": "RECEIVED"
	}

---

## Failure Response

HTTP 400 Bad Request

	{
	"error": "Missing required attribute: merchant"
	}

---

# 6. Business Rules

## BR-1

Every request must contain a unique requestId.

---

## BR-2

Every event type must have at least one active template.

---

## BR-3

All required template attributes must be supplied.

---

## BR-4

One notification request may generate multiple channel deliveries.

Example:

PAYMENT_SUCCESS

→ EMAIL

→ SMS

---

## BR-5

Only active templates shall be used for notification generation.

---

## BR-6

Notification consumers shall update delivery status after processing.

---

# 7. Assumptions

1. A Kafka broker is available for asynchronous message processing.

2. An SMTP provider is available for email delivery.

3. An SMS gateway is available for SMS delivery.

4. Recipient details are supplied by the calling application.

5. MySQL database is available and accessible.

6. Notification templates are pre-configured in the system.

---

# 8. Constraints

1. Phase 1 supports only Email and SMS channels.

2. Phase 1 supports MySQL as the persistence layer.

3. Phase 1 does not support scheduled notifications.

4. Phase 1 does not support user preference management.

---

# 9. Future Enhancements

## Phase 2

* Retry mechanism
* Dead Letter Queue (DLQ)
* Redis based caching

## Phase 3

* Docker
* Kubernetes
* Prometheus
* Grafana

## Phase 4

* Push Notifications
* WhatsApp Integration
* Multi-Tenancy
* Distributed Tracing
* Outbox Pattern

---

# 10. Success Criteria

NotifyHub shall be considered successful when:

* Applications can submit notification requests through REST APIs.
* Notifications are generated using templates.
* Email and SMS notifications are processed asynchronously.
* Delivery status is tracked successfully.
* Duplicate requests are prevented using requestId.
* The platform can be extended to support additional channels with minimal code changes.
