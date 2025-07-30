package com.alness.lifemaster.nutrition.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "food")
@Data
public class FoodEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "food_name", nullable = false, columnDefinition = "character varying(256)")
    private String foodName;

    @Column(name = "calories", nullable = true, columnDefinition = "bigint")
    private Integer calories;

    @Column(name = "unit_measurement", nullable = true, columnDefinition = "character varying(128)")
    private String unitMeasurement;

    @Column(name = "quantity", nullable = false, columnDefinition = "character varying(256)")
    private String quantity;

    @ManyToOne
    @JoinColumn(name = "nutrition_id", nullable = true)
    private NutritionEntity nutrition;
}
