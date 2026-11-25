package com.solocrew.platform.email;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailOutboxService {

    private final EmailOutboxRepository repository;

    private final EmailPayloadCipher payloadCipher;

    private final Clock clock;

    public EmailOutboxService(
        EmailOutboxRepository repository,
        EmailPayloadCipher payloadCipher,
        Clock clock
    ) {
        this.repository = repository;
        this.payloadCipher = payloadCipher;
        this.clock = clock;
    }

    @Transactional
    public UUID enqueue(
        String recipientEmail,
        EmailContent content
    ) {
        UUID outboxId = UUID.randomUUID();

        EncryptedEmailPayload encryptedPayload =
            payloadCipher.encrypt(content);

        repository.enqueue(
            outboxId,
            recipientEmail,
            encryptedPayload,
            clock.instant()
        );

        return outboxId;
    }
}
