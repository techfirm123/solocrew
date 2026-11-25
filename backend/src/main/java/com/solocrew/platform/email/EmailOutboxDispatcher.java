package com.solocrew.platform.email;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "solocrew.mail.outbox",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class EmailOutboxDispatcher {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(EmailOutboxDispatcher.class);

    private static final int MAXIMUM_BATCH_SIZE = 10;

    private final EmailOutboxRepository repository;

    private final EmailPayloadCipher payloadCipher;

    private final JavaMailSender mailSender;

    private final String fromAddress;

    public EmailOutboxDispatcher(
        EmailOutboxRepository repository,
        EmailPayloadCipher payloadCipher,
        JavaMailSender mailSender,
        @Value("${solocrew.mail.from}") String fromAddress
    ) {
        this.repository = repository;
        this.payloadCipher = payloadCipher;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Scheduled(
        fixedDelayString = "${solocrew.mail.outbox.poll-interval-ms:5000}"
    )
    public void dispatchPendingEmails() {
        for (int index = 0; index < MAXIMUM_BATCH_SIZE; index++) {
            Optional<EmailOutboxDelivery> optionalDelivery =
                repository.claimNext();

            if (optionalDelivery.isEmpty()) {
                return;
            }

            dispatch(optionalDelivery.get());
        }
    }

    private void dispatch(EmailOutboxDelivery delivery) {
        try {
            EncryptedEmailPayload encryptedPayload =
                new EncryptedEmailPayload(
                    delivery.nonce(),
                    delivery.ciphertext()
                );

            EmailContent content = payloadCipher.decrypt(
                encryptedPayload
            );

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromAddress);
            message.setTo(delivery.recipientEmail());
            message.setSubject(content.subject());
            message.setText(content.body());

            mailSender.send(message);

            repository.markSent(delivery.id());

            LOGGER.info(
                "Email outbox message delivered; outboxId={}",
                delivery.id()
            );
        } catch (RuntimeException exception) {
            long retryDelaySeconds = calculateRetryDelay(
                delivery.attempts()
            );

            repository.markFailed(
                delivery.id(),
                exception.getClass().getSimpleName(),
                retryDelaySeconds
            );

            LOGGER.warn(
                "Email delivery failed; outboxId={}, errorType={}, attempt={}",
                delivery.id(),
                exception.getClass().getSimpleName(),
                delivery.attempts()
            );
        }
    }

    private long calculateRetryDelay(int attempts) {
        int exponent = Math.min(Math.max(attempts - 1, 0), 6);

        return Math.min(900L, 15L * (1L << exponent));
    }
}
