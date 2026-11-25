package com.solocrew.platform.email;

import java.util.UUID;

public record EmailOutboxDelivery(
    UUID id,
    String recipientEmail,
    int attempts,
    byte[] nonce,
    byte[] ciphertext
) {
}
