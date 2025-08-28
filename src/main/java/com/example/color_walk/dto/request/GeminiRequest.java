package com.example.color_walk.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GeminiRequest {
    private List<Content> contents;

    public GeminiRequest(List<Content> contents) {
        this.contents = contents;
    }

    @Setter
    @Getter
    public static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }

    }

    @Setter
    @Getter
    public static class Part {
        private String text;
        @JsonProperty("inline_data")
        private InlineData inlineData;

        public Part(String text) {
            this.text = text;
        }

        public Part(InlineData inlineData) {
            this.inlineData = inlineData;
        }

    }

    @Setter
    @Getter
    public static class InlineData {
        @JsonProperty("mime_type")
        private String mimeType;
        private String data;

        public InlineData(String mimeType, String data) {
            this.mimeType = mimeType;
            this.data = data;
        }

    }
}
