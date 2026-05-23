package com.eventmaster.client;

import com.eventmaster.model.FeedEvent;
import com.eventmaster.model.PageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class EventServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceClient.class);

    @Value("${event.service.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<FeedEvent> getUpcomingEventsByCreator(String creatorUsername, LocalDateTime startAfter) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/events")
                .queryParam("creatorUsername", creatorUsername)
                .queryParam("startAfter", startAfter.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .queryParam("size", 50)
                .build()
                .encode()
                .toUri();
        return fetch(uri, "events for creator '" + creatorUsername + "'");
    }

    public List<FeedEvent> getUpcomingPublicEvents(LocalDateTime startAfter) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/events")
                .queryParam("visibility", "PUBLIC")
                .queryParam("startAfter", startAfter.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .queryParam("size", 100)
                .build()
                .encode()
                .toUri();
        return fetch(uri, "upcoming public events");
    }

    private List<FeedEvent> fetch(URI uri, String description) {
        try {
            ResponseEntity<PageContent<FeedEvent>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageContent<FeedEvent>>() {}
            );
            if (response.getBody() == null) return Collections.emptyList();
            return response.getBody().getContent();
        } catch (RestClientException e) {
            logger.warn("Could not fetch {} from event-service: {}", description, e.getMessage());
            return Collections.emptyList();
        }
    }
}
