package com.example.color_walk.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PhotoUrlsRequest {
    private List<String> urls;

    public PhotoUrlsRequest() {}

    public PhotoUrlsRequest(List<String> urls) {
        this.urls = urls;
    }

}