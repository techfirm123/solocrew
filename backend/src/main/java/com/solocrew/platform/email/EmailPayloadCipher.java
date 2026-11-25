package com.solocrew.platform.email;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HexFormat;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailPayloadCipher {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final int NONCE_LENGTH_BYTES = 12;

    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;

    private final SecureRandom secureRandom;

    public EmailPayloadCipher(
        @Value("${solocrew.mail.encryption-key}") String encryptionKeyHex
    ) {
        byte[] keyBytes;

        try {
            keyBytes = HexFormat.of().parseHex(encryptionKeyHex);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                "SoloCrew email encryption key must be hexadecimal",
                exception
            );
        }

        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                "SoloCrew email encryption key must contain 32 bytes"
            );
        }

        this.key = new SecretKeySpec(keyBytes, "AES");
        this.secureRandom = new SecureRandom();
    }

    public EncryptedEmailPayload encrypt(EmailContent content) {
        byte[] nonce = new byte[NONCE_LENGTH_BYTES];

        secureRandom.nextBytes(nonce);

        String serialized =
            content.subject() + "\n\n" + content.body();

        byte[] plaintext = serialized.getBytes(
            StandardCharsets.UTF_8
        );

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                new GCMParameterSpec(TAG_LENGTH_BITS, nonce)
            );

            byte[] ciphertext = cipher.doFinal(plaintext);

            return new EncryptedEmailPayload(
                nonce,
                ciphertext
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                "Could not encrypt email payload",
                exception
            );
        }
    }

    public EmailContent decrypt(EncryptedEmailPayload encryptedPayload) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                new GCMParameterSpec(
                    TAG_LENGTH_BITS,
                    encryptedPayload.nonce()
                )
            );

            byte[] plaintext = cipher.doFinal(
                encryptedPayload.ciphertext()
            );

            String serialized = new String(
                plaintext,
                StandardCharsets.UTF_8
            );

            int separatorIndex = serialized.indexOf("\n\n");

            if (separatorIndex <= 0) {
                throw new IllegalStateException(
                    "Encrypted email payload has an invalid structure"
                );
            }

            return new EmailContent(
                serialized.substring(0, separatorIndex),
                serialized.substring(separatorIndex + 2)
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                "Could not decrypt email payload",
                exception
            );
        }
    }
}
