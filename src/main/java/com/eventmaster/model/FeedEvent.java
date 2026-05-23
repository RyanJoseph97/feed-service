package com.eventmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedEvent {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
    private String creatorUsername;
    private String visibility;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String feedSource;
    private Integer likeCount;
    private Integer rsvpCount;
    private Integer goingCount;
    private Integer interestedCount;
    private Integer notGoingCount;
    private String recurrenceType;
    private String recurrenceEndDate;
    private String category;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFeedSource() { return feedSource; }
    public void setFeedSource(String feedSource) { this.feedSource = feedSource; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getRsvpCount() { return rsvpCount; }
    public void setRsvpCount(Integer rsvpCount) { this.rsvpCount = rsvpCount; }

    public Integer getGoingCount() { return goingCount; }
    public void setGoingCount(Integer goingCount) { this.goingCount = goingCount; }

    public Integer getInterestedCount() { return interestedCount; }
    public void setInterestedCount(Integer interestedCount) { this.interestedCount = interestedCount; }

    public Integer getNotGoingCount() { return notGoingCount; }
    public void setNotGoingCount(Integer notGoingCount) { this.notGoingCount = notGoingCount; }

    public String getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }

    public String getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(String recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
