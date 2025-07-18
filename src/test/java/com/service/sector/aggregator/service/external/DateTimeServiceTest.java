package com.service.sector.aggregator.service.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeServiceTest {

    private final DateTimeService service = new DateTimeService();

    @Test
    @DisplayName("Converts the full week into a 7-bit mask (0b111_1111 = 127)")
    void toMask_withAllDays_returnsFullMask() {
        List<String> allDays = List.of(
                "MONDAY", "TUESDAY", "WEDNESDAY",
                "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        );

        short expected = 0b111_1111;   // 127
        short actual   = service.toMask(allDays);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Converts a single day into the correct bit")
    void toMask_withSingleDay_returnsCorrectBit() {
        assertEquals(0b000_0001, service.toMask(List.of("MONDAY")));  // 1
        assertEquals(0b100_0000, service.toMask(List.of("SUNDAY")));  // 64
    }

    @Test
    @DisplayName("Is case-insensitive")
    void toMask_isCaseInsensitive() {
        short expected = 0b100_0000;  // Sunday bit
        short actual   = service.toMask(List.of("sUnDaY"));

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ignores duplicate day names")
    void toMask_ignoresDuplicates() {
        short expected = 0b000_0001;  // Monday bit
        short actual   = service.toMask(List.of("MONDAY", "monday", "MONDAY"));

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Returns 0 for an empty list")
    void toMask_withEmptyList_returnsZero() {
        assertEquals(0, service.toMask(List.of()));
    }

    @Test
    @DisplayName("Throws IllegalArgumentException for an invalid day")
    void toMask_withInvalidDay_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.toMask(List.of("FUNDAY")));
    }
}