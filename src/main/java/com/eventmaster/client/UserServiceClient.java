package com.eventmaster.client;

import com.eventmaster.model.FollowerSummary;
import com.eventmaster.model.PageContent;
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
import java.util.stream.Collectors;

@Component
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

    @Value("${user.service.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<String> getFollowingUsernames(String username, String token) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/users/" + username + "/following")
                .queryParam("size", 200)
                .build()
                .encode()
                .toUri();
        try {
            HttpEntity<?> entity = bearerEntity(token);
            ResponseEntity<PageContent<FollowerSummary>> response = restTemplate.exchange(
                    uri, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<PageContent<FollowerSummary>>() {}
            );
            if (response.getBody() == null) return Collections.emptyList();
            return response.getBody().getContent().stream()
                    .map(FollowerSummary::getUsername)
                    .collect(Collectors.toList());
        } catch (RestClientException e) {
            logger.warn("Could not fetch following for '{}' from user-service: {}", username, e.getMessage());
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
