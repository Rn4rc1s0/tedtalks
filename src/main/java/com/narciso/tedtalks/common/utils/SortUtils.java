package com.narciso.tedtalks.common.utils;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;

public class SortUtils {
    public static Sort createSort(String[] sort) {
        try {
            List<Sort.Order> orders = new ArrayList<>();
            if (sort != null) {
                for (String sortOrder : sort) {
                    String[] _sort = sortOrder.split(",");
                    if (_sort.length == 2) {
                        Sort.Direction direction = Sort.Direction.fromString(_sort[1]);
                        orders.add(new Sort.Order(direction, _sort[0]));
                    } else if (_sort.length == 1) {
                        orders.add(new Sort.Order(Sort.Direction.ASC, _sort[0]));
                    }
                }
            }

            return orders.isEmpty() ? Sort.by("name").ascending() : Sort.by(orders);

        } catch (Exception e) {
            return Sort.by("name").ascending();
        }
    }
}
