package com.excelutils.config;

/**
 * Configuration for Excel export operations.
 * Use the builder pattern to create instances.
 */
public class ExcelConfiguration {

    private final boolean autoResize;
    private final boolean rightToLeft;
    private final String defaultSheetName;
    private final int maxThreadsForResize;
    private final boolean parallelResize;
    private final String dateFormat;
    private final String timezone;
    private final String locale;

    private ExcelConfiguration(Builder builder) {
        this.autoResize = builder.autoResize;
        this.rightToLeft = builder.rightToLeft;
        this.defaultSheetName = builder.defaultSheetName;
        this.maxThreadsForResize = builder.maxThreadsForResize;
        this.parallelResize = builder.parallelResize;
        this.dateFormat = builder.dateFormat;
        this.timezone = builder.timezone;
        this.locale = builder.locale;
    }

    public boolean isAutoResize() {
        return autoResize;
    }

    public boolean isRightToLeft() {
        return rightToLeft;
    }

    public String getDefaultSheetName() {
        return defaultSheetName;
    }

    public int getMaxThreadsForResize() {
        return maxThreadsForResize;
    }

    public boolean isParallelResize() {
        return parallelResize;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getLocale() {
        return locale;
    }

    public static ExcelConfiguration getDefault() {
        return new Builder().build();
    }

    public static class Builder {
        private boolean autoResize = true;
        private boolean rightToLeft = true;
        private String defaultSheetName = "Sheet1";
        private int maxThreadsForResize = Runtime.getRuntime().availableProcessors();
        private boolean parallelResize = true;
        private String dateFormat = "yyyy/MM/dd HH:mm:ss";
        private String timezone = "Asia/Tehran";
        private String locale = "fa_IR";

        public Builder autoResize(boolean autoResize) {
            this.autoResize = autoResize;
            return this;
        }

        public Builder rightToLeft(boolean rightToLeft) {
            this.rightToLeft = rightToLeft;
            return this;
        }

        public Builder defaultSheetName(String defaultSheetName) {
            this.defaultSheetName = defaultSheetName;
            return this;
        }

        public Builder maxThreadsForResize(int maxThreadsForResize) {
            this.maxThreadsForResize = maxThreadsForResize;
            return this;
        }

        public Builder parallelResize(boolean parallelResize) {
            this.parallelResize = parallelResize;
            return this;
        }

        public Builder dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public ExcelConfiguration build() {
            return new ExcelConfiguration(this);
        }
    }
}
