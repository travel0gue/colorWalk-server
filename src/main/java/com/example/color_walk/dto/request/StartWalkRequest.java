package com.example.color_walk.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StartWalkRequest {
    @NotNull(message = "멤버 ID는 필수입니다")
    private Long memberId;

    @NotNull(message = "제목은 필수입니다")
    private String title;

    private String content;
    private String colorTheme;
}
