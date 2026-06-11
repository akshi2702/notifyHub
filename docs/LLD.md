# NotifyHub - Low Level Design (LLD)

# 1. Overview

NotifyHub is an event-driven notification platform designed to provide a centralized mechanism for sending notifications through multiple channels such as Email and SMS.

The platform uses Spring Boot, Apache Kafka, and MySQL to provide asynchronous notification processing, template-driven message generation, delivery tracking, and idempotent request handling.

---

# 2. Database Design

## 2.1 notification_request

### Purpose

Stores accepted notification requests and provides idempotency using request_id.

### Schema

```sql
notification_request

notification_id      BIGINT AUTO_INCREMENT PRIMARY KEY

request_id           VARCHAR(36) NOT NULL UNIQUE

event_type           VARCHAR(100) NOT NULL

recipient_details    JSON NOT NULL

attributes           JSON NOT NULL

created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

### Sample Record

```json
{
  "notificationId": 101,
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "PAYMENT_SUCCESS",
  "recipientDetails": {
    "email": "john@test.com",
    "phone": "9999999999"
  },
  "attributes": {
    "amount": "1000",
    "merchant": "Amazon"
  }
}
```

---

## 2.2 notification_template

### Purpose

Stores channel-specific notification templates.

### Schema

```sql
notification_template

template_id           BIGINT AUTO_INCREMENT PRIMARY KEY

event_type            VARCHAR(100) NOT NULL

channel               VARCHAR(20) NOT NULL

template_text         TEXT NOT NULL

required_attributes   JSON NOT NULL

active                BOOLEAN NOT NULL DEFAULT TRUE

created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

UNIQUE(event_type, channel)
```

### Sample Records

#### EMAIL

```text
templateId = 1
eventType = PAYMENT_SUCCESS
channel = EMAIL
requiredAttributes = ["amount","merchant"]
templateText = Payment of ₹{amount} at {merchant} successful
active = true
```

#### SMS

```text
templateId = 2
eventType = PAYMENT_SUCCESS
channel = SMS
requiredAttributes = ["amount","merchant"]
templateText = Rs {amount} spent at {merchant}
active = true
```

---

## 2.3 notification_delivery

### Purpose

Tracks delivery attempts and acts as the source of truth for notification status.

### Schema

```sql
notification_delivery

delivery_id          BIGINT AUTO_INCREMENT PRIMARY KEY

notification_id      BIGINT NOT NULL

channel              VARCHAR(20) NOT NULL

recipient            VARCHAR(255) NOT NULL

message              TEXT NOT NULL

status               VARCHAR(20) NOT NULL

failure_reason       VARCHAR(500)

created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

FOREIGN KEY(notification_id)
REFERENCES notification_request(notification_id)
```

### Sample Records

```text
deliveryId = 1
notificationId = 101
channel = EMAIL
recipient = john@test.com
message = Payment of ₹1000 at Amazon successful
status = DELIVERED
```

```text
deliveryId = 2
notificationId = 101
channel = SMS
recipient = 9999999999
message = Rs 1000 spent at Amazon
status = FAILED
failureReason = Invalid Phone Number
```

---

# 3. Entity Relationship Design

## 3.1 ER Diagram

![NotifyHub ER Diagram](images/notifyhub-erd.png)


---

## 3.2 Relationship Description

### notification_request → notification_delivery

A notification request has a one-to-many relationship with notification_delivery.

A single notification request may result in multiple delivery records depending on the active notification channels configured for the event type.

Example:

```text
Notification Request
        |
        +---- Email Delivery
        |
        +---- SMS Delivery
```

For a PAYMENT_SUCCESS event, if both Email and SMS templates are active, two delivery records will be created.

Example:

```text
notificationId = 101

deliveryId = 1 (EMAIL)

deliveryId = 2 (SMS)
```

---

## 3.3 Design Decisions

### Template Independence

notification_template is intentionally not linked to notification_request through a foreign key relationship.

Templates are retrieved dynamically using:

* event_type
* channel

This allows notification templates to be modified independently without impacting historical notification records.

---

### Idempotency

request_id is maintained as a unique key in notification_request.

This prevents duplicate notification requests from being processed multiple times.

If a request with an existing request_id is received, NotifyHub treats it as a duplicate request and avoids creating duplicate notifications.

---

### Delivery Tracking

notification_delivery acts as the source of truth for notification status.

Each channel-specific delivery attempt is stored as an individual record.

Examples:

```text
EMAIL -> DELIVERED

SMS -> FAILED
```

The overall notification status can be derived from associated delivery records instead of storing a separate status in notification_request.

---

### Delivery Status Values

The following delivery statuses are supported in Phase 1:

```text
PENDING
DELIVERED
FAILED
```

---

### Template Uniqueness

The notification_template table enforces the following unique constraint:

```text
UNIQUE(event_type, channel)
```

This ensures only one template exists for a given event type and channel combination.

---

### Failure Reason Tracking

notification_delivery contains an optional failure_reason column.

Examples:

```text
Invalid Phone Number

SMTP Connection Timeout

