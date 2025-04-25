package com.narciso.tedtalks.speakers.strategy;

import java.math.BigDecimal;

public interface InfluenceStrategy {
    BigDecimal score(long totalViews, long totalLikes);
}
