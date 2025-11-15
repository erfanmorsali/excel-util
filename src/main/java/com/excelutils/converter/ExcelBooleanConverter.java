package com.excelutils.converter;

import com.excelutils.annotation.ExcelField;

/**
 * Convert Boolean to Persian Yes/No.
 * Version 1 - Original implementation.
 */
public class ExcelBooleanConverter implements ExcelField.FieldConverter {
    @Override
    public String convert(Object value) {
        if (value == null) {
            return "";
        }
        return (Boolean) value ? "بله" : "خیر";
    }
}