package com.excelutils.converter;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Utility for generating Persian (Jalali) formatted date strings using ICU4J.
 */
public class JalaliUtils {

    public static final String PERSIAN_LOCALE_ID = "fa_IR";
    public static final String PERSIAN_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String PERSIAN_ZONE_ID = "Asia/Tehran";

    public static String generatePersianDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        ULocale locale = new ULocale(PERSIAN_LOCALE_ID);
        SimpleDateFormat formatter = new SimpleDateFormat(PERSIAN_DATE_FORMAT, locale);
        ZonedDateTime zoned = dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of(PERSIAN_ZONE_ID));
        Date date = Date.from(zoned.toInstant());
        return formatter.format(date);
    }
}
