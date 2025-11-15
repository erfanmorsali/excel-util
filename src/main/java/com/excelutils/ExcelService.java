package com.excelutils;

import com.excelutils.annotation.ExcelField;
import com.excelutils.config.ExcelConfiguration;
import com.excelutils.exception.ConverterException;
import com.excelutils.exception.ExcelExportException;
import com.excelutils.util.ExcelValidator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Excel export service - Version 2.
 * Improved with converter caching, validation, and better error handling.
 */
public class ExcelService {

    private final ExcelConfiguration configuration;
    private final Map<Class<? extends ExcelField.FieldConverter>, ExcelField.FieldConverter> converterCache;

    public ExcelService() {
        this(ExcelConfiguration.getDefault());
    }

    public ExcelService(ExcelConfiguration configuration) {
        this.configuration = configuration;
        this.converterCache = new ConcurrentHashMap<>();
    }

    public <T> byte[] exportToExcel(List<T> data) throws ExcelExportException {
        Map<String, List<T>> map = new HashMap<>();
        map.put(configuration.getDefaultSheetName(), data);
        return exportToExcelWithMultipleSheets(map);
    }

    public <T> byte[] exportToExcelWithMultipleSheets(Map<String, List<T>> data) throws ExcelExportException {
        ExcelValidator.validateDataMapNotEmpty(data);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            for (Map.Entry<String, List<T>> entry : data.entrySet()) {
                String sheetName = entry.getKey();
                List<T> sheetData = entry.getValue();

                if (sheetData == null || sheetData.isEmpty()) {
                    continue;
                }

                ExcelValidator.validateSheetName(sheetName);
                ExcelValidator.validateConsistentTypes(sheetData);
                ExcelValidator.validateRowCount(sheetData.size() + 1);

                Class<?> clazz = sheetData.get(0).getClass();
                List<FieldMetadata> fieldMetadata = getAnnotatedFieldsMetadata(clazz);

                ExcelValidator.validateColumnCount(fieldMetadata.size());
                List<Field> fields = fieldMetadata.stream()
                        .map(fm -> fm.field)
                        .collect(Collectors.toList());
                ExcelValidator.validateFieldOrders(fields);

                Sheet sheet = workbook.createSheet(sheetName);
                sheet.setRightToLeft(configuration.isRightToLeft());

                createHeaderRow(sheet, fieldMetadata);
                populateDataRows(sheet, sheetData, fieldMetadata);

                if (configuration.isAutoResize()) {
                    autoSizeColumns(sheet, fieldMetadata.size());
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new ExcelExportException("Failed to write Excel workbook", e);
        } catch (Exception e) {
            throw new ExcelExportException("Unexpected error during Excel export", e);
        }
    }

    public <T> Workbook exportToWorkbook(Map<String, List<T>> data) throws ExcelExportException {
        ExcelValidator.validateDataMapNotEmpty(data);

        try {
            Workbook workbook = new XSSFWorkbook();

            for (Map.Entry<String, List<T>> entry : data.entrySet()) {
                String sheetName = entry.getKey();
                List<T> sheetData = entry.getValue();

                if (sheetData == null || sheetData.isEmpty()) {
                    continue;
                }

                ExcelValidator.validateSheetName(sheetName);
                ExcelValidator.validateConsistentTypes(sheetData);
                ExcelValidator.validateRowCount(sheetData.size() + 1);

                Class<?> clazz = sheetData.get(0).getClass();
                List<FieldMetadata> fieldMetadata = getAnnotatedFieldsMetadata(clazz);

                ExcelValidator.validateColumnCount(fieldMetadata.size());
                List<Field> fields = fieldMetadata.stream()
                        .map(fm -> fm.field)
                        .collect(Collectors.toList());
                ExcelValidator.validateFieldOrders(fields);

                Sheet sheet = workbook.createSheet(sheetName);
                sheet.setRightToLeft(configuration.isRightToLeft());

                createHeaderRow(sheet, fieldMetadata);
                populateDataRows(sheet, sheetData, fieldMetadata);

                if (configuration.isAutoResize()) {
                    autoSizeColumns(sheet, fieldMetadata.size());
                }
            }

            return workbook;

        } catch (Exception e) {
            throw new ExcelExportException("Failed to create Excel workbook", e);
        }
    }

    private List<FieldMetadata> getAnnotatedFieldsMetadata(Class<?> clazz) {
        return Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ExcelField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(ExcelField.class).order()))
                .map(field -> {
                    field.setAccessible(true);
                    ExcelField annotation = field.getAnnotation(ExcelField.class);
                    return new FieldMetadata(field, annotation);
                })
                .collect(Collectors.toList());
    }

    private void createHeaderRow(Sheet sheet, List<FieldMetadata> fieldMetadata) {
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < fieldMetadata.size(); i++) {
            FieldMetadata metadata = fieldMetadata.get(i);
            String headerName = metadata.annotation.name().isEmpty()
                    ? metadata.field.getName()
                    : metadata.annotation.name();

            headerRow.createCell(i).setCellValue(headerName);
        }
    }

    private <T> void populateDataRows(
            Sheet sheet,
            List<T> data,
            List<FieldMetadata> fieldMetadata) throws ConverterException {

        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            T item = data.get(rowIndex);

            for (int colIndex = 0; colIndex < fieldMetadata.size(); colIndex++) {
                FieldMetadata metadata = fieldMetadata.get(colIndex);

                try {
                    Object value = metadata.field.get(item);
                    ExcelField.FieldConverter converter = getConverter(metadata.annotation.converter());
                    String cellValue = converter.convert(value);
                    row.createCell(colIndex).setCellValue(cellValue);

                } catch (IllegalAccessException e) {
                    throw new ConverterException(
                            String.format("Failed to access field '%s' at row %d",
                                    metadata.field.getName(), rowIndex + 1),
                            e
                    );
                } catch (IllegalArgumentException e) {
                    throw new ConverterException(
                            String.format("Type mismatch in field '%s' at row %d: %s",
                                    metadata.field.getName(), rowIndex + 1, e.getMessage()),
                            e
                    );
                } catch (Exception e) {
                    throw new ConverterException(
                            String.format("Conversion failed for field '%s' at row %d",
                                    metadata.field.getName(), rowIndex + 1),
                            e
                    );
                }
            }
        }
    }

    private ExcelField.FieldConverter getConverter(
            Class<? extends ExcelField.FieldConverter> converterClass) {

        return converterCache.computeIfAbsent(converterClass, clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format("Failed to instantiate converter: %s", clazz.getName()),
                        e
                );
            }
        });
    }

    private void autoSizeColumns(Sheet sheet, int numberOfColumns) {
        if (!configuration.isParallelResize() || numberOfColumns < 5) {
            for (int col = 0; col < numberOfColumns; col++) {
                sheet.autoSizeColumn(col);
            }
            return;
        }

        int numThreads = Math.min(
                configuration.getMaxThreadsForResize(),
                numberOfColumns
        );

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Void>> futures = new ArrayList<>();

        int chunkSize = (int) Math.ceil((double) numberOfColumns / numThreads);

        for (int i = 0; i < numberOfColumns; i += chunkSize) {
            final int start = i;
            final int end = Math.min(i + chunkSize, numberOfColumns);

            Future<Void> future = executor.submit(() -> {
                for (int col = start; col < end; col++) {
                    sheet.autoSizeColumn(col);
                }
                return null;
            });

            futures.add(future);
        }

        try {
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Column resizing was interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Column resizing failed", e.getCause());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public ExcelConfiguration getConfiguration() {
        return configuration;
    }

    public void clearConverterCache() {
        converterCache.clear();
    }

    private record FieldMetadata(Field field, ExcelField annotation) {
    }
}
