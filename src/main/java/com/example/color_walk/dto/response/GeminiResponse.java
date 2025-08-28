package com.example.color_walk.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GeminiResponse {
    private List<Candidate> candidates;

    @Setter
    @Getter
    public static class Candidate {
        private Content content;

    }

    @Setter
    @Getter
    public static class Content {
        private List<Part> parts;

    }

    @Setter
    @Getter
    public static class Part {
        private String text;

    }
}
