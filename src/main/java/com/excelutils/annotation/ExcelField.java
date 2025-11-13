package com.excelutils.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark fields for Excel export.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelField {
    String name() default "";
    int order() default Integer.MAX_VALUE;
    Class<? extends FieldConverter> converter() default DefaultConverter.class;

    interface FieldConverter {
        String convert(Object value);
    }

    class DefaultConverter implements FieldConverter {
        @Override
        public String convert(Object value) {
            return value != null ? value.toString() : "";
        }
    }
}
