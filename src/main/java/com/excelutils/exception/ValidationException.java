package com.excelutils.exception;

/**
 * Exception thrown when validation fails during Excel export.
 */
public class ValidationException extends ExcelExportException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
