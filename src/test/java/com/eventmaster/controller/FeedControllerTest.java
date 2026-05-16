package com.eventmaster.controller;

import com.eventmaster.exception.GlobalExceptionHandler;
import com.eventmaster.model.FeedEvent;
import com.eventmaster.model.FeedType;
import com.eventmaster.service.FeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FeedControllerTest {

    @Mock
    private FeedService feedService;

    @InjectMocks
    private FeedController feedController;

    private MockMvc mockMvc;
    private FeedEvent sampleEvent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(feedController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleEvent = new FeedEvent();
        sampleEvent.setId(1L);
        sampleEvent.setTitle("Music Night");
        sampleEvent.setCreatorUsername("alice");
        sampleEvent.setStartTime(LocalDateTime.now().plusDays(1));
        sampleEvent.setFeedSource("FOLLOWING");
    }

    private RequestPostProcessor auth(String username) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("VERIFIED")));
        return (MockHttpServletRequest req) -> { req.setUserPrincipal(token); return req; };
    }

    // --- GET /feed ---

    @Test
    public void getFeed_authenticated_returns200WithPage() throws Exception {
        Page<FeedEvent> page = new PageImpl<>(List.of(sampleEvent));
        when(feedService.getFeed(eq("bob"), eq(FeedType.ALL), any(Pageable.class), any())).thenReturn(page);

        mockMvc.perform(get("/feed").with(auth("bob")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Music Night"))
                .andExpect(jsonPath("$.content[0].feedSource").value("FOLLOWING"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    public void getFeed_defaultTypeIsAll() throws Exception {
        when(feedService.getFeed(eq("bob"), eq(FeedType.ALL), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/feed").with(auth("bob")))
                .andExpect(status().isOk());

        verify(feedService).getFeed(eq("bob"), eq(FeedType.ALL), any(Pageable.class), any());
    }

    @Test
    public void getFeed_typeFollowing_passesFollowingToService() throws Exception {
        when(feedService.getFeed(eq("bob"), eq(FeedType.FOLLOWING), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(sampleEvent)));

        mockMvc.perform(get("/feed").param("type", "FOLLOWING").with(auth("bob")))
                .andExpect(status().isOk());

        verify(feedService).getFeed(eq("bob"), eq(FeedType.FOLLOWING), any(Pageable.class), any());
    }

    @Test
    public void getFeed_typeRecommended_passesRecommendedToService() throws Exception {
        when(feedService.getFeed(eq("bob"), eq(FeedType.RECOMMENDED), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/feed").param("type", "RECOMMENDED").with(auth("bob")))
                .andExpect(status().isOk());

        verify(feedService).getFeed(eq("bob"), eq(FeedType.RECOMMENDED), any(Pageable.class), any());
    }

    @Test
    public void getFeed_paginationParams_passedToService() throws Exception {
        when(feedService.getFeed(any(), any(), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/feed").param("page", "2").param("size", "5").with(auth("bob")))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(feedService).getFeed(any(), any(), pageableCaptor.capture(), any());
        assertEquals(2, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }

    @Test
    public void getFeed_emptyFeed_returns200WithEmptyContent() throws Exception {
        when(feedService.getFeed(any(), any(), any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/feed").with(auth("bob")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    public void getFeed_multipleEvents_returnsAllInContent() throws Exception {
        FeedEvent event2 = new FeedEvent();
        event2.setId(2L);
        event2.setTitle("Jazz Night");
        event2.setFeedSource("RECOMMENDED");

        Page<FeedEvent> page = new PageImpl<>(Arrays.asList(sampleEvent, event2));
        when(feedService.getFeed(any(), any(), any(Pageable.class), any())).thenReturn(page);

        mockMvc.perform(get("/feed").with(auth("bob")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Music Night"))
                .andExpect(jsonPath("$.content[1].title").value("Jazz Night"));
    }

    private void assertEquals(int expected, int actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
