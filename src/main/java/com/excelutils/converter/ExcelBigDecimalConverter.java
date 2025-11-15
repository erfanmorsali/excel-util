package com.excelutils.converter;

import com.excelutils.annotation.ExcelField;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Convert BigDecimal to rounded string (no fraction).
 * Version 1 - Original implementation.
 */
public class ExcelBigDecimalConverter implements ExcelField.FieldConverter {
    @Override
    public String convert(Object value) {
        if (value == null) {
            return "";
        }
        BigDecimal decimalValue = (BigDecimal) value;
        if (decimalValue.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        } else {
            return decimalValue.setScale(0, RoundingMode.HALF_UP).toString();
        }
    }
}