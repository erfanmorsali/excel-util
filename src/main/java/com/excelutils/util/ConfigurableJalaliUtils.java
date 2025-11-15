package com.excelutils.util;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class ConfigurableJalaliUtils {

    public static String generatePersianDate(
            LocalDateTime dateTime,
            String dateFormat,
            String zoneId,
            String localeId) {

        if (dateTime == null) {
            return "";
        }

        ULocale locale = new ULocale(localeId);
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, locale);

        ZonedDateTime zoned = dateTime
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneId.of(zoneId));

        Date date = Date.from(zoned.toInstant());
        return formatter.format(date);
    }

    public static String generatePersianDate(LocalDateTime dateTime) {
        return generatePersianDate(
            dateTime,
            "yyyy/MM/dd HH:mm:ss",
            "Asia/Tehran",
            "fa_IR"
        );
    }
}
