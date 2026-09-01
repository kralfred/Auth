package org.example.reservation_api.services;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.JWK;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class DpopService {

    // Maximum allowed age for a DPoP proof to prevent replay attacks (e.g., 60 seconds)
    private static final long MAX_ALLOWED_AGE_SECONDS = 60;

    public String verifyAndExtractJkt(String dpopHeader, String expectedMethod, String expectedUri) {
        try {
            // 1. Parse the DPoP proof JWT
            SignedJWT signedJwt = SignedJWT.parse(dpopHeader);
            JWSHeader header = signedJwt.getHeader();

            // 2. Validate header parameters: typ MUST be "dpop+jwt"
            if (header.getType() == null || !"dpop+jwt".equalsIgnoreCase(header.getType().getType())) {
                throw new BadCredentialsException("Invalid DPoP header type. Must be 'dpop+jwt'");
            }

            // 3. Extract the embedded JWK public key
            JWK jwk = header.getJWK();
            if (jwk == null) {
                throw new BadCredentialsException("DPoP proof missing embedded public key ('jwk') in header");
            }

            // 4. Verify signature using the embedded public key
            PublicKey publicKey = jwk.toRSAKey() != null ? jwk.toRSAKey().toPublicKey() : jwk.toECKey().toPublicKey();
            DefaultJWSVerifierFactory verifierFactory = new DefaultJWSVerifierFactory();
            var verifier = verifierFactory.createJWSVerifier(header, publicKey);

            if (!signedJwt.verify(verifier)) {
                throw new BadCredentialsException("Invalid DPoP proof signature");
            }

            // 5. Validate DPoP claims (htm, htu, iat, jti)
            Map<String, Object> claims = signedJwt.getJWTClaimsSet().getClaims();

            String htm = (String) claims.get("htm"); // HTTP Method
            String htu = (String) claims.get("htu"); // HTTP URI
            Date iat = signedJwt.getJWTClaimsSet().getIssueTime(); // Issued At
            String jti = signedJwt.getJWTClaimsSet().getJWTID(); // Unique token ID

            if (htm == null || !htm.equalsIgnoreCase(expectedMethod)) {
                throw new BadCredentialsException("DPoP proof 'htm' claim mismatch");
            }

            if (htu == null || !htu.contains(expectedUri)) {
                throw new BadCredentialsException("DPoP proof 'htu' claim mismatch");
            }

            if (jti == null || jti.isBlank()) {
                throw new BadCredentialsException("DPoP proof missing 'jti' claim");
            }

            // Ensure proof is fresh (not older than 60s)
            if (iat == null || Instant.now().minusSeconds(MAX_ALLOWED_AGE_SECONDS).isAfter(iat.toInstant())) {
                throw new BadCredentialsException("DPoP proof has expired");
            }

            // 6. Compute and return the SHA-256 thumbprint (dpop_jkt)
            return jwk.computeThumbprint().toString();

        } catch (Exception e) {
            throw new BadCredentialsException("Failed to validate DPoP proof: " + e.getMessage(), e);
        }
    }
}
