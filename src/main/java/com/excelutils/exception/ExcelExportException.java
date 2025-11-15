package com.excelutils.exception;

/**
 * Exception thrown when Excel export operations fail.
 */
public class ExcelExportException extends Exception {

    public ExcelExportException(String message) {
        super(message);
    }

    public ExcelExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
