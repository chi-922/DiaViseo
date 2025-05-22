package com.s206.health.nutrition.foodset.dto.response;

import com.s206.health.nutrition.foodset.entity.FoodSet;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class FoodSetDetailResponse {

    private Integer foodSetId;
    private String name;
    private List<FoodItem> foods;
    private Integer totalCalories; // 음식 세트의 종 칼로리 계산

    @Getter
    @Builder
    public static class FoodItem {
        private Integer foodId;
        private String foodName;
        private Float quantity;
        private BigDecimal carbohydrate;
        private BigDecimal protein;
        private BigDecimal fat;
        private BigDecimal sweet;
        private BigDecimal sodium;
        private String baseAmount;
        private Integer calorie;
    }

    public static FoodSetDetailResponse toDto(FoodSet foodSet) {
        List<FoodItem> foodItems = foodSet.getFoodSetFoods().stream()
                .map(f -> FoodItem.builder()
                        .foodId(f.getFood().getFoodId())
                        .foodName(f.getFood().getFoodName())
                        .quantity(f.getQuantity())
                        .carbohydrate(f.getFood().getCarbohydrate())
                        .protein(f.getFood().getProtein())
                        .fat(f.getFood().getFat())
                        .sweet(f.getFood().getSweet())
                        .sodium(f.getFood().getSodium())
                        .baseAmount(f.getFood().getBaseAmount())
                        .calorie(f.getFood().getCalorie())
                        .build())
                .toList();

        int totalCalories = foodSet.getFoodSetFoods().stream()
                .mapToInt(f -> (int) (f.getFood().getCalorie() * f.getQuantity()))
                .sum();

        return FoodSetDetailResponse.builder()
                .foodSetId(foodSet.getFoodSetId())
                .name(foodSet.getName())
                .foods(foodItems)
                .totalCalories(totalCalories)
                .build();
    }
}
