package com.eventmaster.controller;

import com.eventmaster.model.FeedEvent;
import com.eventmaster.model.FeedType;
import com.eventmaster.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/feed")
public class FeedController {

    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    @Autowired
    private FeedService feedService;

    @GetMapping
    public ResponseEntity<Page<FeedEvent>> getFeed(
            @RequestParam(defaultValue = "ALL") FeedType type,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication,
            HttpServletRequest request) {
        String username = authentication.getName();
        // Propagate the caller's JWT so user-service can authorise the /following lookup.
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        logger.debug("GET /feed type={} page={} size={} user={}", type, pageable.getPageNumber(), pageable.getPageSize(), username);
        return ResponseEntity.ok(feedService.getFeed(username, type, pageable, token));
    }
}
