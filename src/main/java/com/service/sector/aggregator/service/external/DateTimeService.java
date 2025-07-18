package com.service.sector.aggregator.service.external;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class DateTimeService {

    /**  bit 0 → Monday, … bit 6 → Sunday  */
    public short toMask(List<String> days) {
        short mask = 0;
        for (String d : days) {
            DayOfWeek dow = DayOfWeek.valueOf(d.toUpperCase());
            mask |= 1 << (dow.getValue() - 1);
        }
        return mask;
    }
}