Email Provider Unavailable
```

This information can be used for troubleshooting and operational visibility.

---

### JSON-Based Recipient Storage

Recipient information is stored in JSON format.

Example:

```json
{
  "email": "john@test.com",
  "phone": "9999999999"
}
```

This allows future recipient types to be introduced without database schema changes.

---

### JSON-Based Event Attributes

Notification-specific attributes are stored as JSON.

Example:

```json
{
  "amount": "1000",
  "merchant": "Amazon"
}
```

This enables support for multiple event types without requiring schema modifications.

---

# 4. Database Indexes

## notification_request

```sql
PRIMARY KEY(notification_id)

UNIQUE INDEX uk_request_id(request_id)

INDEX idx_event_type(event_type)
```

## notification_template

```sql
PRIMARY KEY(template_id)

UNIQUE INDEX uk_event_channel(event_type, channel)

INDEX idx_active(active)
```

## notification_delivery

```sql
PRIMARY KEY(delivery_id)

INDEX idx_delivery_notification_id(notification_id)

INDEX idx_status(status)
```

---

# 5. Kafka Design

## Topics

```text
email-topic

sms-topic
```

---

## Email Topic Payload

```json
{
  "notificationId": 101,
  "deliveryId": 1,
  "channel": "EMAIL",
  "recipient": "john@test.com",
  "message": "Payment of ₹1000 at Amazon successful"
}
```

---

## SMS Topic Payload

```json
{
  "notificationId": 101,
  "deliveryId": 2,
  "channel": "SMS",
  "recipient": "9999999999",
  "message": "Rs 1000 spent at Amazon"
}
```

---

# 6. Processing Flow

1. Client submits notification request.
2. Notification Service validates request.
3. Notification Service persists request in notification_request.
4. Notification Service retrieves active templates from notification_template.
5. Notification Service creates PENDING records in notification_delivery.
6. Notification Service publishes messages to Kafka topics.
7. Email Consumer processes email messages.
8. SMS Consumer processes SMS messages.
9. Consumers update notification_delivery status.
10. Notification status can be retrieved using delivery records.

---
# 7. API Contract Design

## 7.1 Create Notification API

### Endpoint

```http
POST /v1/notifications
```

### Purpose

Accepts a notification request from a client application and initiates asynchronous notification processing.

### Request Body

```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
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
```

### Success Response

HTTP 202 Accepted

```json
{
  "notificationId": 101,
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  
}
```

### Validation Rules


| Field      | Validation             |
| ---------- | ---------------------- |
| requestId  | Mandatory, UUID format |
| eventType  | Mandatory              |
| recipient  | Mandatory              |
| attributes | Mandatory              |
| requestId  | Must be unique         |


## Supported Event Types

### 1. PAYMENT_SUCCESS

#### Required Attributes

```json
{
  "amount": "1000",
  "merchant": "Amazon"
}
```

#### Supported Channels

```text
EMAIL
SMS
```

---

### 2. ORDER_DELIVERED

#### Required Attributes

```json
{
  "orderId": "ORD123",
  "deliveryDate": "2026-06-15"
}
```

#### Supported Channels

```text
EMAIL
SMS
```

---

### 3. PASSWORD_RESET

#### Required Attributes

```json
{
  "resetLink": "https://example.com/reset"
}
```

#### Supported Channels

```text
EMAIL
```

---

### Validation Rule

For every notification request:

1. eventType must exist in notification_template.
2. All required attributes configured for the event type must be present in the request.
3. Missing required attributes result in HTTP 400 Bad Request.

### Error Responses

#### Duplicate Request

HTTP 409 Conflict

```json
{
  "errorCode": "DUPLICATE_REQUEST",
  "message": "Request already exists"
}
```

#### Invalid Request

HTTP 400 Bad Request

```json
{
  "errorCode": "MISSING_REQUIRED_ATTRIBUTE",
  "message": "merchant attribute is mandatory"
}
```

---

## 7.2 Get Notification Status API

### Endpoint

```http
GET /v1/notifications/request/{requestId}
```

### Purpose

Returns delivery status information for a notification request.

### Path Parameters

| Parameter | Description                                      |
| --------- | ------------------------------------------------ |
| requestId | Unique request identifier supplied by the client |

### Success Response

HTTP 200 OK

```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "overallStatus": "PARTIALLY_COMPLETED",
  "deliveries": [
    {
      "channel": "EMAIL",
      "status": "DELIVERED"
    },
    {
      "channel": "SMS",
      "status": "FAILED"
    }
  ]
}
```

### Overall Status Calculation

| Delivery Statuses              | Overall Status      |
| ------------------------------ | ------------------- |
| All DELIVERED                  | COMPLETED           |
| Some DELIVERED and Some FAILED | PARTIALLY_COMPLETED |
| All FAILED                     | FAILED              |
| At least one PENDING           | IN_PROGRESS         |

### Error Response

HTTP 404 Not Found

```json
{
  "errorCode": "NOTIFICATION_NOT_FOUND",
  "message": "Notification request not found"
}
```


# 8. Phase 1 Scope

## Included

* Notification Submission API
* Email Notifications
* SMS Notifications
* Kafka-Based Processing
* Template-Driven Messages
* Delivery Tracking
* MySQL Persistence
* Idempotent Request Handling

## Excluded

* Retry Mechanism
* Dead Letter Queue (DLQ)
* Redis Cache
* Push Notifications
* WhatsApp Notifications
* Kubernetes Deployment
* Prometheus Monitoring
* Grafana Dashboards
* Audit Framework
* Multi-Tenant Client Management

```
```
