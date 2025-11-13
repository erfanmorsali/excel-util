package com.excelutils.converter;

import com.excelutils.annotation.ExcelField;
import com.excelutils.converter.JalaliUtils;

import java.time.LocalDateTime;

/**
 * Convert LocalDateTime to Jalali (Persian) formatted string.
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
