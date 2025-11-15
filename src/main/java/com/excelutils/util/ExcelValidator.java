package com.excelutils.util;

import com.excelutils.annotation.ExcelField;
import com.excelutils.exception.ValidationException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

public class ExcelValidator {

    private static final int MAX_EXCEL_ROWS = 1_048_576;
    private static final int MAX_EXCEL_COLUMNS = 16_384;
    private static final int MAX_SHEET_NAME_LENGTH = 31;
    private static final Pattern INVALID_SHEET_NAME_CHARS = Pattern.compile("[\\\\/:*?\\[\\]]");

    public static <T> void validateDataNotEmpty(List<T> data) throws ValidationException {
        if (data == null) {
            throw new ValidationException("Data list cannot be null");
        }
        if (data.isEmpty()) {
            throw new ValidationException("Data list cannot be empty");
        }
    }

    public static <T> void validateDataMapNotEmpty(Map<String, List<T>> data) throws ValidationException {
        if (data == null) {
            throw new ValidationException("Data map cannot be null");
        }
        if (data.isEmpty()) {
            throw new ValidationException("Data map cannot be empty - no sheets to export");
        }
    }

    public static void validateSheetName(String sheetName) throws ValidationException {
        if (sheetName == null || sheetName.trim().isEmpty()) {
            throw new ValidationException("Sheet name cannot be null or empty");
        }
        if (sheetName.length() > MAX_SHEET_NAME_LENGTH) {
            throw new ValidationException(
                String.format("Sheet name '%s' exceeds maximum length of %d characters",
                    sheetName, MAX_SHEET_NAME_LENGTH)
            );
        }
        if (INVALID_SHEET_NAME_CHARS.matcher(sheetName).find()) {
            throw new ValidationException(
                String.format("Sheet name '%s' contains invalid characters. Cannot use: \ / : * ? [ ]",
                    sheetName)
            );
        }
    }

    public static void validateRowCount(int rowCount) throws ValidationException {
        if (rowCount > MAX_EXCEL_ROWS) {
            throw new ValidationException(
                String.format("Row count %d exceeds Excel maximum of %d rows",
                    rowCount, MAX_EXCEL_ROWS)
            );
        }
    }

    public static void validateColumnCount(int columnCount) throws ValidationException {
        if (columnCount > MAX_EXCEL_COLUMNS) {
            throw new ValidationException(
                String.format("Column count %d exceeds Excel maximum of %d columns",
                    columnCount, MAX_EXCEL_COLUMNS)
            );
        }
    }

    public static void validateFieldOrders(List<Field> fields) throws ValidationException {
        Set<Integer> orderValues = new HashSet<>();

        for (Field field : fields) {
            ExcelField annotation = field.getAnnotation(ExcelField.class);
            if (annotation != null) {
                int order = annotation.order();

                if (order < 0) {
                    throw new ValidationException(
                        String.format("Field '%s' has negative order value: %d",
                            field.getName(), order)
                    );
                }

                if (order != Integer.MAX_VALUE && orderValues.contains(order)) {
                    throw new ValidationException(
                        String.format("Duplicate order value %d found. Field orders must be unique.",
                            order)
                    );
                }

                orderValues.add(order);
            }
        }
    }

    public static <T> void validateConsistentTypes(List<T> data) throws ValidationException {
        if (data == null || data.isEmpty()) {
            return;
        }

        Class<?> expectedType = null;
        for (int i = 0; i < data.size(); i++) {
            T item = data.get(i);
            if (item == null) {
                throw new ValidationException(
                    String.format("Null item found at index %d. All data items must be non-null.", i)
                );
            }

            if (expectedType == null) {
                expectedType = item.getClass();
            } else if (!expectedType.equals(item.getClass())) {
                throw new ValidationException(
                    String.format("Inconsistent types in data list. Expected %s but found %s at index %d",
                        expectedType.getName(), item.getClass().getName(), i)
                );
            }
        }
    }
}
