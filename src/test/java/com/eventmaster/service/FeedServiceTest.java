package com.eventmaster.service;

import com.eventmaster.client.EventServiceClient;
import com.eventmaster.client.UserServiceClient;
import com.eventmaster.model.FeedEvent;
import com.eventmaster.model.FeedType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FeedServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private EventServiceClient eventServiceClient;

    @InjectMocks
    private FeedService feedService;

    private FeedEvent event1;
    private FeedEvent event2;
    private FeedEvent event3;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        event1 = feedEvent(1L, "alice", LocalDateTime.now().plusDays(1));
        event2 = feedEvent(2L, "bob",   LocalDateTime.now().plusDays(3));
        event3 = feedEvent(3L, "carol", LocalDateTime.now().plusDays(2));
    }

    private FeedEvent feedEvent(Long id, String creator, LocalDateTime startTime) {
        FeedEvent e = new FeedEvent();
        e.setId(id);
        e.setTitle("Event " + id);
        e.setCreatorUsername(creator);
        e.setStartTime(startTime);
        e.setVisibility("PUBLIC");
        return e;
    }

    // --- FOLLOWING type ---

    @Test
    public void getFeed_followingType_onlyCallsFollowingClients() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(List.of("alice"));
        when(eventServiceClient.getUpcomingEventsByCreator(eq("alice"), any())).thenReturn(List.of(event1));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.FOLLOWING, PageRequest.of(0, 20), null);

        assertEquals(1, result.getTotalElements());
        assertEquals("FOLLOWING", result.getContent().get(0).getFeedSource());
        verify(eventServiceClient, never()).getUpcomingPublicEvents(any());
    }

    @Test
    public void getFeed_followingType_noFollowing_returnsEmptyPage() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(Collections.emptyList());

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.FOLLOWING, PageRequest.of(0, 20), null);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    public void getFeed_followingType_multipleFollowedUsers_aggregatesEvents() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(List.of("alice", "carol"));
        when(eventServiceClient.getUpcomingEventsByCreator(eq("alice"), any())).thenReturn(List.of(event1));
        when(eventServiceClient.getUpcomingEventsByCreator(eq("carol"), any())).thenReturn(List.of(event3));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.FOLLOWING, PageRequest.of(0, 20), null);

        assertEquals(2, result.getTotalElements());
        result.getContent().forEach(e -> assertEquals("FOLLOWING", e.getFeedSource()));
    }

    // --- RECOMMENDED type ---

    @Test
    public void getFeed_recommendedType_onlyCallsPublicEvents() {
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(List.of(event1, event2));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.RECOMMENDED, PageRequest.of(0, 20), null);

        assertEquals(2, result.getTotalElements());
        result.getContent().forEach(e -> assertEquals("RECOMMENDED", e.getFeedSource()));
        verify(userServiceClient, never()).getFollowingUsernames(any(), any());
    }

    @Test
    public void getFeed_recommendedType_noPublicEvents_returnsEmptyPage() {
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Collections.emptyList());

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.RECOMMENDED, PageRequest.of(0, 20), null);

        assertEquals(0, result.getTotalElements());
    }

    // --- ALL type ---

    @Test
    public void getFeed_allType_mergesFollowingAndRecommended() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(List.of("alice"));
        when(eventServiceClient.getUpcomingEventsByCreator(eq("alice"), any())).thenReturn(List.of(event1));
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(List.of(event2, event3));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.ALL, PageRequest.of(0, 20), null);

        assertEquals(3, result.getTotalElements());
    }

    @Test
    public void getFeed_allType_deduplicatesEventsAppearingInBothSources() {
        // event1 comes from following AND from recommended public events
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(List.of("alice"));
        when(eventServiceClient.getUpcomingEventsByCreator(eq("alice"), any())).thenReturn(List.of(event1));
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(List.of(event1, event2));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.ALL, PageRequest.of(0, 20), null);

        assertEquals(2, result.getTotalElements());
        // event1 should be labeled FOLLOWING (seen first), event2 RECOMMENDED
        FeedEvent first = result.getContent().stream()
                .filter(e -> e.getId().equals(1L)).findFirst().orElseThrow();
        assertEquals("FOLLOWING", first.getFeedSource());
    }

    // --- Sorting ---

    @Test
    public void getFeed_sortsByStartTimeAscending() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(Collections.emptyList());
        // event2 is day+3, event3 is day+2 — should come back as event3, event2
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(event2, event3));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.RECOMMENDED, PageRequest.of(0, 20), null);

        List<FeedEvent> content = result.getContent();
        assertEquals(2, content.size());
        assertTrue(content.get(0).getStartTime().isBefore(content.get(1).getStartTime()));
        assertEquals(3L, content.get(0).getId()); // event3 is sooner
        assertEquals(2L, content.get(1).getId()); // event2 is later
    }

    // --- Pagination ---

    @Test
    public void getFeed_paginatesCorrectly_firstPage() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(Collections.emptyList());
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(event1, event3, event2));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.RECOMMENDED, PageRequest.of(0, 2), null);

        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    public void getFeed_paginatesCorrectly_secondPage() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(Collections.emptyList());
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(Arrays.asList(event1, event3, event2));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.RECOMMENDED, PageRequest.of(1, 2), null);

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    public void getFeed_pageOutOfBounds_returnsEmptyContent() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(Collections.emptyList());
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(List.of(event1));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.RECOMMENDED, PageRequest.of(5, 20), null);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // --- Client resilience ---

    @Test
    public void getFeed_userServiceClientReturnsEmpty_stillReturnsFeed() {
        when(userServiceClient.getFollowingUsernames(eq("bob"), any())).thenReturn(Collections.emptyList());
        when(eventServiceClient.getUpcomingPublicEvents(any())).thenReturn(List.of(event1));

        Page<FeedEvent> result = feedService.getFeed("bob", FeedType.ALL, PageRequest.of(0, 20), null);

        assertEquals(1, result.getTotalElements());
        assertEquals("RECOMMENDED", result.getContent().get(0).getFeedSource());
    }
}
