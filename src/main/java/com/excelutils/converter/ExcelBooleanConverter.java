package com.excelutils.converter;

import com.excelutils.annotation.ExcelField;

public class ExcelBooleanConverter implements ExcelField.FieldConverter {

    @Override
    public String convert(Object value) {
        if (value == null) {
            return "";
        }

        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException(
                String.format("Expected Boolean but got %s", value.getClass().getName())
            );
        }

        return (Boolean) value ? "بله" : "خیر";
    }
}
