package org.example.reservation_api.services;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Service
public class DpopService {

    private static final long MAX_ALLOWED_AGE_SECONDS = 60;

    // Algorithms we accept for DPoP proofs. Never allow "none" or symmetric (HS*).
    private static final Set<JWSAlgorithm> ALLOWED_ALGS = Set.of(
            JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512,
            JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.PS256
    );

    /** Result of verifying a proof. */
    public record Proof(String jkt, String jti) {}

    /**
     * Verify a DPoP proof.
     *
     * @param accessToken when non-null, the "ath" claim is also checked.
     *                    Pass null for the login flow (no token exists yet).
     */
    public Proof verifyProof(String dpopHeader,
                             String expectedMethod,
                             String expectedUri,
                             String accessToken) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(dpopHeader);
            JWSHeader header = signedJwt.getHeader();

            if (header.getType() == null
                    || !"dpop+jwt".equalsIgnoreCase(header.getType().getType())) {
                throw new BadCredentialsException("Invalid DPoP header 'typ'");
            }

            if (!ALLOWED_ALGS.contains(header.getAlgorithm())) {
                throw new BadCredentialsException("Unsupported DPoP algorithm: " + header.getAlgorithm());
            }

            JWK jwk = header.getJWK();
            if (jwk == null) {
                throw new BadCredentialsException("DPoP proof missing 'jwk' in header");
            }

            PublicKey publicKey = extractPublicKey(jwk);
            JWSVerifier verifier = new DefaultJWSVerifierFactory()
                    .createJWSVerifier(header, publicKey);
            if (!signedJwt.verify(verifier)) {
                throw new BadCredentialsException("Invalid DPoP proof signature");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            String htm = claims.getStringClaim("htm");
            String htu = claims.getStringClaim("htu");
            Date iat   = claims.getIssueTime();
            String jti = claims.getJWTID();

            if (htm == null || !htm.equalsIgnoreCase(expectedMethod)) {
                throw new BadCredentialsException("DPoP 'htm' mismatch");
            }
            if (htu == null || !htuMatches(htu, expectedUri)) {
                throw new BadCredentialsException("DPoP 'htu' mismatch");
            }
            if (jti == null || jti.isBlank()) {
                throw new BadCredentialsException("DPoP 'jti' missing");
            }
            if (iat == null
                    || Instant.now().minusSeconds(MAX_ALLOWED_AGE_SECONDS).isAfter(iat.toInstant())) {
                throw new BadCredentialsException("DPoP proof has expired");
            }

            if (accessToken != null) {
                String ath = claims.getStringClaim("ath");
                if (ath == null || !ath.equals(computeAth(accessToken))) {
                    throw new BadCredentialsException("DPoP 'ath' mismatch");
                }
            }

            String jkt = jwk.computeThumbprint().toString();
            return new Proof(jkt, jti);

        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new BadCredentialsException("Failed to validate DPoP proof: " + e.getMessage(), e);
        }
    }

    /** Kept for the login path — no access token exists yet. */
    public String verifyAndExtractJkt(String dpopHeader, String expectedMethod, String expectedUri) {
        return verifyProof(dpopHeader, expectedMethod, expectedUri, null).jkt();
    }

    private PublicKey extractPublicKey(JWK jwk) throws Exception {
        KeyType type = jwk.getKeyType();
        if (KeyType.RSA.equals(type)) return jwk.toRSAKey().toPublicKey();
        if (KeyType.EC.equals(type))  return jwk.toECKey().toPublicKey();
        throw new BadCredentialsException("Unsupported DPoP key type: " + type);
    }

    private String computeAth(String accessToken) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(accessToken.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    /**
     * Compare proof 'htu' against the expected URL. Compares path strictly.
     * If the expected URI carries a scheme/host (i.e. it's a full URL),
     * those are compared too. Query and fragment are ignored.
     */
    private boolean htuMatches(String proofHtu, String expectedUri) {
        try {
            URI proof = URI.create(proofHtu);
            URI expected = URI.create(expectedUri);
            if (!Objects.equals(proof.getPath(), expected.getPath())) return false;
            if (expected.getScheme() != null
                    && !expected.getScheme().equalsIgnoreCase(proof.getScheme())) return false;
            if (expected.getHost() != null
                    && !expected.getHost().equalsIgnoreCase(proof.getHost())) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
