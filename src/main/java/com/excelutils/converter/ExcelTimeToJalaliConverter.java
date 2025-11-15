package com.excelutils.converter;

import com.excelutils.annotation.ExcelField;

import java.time.LocalDateTime;

/**
 * Convert LocalDateTime to Jalali (Persian) formatted string.
 * Version 1 - Original implementation.
 */
public class ExcelTimeToJalaliConverter implements ExcelField.FieldConverter {
    @Override
    public String convert(Object value) {
        if (value == null) {
            return "";
        }
        return JalaliUtils.generatePersianDate((LocalDateTime) value);
    }
}