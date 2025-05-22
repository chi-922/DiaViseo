package com.s206.health.exercise.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_category_tb")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_category_id", nullable = false)
    private Integer exerciseCategoryId;

    @Column(name = "exercise_category_name", nullable = false, length = 50)
    private String exerciseCategoryName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

}
