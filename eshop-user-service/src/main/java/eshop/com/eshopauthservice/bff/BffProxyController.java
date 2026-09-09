package eshop.com.eshopauthservice.bff;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@RestController
@RequestMapping("/bff/api")
public class BffProxyController {

    private final RestTemplate restTemplate;

    public BffProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(
            HttpServletRequest request,
            @CookieValue("access_token") String accessToken
    ) throws Exception {
        String targetPath = request.getRequestURI().replaceFirst("/bff", "");
        String queryString = request.getQueryString();
        String targetUrl = "http://traefik" + targetPath + (queryString != null ? "?" + queryString : "");

        HttpHeaders headers = copyHeaders(request);
        headers.setBearerAuth(accessToken);

        byte[] body = request.getInputStream().readAllBytes();
        HttpEntity<byte[]> entity = new HttpEntity<>(body.length > 0 ? body : null, headers);

        return restTemplate.exchange(
                URI.create(targetUrl),
                HttpMethod.valueOf(request.getMethod()),
                entity,
                byte[].class
        );
    }

    private HttpHeaders copyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!name.equalsIgnoreCase("cookie") && !name.equalsIgnoreCase("authorization")) {
                List<String> values = Collections.list(request.getHeaders(name));
                headers.put(name, values);
            }
        }
        return headers;
    }
}
