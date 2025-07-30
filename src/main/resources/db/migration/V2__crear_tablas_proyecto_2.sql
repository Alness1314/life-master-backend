CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notes TEXT,
    training_date DATE NOT NULL,
    start_time TIME WITHOUT TIME ZONE,
    end_time TIME WITHOUT TIME ZONE,
    activity_type VARCHAR(128),
    duration_minutes BIGINT,
    user_id UUID NOT NULL,
    create_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    erased BOOLEAN NOT NULL,
    
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE nutrition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_time_consumption TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    meal_type VARCHAR(128),
    notes TEXT,
    user_id UUID NOT NULL,
    create_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    erased BOOLEAN NOT NULL,

    CONSTRAINT fk_nutrition_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE food (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    food_name VARCHAR(256) NOT NULL,
    calories BIGINT,
    unit_measurement VARCHAR(128),
    quantity VARCHAR(256) NOT NULL,
    nutrition_id UUID,

    CONSTRAINT fk_food_nutrition FOREIGN KEY (nutrition_id) REFERENCES nutrition(id) ON DELETE CASCADE
);