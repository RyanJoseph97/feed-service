package com.eventmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageContent<T> {

    private List<T> content = new ArrayList<>();

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
}
