package eshop.com.eshopauthservice.bff;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/bff")
public class BffCallbackController {

    private record TokenPair(String accessToken, String refreshToken) {}

    private final RestTemplate restTemplate;

    @Value("${bff.spa-redirect-uri}")
    private String spaRedirectUri;

    @Value("${bff.token-uri}")
    private String tokenUri;

    @Value("${bff.client-id}")
    private String clientId;

    @Value("${bff.redirect-uri}")
    private String redirectUri;

    public BffCallbackController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam("code_verifier") String codeVerifier,
            HttpServletResponse response
    ) {
        TokenPair tokens = exchangeCodeForTokens(code, codeVerifier);

        response.addCookie(buildCookie("access_token", tokens.accessToken(), 60 * 15));
        response.addCookie(buildCookie("refresh_token", tokens.refreshToken(), 60 * 60 * 24 * 7));

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, spaRedirectUri)
                .build();
    }

    @SuppressWarnings("unchecked")
    private TokenPair exchangeCodeForTokens(String code, String codeVerifier) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("code_verifier", codeVerifier);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                tokenUri,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> responseBody = tokenResponse.getBody();
        return new TokenPair(
                (String) responseBody.get("access_token"),
                (String) responseBody.get("refresh_token")
        );
    }

    private Cookie buildCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
