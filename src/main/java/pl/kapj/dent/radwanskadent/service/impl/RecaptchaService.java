package pl.kapj.dent.radwanskadent.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.kapj.dent.radwanskadent.service.IRecaptchaService;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class RecaptchaService implements IRecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);

    private final String recaptchaUrl;
    private final String googleSecretKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecaptchaService(@Value("${recaptcha.url:https://www.google.com/recaptcha/api/siteverify}") String recaptchaUrl,
                            @Value("${recaptcha.secret}") String googleSecretKey) {
        this.recaptchaUrl = recaptchaUrl;
        this.googleSecretKey = googleSecretKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public boolean verify(String captcha) {
        if (captcha == null || captcha.isBlank()) {
            return false;
        }

        try {
            String formBody = "secret=" + URLEncoder.encode(googleSecretKey, StandardCharsets.UTF_8)
                    + "&response=" + URLEncoder.encode(captcha, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(recaptchaUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return objectMapper.readTree(response.body()).path("success").asBoolean(false);
        } catch (Exception e) {
            log.error("Błąd weryfikacji reCAPTCHA: {}", e.getMessage(), e);
            return false;
        }
    }
}
