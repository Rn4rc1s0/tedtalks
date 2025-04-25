package com.narciso.tedtalks.speakers.strategy;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Primary
@Component
public class WeightedStrategy implements InfluenceStrategy {
    private static final BigDecimal VIEW_WEIGHT = BigDecimal.valueOf(0.7);
    private static final BigDecimal LIKE_WEIGHT = BigDecimal.valueOf(0.3);

    @Override
    public BigDecimal score(long totalViews, long totalLikes) {
        return BigDecimal.valueOf(totalViews).multiply(VIEW_WEIGHT)
                .add(BigDecimal.valueOf(totalLikes).multiply(LIKE_WEIGHT));
    }
}
