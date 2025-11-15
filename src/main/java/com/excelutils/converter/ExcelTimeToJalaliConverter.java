package com.excelutils.converter;

import com.excelutils.annotation.ExcelField;
import com.excelutils.config.ExcelConfiguration;
import com.excelutils.util.ConfigurableJalaliUtils;

import java.time.LocalDateTime;

public record ExcelTimeToJalaliConverter(String dateFormat, String timezone,
                                         String locale) implements ExcelField.FieldConverter {

    public ExcelTimeToJalaliConverter() {
        this("yyyy/MM/dd HH:mm:ss", "Asia/Tehran", "fa_IR");
    }

    public ExcelTimeToJalaliConverter(ExcelConfiguration config) {
        this(config.getDateFormat(), config.getTimezone(), config.getLocale());
    }

    @Override
    public String convert(Object value) {
        if (value == null) {
            return "";
        }

        if (!(value instanceof LocalDateTime)) {
            throw new IllegalArgumentException(
                String.format("Expected LocalDateTime but got %s", value.getClass().getName())
            );
        }

        return ConfigurableJalaliUtils.generatePersianDate(
                (LocalDateTime) value,
                dateFormat,
                timezone,
                locale
        );
    }
}
