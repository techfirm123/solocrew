package com.solocrew.platform.email;

public record EncryptedEmailPayload(
    byte[] nonce,
    byte[] ciphertext
) {

    public EncryptedEmailPayload {
        nonce = nonce.clone();
        ciphertext = ciphertext.clone();
    }

    @Override
    public byte[] nonce() {
        return nonce.clone();
    }

    @Override
    public byte[] ciphertext() {
        return ciphertext.clone();
    }
}
