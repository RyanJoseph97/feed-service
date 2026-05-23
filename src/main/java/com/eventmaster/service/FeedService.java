package com.eventmaster.service;

import com.eventmaster.client.EventServiceClient;
import com.eventmaster.client.UserServiceClient;
import com.eventmaster.model.FeedEvent;
import com.eventmaster.model.FeedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FeedService {

    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private EventServiceClient eventServiceClient;

    public Page<FeedEvent> getFeed(String username, FeedType type, Pageable pageable, String token) {
        logger.debug("Building feed for '{}' type={}", username, type);

        List<FeedEvent> all = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        if (type == FeedType.FOLLOWING || type == FeedType.ALL) {
            // Forward the caller's token — user-service requires auth on /following
            List<String> following = userServiceClient.getFollowingUsernames(username, token);
            logger.debug("User '{}' follows {} accounts", username, following.size());
            for (String followedUser : following) {
                List<FeedEvent> events = eventServiceClient.getEventsByCreator(followedUser);
                for (FeedEvent event : events) {
                    if (event.getId() != null && seenIds.add(event.getId())) {
                        event.setFeedSource("FOLLOWING");
                        all.add(event);
                    }
                }
            }
        }

        if (type == FeedType.RECOMMENDED || type == FeedType.ALL) {
            List<FeedEvent> recommended = eventServiceClient.getUpcomingPublicEvents(now);
            for (FeedEvent event : recommended) {
                if (event.getId() != null && seenIds.add(event.getId())) {
                    event.setFeedSource("RECOMMENDED");
                    all.add(event);
                }
            }
        }

        all.sort(Comparator.comparing(FeedEvent::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));

        int total = all.size();
        int offset = (int) pageable.getOffset();
        int end = Math.min(offset + pageable.getPageSize(), total);
        List<FeedEvent> slice = offset >= total ? Collections.emptyList() : all.subList(offset, end);

        return new PageImpl<>(slice, pageable, total);
    }
}
