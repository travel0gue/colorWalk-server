package com.example.color_walk.domain;

import lombok.Getter;

@Getter
public enum Color {
    RED("빨강"),
    BLUE("파랑"),
    YELLOW("노랑"),
    GREEN("초록"),
    PURPLE("보라"),
    ORANGE("주황"),
    PINK("분홍"),
    BLACK("검정"),
    WHITE("하양"),
    BROWN("갈색");

    private final String koreanName;

    Color(String koreanName) {
        this.koreanName = koreanName;
    }

    // 오늘의 컬러 랜덤 선택 (컬러링 동대문 일일 미션용)
    public static Color getTodaysColor() {
        Color[] colors = values();
        return colors[(int) (Math.random() * colors.length)];
    }
}
