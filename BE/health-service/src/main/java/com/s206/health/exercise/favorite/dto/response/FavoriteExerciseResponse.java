package com.s206.health.exercise.favorite.dto.response;

import com.s206.health.exercise.favorite.entity.FavoriteExercise;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteExerciseResponse {
    private Integer exerciseTypeId;
    private Integer exerciseNumber;
    private String exerciseName;
    private Integer calorie;
    private String categoryName;
    private LocalDateTime createdAt;

    public static FavoriteExerciseResponse toDto(FavoriteExercise favorite, String categoryName) {
        return FavoriteExerciseResponse.builder()
                .exerciseTypeId(favorite.getExerciseType().getExerciseTypeId())
                .exerciseNumber(favorite.getExerciseType().getExerciseNumber())
                .exerciseName(favorite.getExerciseType().getExerciseName())
                .calorie(favorite.getExerciseType().getExerciseCalorie())
                .categoryName(categoryName)
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
