package com.excelutils.exception;

/**
 * Exception thrown when field conversion fails during Excel export.
 */
public class ConverterException extends ExcelExportException {

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
}
