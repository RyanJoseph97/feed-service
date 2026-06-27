package com.eventmaster.client;

import com.eventmaster.model.FeedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Component
public class RecommendationServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceClient.class);

    @Value("${recommendation.service.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Fetch personalized recommendations for the caller. recommendation-service derives
     * the user from the forwarded JWT and returns events already ranked by descending
     * score, so the caller must preserve this order rather than re-sorting.
     * Returns an empty list (fail-soft) if the service is unreachable.
     */
    public List<FeedEvent> getRecommendedEvents(int limit, String token) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/recommendations")
                .queryParam("limit", limit)
                .build()
                .encode()
                .toUri();
        try {
            ResponseEntity<List<FeedEvent>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    bearerEntity(token),
                    new ParameterizedTypeReference<List<FeedEvent>>() {}
            );
            if (response.getBody() == null) return Collections.emptyList();
            return response.getBody();
        } catch (RestClientException e) {
            logger.warn("Could not fetch recommendations from recommendation-service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private HttpEntity<?> bearerEntity(String token) {
        if (token == null) return HttpEntity.EMPTY;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
