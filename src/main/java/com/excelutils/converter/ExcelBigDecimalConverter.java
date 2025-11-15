package com.excelutils.converter;

import com.excelutils.annotation.ExcelField;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExcelBigDecimalConverter implements ExcelField.FieldConverter {

    @Override
    public String convert(Object value) {
        if (value == null) {
            return "";
        }

        if (!(value instanceof BigDecimal decimalValue)) {
            throw new IllegalArgumentException(
                String.format("Expected BigDecimal but got %s", value.getClass().getName())
            );
        }

        if (decimalValue.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        return decimalValue.setScale(0, RoundingMode.HALF_UP).toString();
    }
}
