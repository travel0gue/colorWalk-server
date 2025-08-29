package com.example.color_walk.dto.response;

public class ColorAnalysisResponseWithPoint {
    ColorAnalysisResponse response;
    Integer point;

    public ColorAnalysisResponseWithPoint(ColorAnalysisResponse response, Integer gainedPoint) {
        this.response = response;
        this.point = gainedPoint;
    }
}
