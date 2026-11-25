package com.solocrew.platform.email;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailPayloadCipherTests {

    private static final String TEST_KEY =
        "0123456789abcdef0123456789abcdef"
            + "0123456789abcdef0123456789abcdef";

    private final EmailPayloadCipher cipher =
        new EmailPayloadCipher(TEST_KEY);

    @Test
    void encryptsAndDecryptsEmailContent() {
        EmailContent original = new EmailContent(
            "Verify your email",
            "Open this private verification link."
        );

        EncryptedEmailPayload encrypted = cipher.encrypt(original);

        EmailContent decrypted = cipher.decrypt(encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    void encryptedPayloadDoesNotContainPlaintext() {
        EmailContent original = new EmailContent(
            "Private subject",
            "Private verification token"
        );

        EncryptedEmailPayload encrypted = cipher.encrypt(original);

        String ciphertext = new String(
            encrypted.ciphertext(),
            StandardCharsets.UTF_8
        );

        assertFalse(ciphertext.contains("Private subject"));
        assertFalse(ciphertext.contains("Private verification token"));
    }

    @Test
    void usesFreshNonceForEveryEncryption() {
        EmailContent content = new EmailContent(
            "Subject",
            "Body"
        );

        EncryptedEmailPayload first = cipher.encrypt(content);
        EncryptedEmailPayload second = cipher.encrypt(content);

        assertFalse(Arrays.equals(first.nonce(), second.nonce()));
        assertFalse(
            Arrays.equals(first.ciphertext(), second.ciphertext())
        );
    }

    @Test
    void rejectsModifiedCiphertext() {
        EncryptedEmailPayload encrypted = cipher.encrypt(
            new EmailContent("Subject", "Body")
        );

        byte[] modifiedCiphertext = encrypted.ciphertext();
        modifiedCiphertext[0] ^= 1;

        EncryptedEmailPayload modified = new EncryptedEmailPayload(
            encrypted.nonce(),
            modifiedCiphertext
        );

        assertThrows(
            IllegalStateException.class,
            () -> cipher.decrypt(modified)
        );
    }

    @Test
    void payloadDoesNotExposeMutableInternalArrays() {
        EmailContent original = new EmailContent(
            "Subject",
            "Body"
        );

        EncryptedEmailPayload encrypted = cipher.encrypt(original);

        byte[] returnedNonce = encrypted.nonce();
        returnedNonce[0] ^= 1;

        byte[] returnedCiphertext = encrypted.ciphertext();
        returnedCiphertext[0] ^= 1;

        // Mutating arrays returned by the record must not change
        // the original encrypted payload.
        assertEquals(original, cipher.decrypt(encrypted));
    }

    @Test
    void usesExpectedNonceLength() {
        EncryptedEmailPayload encrypted = cipher.encrypt(
            new EmailContent("Subject", "Body")
        );

        assertEquals(12, encrypted.nonce().length);
        assertFalse(encrypted.ciphertext().length == 0);
    }
}
