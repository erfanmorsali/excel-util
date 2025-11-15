# excel-utils

A small standalone Java library to convert annotated DTOs into Excel files (XLSX).

## Quick start

Add the built JAR to your project (install to local repo with `mvn install`) or publish to your internal repository.

### Build locally

```bash
mvn clean package
```

This produces `target/excel-utils-1.0.0.jar`.

### Usage

Annotate DTO fields with `@ExcelField` to include them in the exported Excel and control column name / order / converter.

```java
import com.excelutils.annotation.ExcelField;

public class UserDto {
    @ExcelField(name = "شناسه", order = 1)
    private Long id;

    @ExcelField(name = "نام", order = 2)
    private String name;

    @ExcelField(name = "فعال", order = 3, converter = com.excelutils.converter.ExcelBooleanConverter.class)
    private Boolean active;

    // getters/setters
}
```

Then call `ExcelService`:

```java

import java.util.List;
import java.io.FileOutputStream;

List<UserDto> users = ...;

ExcelService service = new ExcelService();
byte[] excelBytes = service.exportToExcel(users, true);

try(
FileOutputStream fos = new FileOutputStream("users.xlsx")){
        fos.

write(excelBytes);
}
```

You can export multiple sheets:

```java
Map<String, List<Object>> sheets = new HashMap<>();
sheets.put("users", userList);
sheets.put("orders", orderList);

byte[] zip = service.exportToExcelWithMultipleSheets(sheets, true);
```

### Converters

The library ships with a few built-in converters:
- `com.excelutils.converter.ExcelBooleanConverter` — converts `Boolean` into Persian "بله"/"خیر".
- `com.excelutils.converter.ExcelBigDecimalConverter` — rounds BigDecimal to 0 scale, returns "0" for zero.
- `com.excelutils.converter.ExcelTimeToJalaliConverter` — converts `LocalDateTime` to a Persian (Jalali) formatted string using ICU4J.

You may implement your own converter by implementing `ExcelField.FieldConverter` and referencing it in the annotation.

### Notes

- The library depends on Apache POI (`poi-ooxml`) and ICU4J (`icu4j`). These are declared in `pom.xml`.
- The default date format used by the Jalali converter is `yyyy/MM/dd HH:mm:ss` and timezone `Asia/Tehran`.
- This is a lightweight utility and does not use Spring — it's a pure Java library.

## License

(Choose a license when publishing.)
