package com.loanapplication.security;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class TotpService {

    private final SecretGenerator secretGenerator =
            new DefaultSecretGenerator();

    private final DefaultCodeVerifier verifier;

    private final String issuer;

    public TotpService(
            @Value("${totp.issuer:Loan Application}") String issuer,
            @Value("${totp.window:1}") int window) {

        CodeGenerator generator = new DefaultCodeGenerator();

        this.verifier =
                new DefaultCodeVerifier(
                        generator,
                        new SystemTimeProvider()
                );

        this.verifier.setAllowedTimePeriodDiscrepancy(window);

        this.issuer = issuer;
    }

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public boolean verify(String secret, String code) {

        if (secret == null || code == null) {
            return false;
        }

        return verifier.isValidCode(secret, code);
    }

    public String otpAuthUri(
            String email,
            String secret) {

        return "otpauth://totp/"
                + encode(issuer + ":" + email)
                + "?secret=" + secret
                + "&issuer=" + encode(issuer)
                + "&algorithm=SHA1&digits=6&period=30";
    }

    public String qrDataUri(String otpAuthUri) {

        try {

            BitMatrix matrix =
                    new QRCodeWriter().encode(
                            otpAuthUri,
                            BarcodeFormat.QR_CODE,
                            240,
                            240,
                            Map.of(
                                    EncodeHintType.MARGIN,
                                    1
                            )
                    );

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output
            );

            return "data:image/png;base64,"
                    + Base64.getEncoder()
                    .encodeToString(output.toByteArray());

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Could not generate QR code",
                    e
            );
        }
    }

    public String formatSecret(String secret) {

        return secret
                .replaceAll("(.{4})", "$1 ")
                .trim();
    }

    private static String encode(String text) {

        return URLEncoder
                .encode(text, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}