package com.narciso.tedtalks.speakers.strategy;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.MathContext;

@Component("compositeStrategy")
public class CompositeStrategy implements InfluenceStrategy {
    @Override
    public BigDecimal score(long totalViews, long totalLikes) {
        BigDecimal normViews = BigDecimal.valueOf(Math.log1p(totalViews));
        BigDecimal normLikes = BigDecimal.valueOf(Math.log1p(totalLikes));
        return normViews.add(normLikes, MathContext.DECIMAL64);
    }
}
