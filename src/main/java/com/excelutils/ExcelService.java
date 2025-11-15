package com.excelutils;

import com.excelutils.annotation.ExcelField;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Excel export service - Version 1 (Original).
 * Simple and straightforward Excel export functionality.
 */
public class ExcelService {

    public <T> byte[] exportToExcelWithMultipleSheets(Map<String, List<T>> data, boolean resizeColumns) throws Exception {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data map is empty");
        }
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (String sheetName : data.keySet()) {
                List<T> sheetData = data.get(sheetName);
                if (sheetData == null || sheetData.isEmpty()) {
                    continue;
                }
                Class<?> clazz = sheetData.get(0).getClass();
                List<Field> fields = getAnnotatedFields(clazz);

                Sheet sheet = workbook.createSheet(sheetName);
                sheet.setRightToLeft(true);
                createHeaderRow(sheet, fields);
                populateDataRows(sheet, sheetData, fields);

                if (resizeColumns) {
                    autoSizeColumns(sheet, fields.size());
                }
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public <T> byte[] exportToExcel(List<T> data, boolean resizeColumns) throws Exception {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data list is empty");
        }
        Map<String, List<T>> map = new HashMap<>();
        map.put("sheet1", data);
        return exportToExcelWithMultipleSheets(map, resizeColumns);
    }

    private List<Field> getAnnotatedFields(Class<?> clazz) {
        return Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ExcelField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(ExcelField.class).order()))
                .collect(Collectors.toList());
    }

    private void createHeaderRow(Sheet sheet, List<Field> fields) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            ExcelField excelField = field.getAnnotation(ExcelField.class);
            String headerName = excelField.name().isEmpty() ? field.getName() : excelField.name();
            headerRow.createCell(i).setCellValue(headerName);
        }
    }

    private <T> void populateDataRows(Sheet sheet, List<T> data, List<Field> fields) throws Exception {
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            T item = data.get(i);
            for (int j = 0; j < fields.size(); j++) {
                Field field = fields.get(j);
                field.setAccessible(true);
                Object value = field.get(item);
                ExcelField excelField = field.getAnnotation(ExcelField.class);

                // Use the converter if specified
                ExcelField.FieldConverter converter =
                        excelField.converter().getDeclaredConstructor().newInstance();
                String cellValue = converter.convert(value);
                row.createCell(j).setCellValue(cellValue);
            }
        }
    }

    private void autoSizeColumns(Sheet sheet, int numberOfColumns) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Callable<Void>> tasks = new ArrayList<>();

        int chunkSize = (int) Math.ceil((double) numberOfColumns / numThreads);
        for (int i = 0; i < numberOfColumns; i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, numberOfColumns);
            tasks.add(() -> {
                for (int col = start; col < end; col++) {
                    sheet.autoSizeColumn(col);
                }
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }
}