package com.example.color_walk.dto.request;

import com.example.color_walk.domain.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GachaRequest {
    private Long memberId;
    private Color selectedColor; // enum으로 직접 받기
}
