INSERT INTO notification_template
(
    event_type,
    channel,
    template_text,
    required_attributes,
    active
)
VALUES
(
    'PAYMENT_SUCCESS',
    'EMAIL',
    'Payment of ₹{amount} at {merchant} successful',
    '["amount","merchant"]',
    true
);

INSERT INTO notification_template
(
    event_type,
    channel,
    template_text,
    required_attributes,
    active
)
VALUES
(
    'PAYMENT_SUCCESS',
    'SMS',
    'Rs {amount} spent at {merchant}',
    '["amount","merchant"]',
    true
);

INSERT INTO notification_template
(
    event_type,
    channel,
    template_text,
    required_attributes,
    active
)
VALUES
(
    'ORDER_DELIVERED',
    'EMAIL',
    'Order {orderId} delivered on {deliveryDate}',
    '["orderId","deliveryDate"]',
    true
);

INSERT INTO notification_template
(
    event_type,
    channel,
    template_text,
    required_attributes,
    active
)
VALUES
(
    'PASSWORD_RESET',
    'EMAIL',
    'Reset password using {resetLink}',
    '["resetLink"]',
    true
);