package com.solocrew.platform.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VerificationEmailTemplate {

    private final String verificationUrlBase;

    public VerificationEmailTemplate(
        @Value("${solocrew.mail.verification-url-base}")
        String verificationUrlBase
    ) {
        this.verificationUrlBase = removeTrailingSlash(
            verificationUrlBase
        );
    }

    public EmailContent create(String rawToken) {
        String verificationUrl =
            verificationUrlBase + "?token=" + rawToken;

        String body = """
            Welcome to SoloCrew.

            Please verify your email address by opening the link below:

            %s

            This link expires in 24 hours and can only be used once.

            If you did not request a SoloCrew account, you can ignore
            this email.

            Your business. Your digital workforce.
            SoloCrew
            """.formatted(verificationUrl);

        return new EmailContent(
            "Verify your SoloCrew email address",
            body
        );
    }

    private String removeTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }
}
