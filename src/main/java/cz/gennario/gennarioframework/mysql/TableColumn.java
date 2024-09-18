package cz.gennario.gennarioframework.mysql;

import lombok.Data;

@Data
public class TableColumn {

    public enum ColumnType {
        // Numeric Types
        TINYINT("TINYINT"),
        SMALLINT("SMALLINT"),
        MEDIUMINT("MEDIUMINT"),
        INT("INT"),
        BIGINT("BIGINT"),
        FLOAT("FLOAT"),
        DOUBLE("DOUBLE"),
        DECIMAL("DECIMAL"),

        // Date and Time Types
        DATE("DATE"),
        DATETIME("DATETIME"),
        TIMESTAMP("TIMESTAMP"),
        TIME("TIME"),
        YEAR("YEAR"),

        // String Types
        CHAR("CHAR"),
        VARCHAR("VARCHAR"),
        TEXT("TEXT"),
        MEDIUMTEXT("MEDIUMTEXT"),
        LONGTEXT("LONGTEXT"),
        JSON("JSON"),
        BLOB("BLOB");

        private final String type;

        ColumnType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        // Method to get the type
        public String getType() {
            return type;
        }
    }

    private String name;
    private ColumnType columnType;
    private int length;
    private boolean isNull;
    private boolean isPrimary;
    private boolean isAutoIncrement;

    public TableColumn(String name, ColumnType columnType, int length, boolean isNull, boolean isPrimary, boolean isAutoIncrement) {
        this.name = name;
        this.columnType = columnType;
        this.length = length;
        this.isNull = isNull;
        this.isPrimary = isPrimary;
        this.isAutoIncrement = isAutoIncrement;
    }

    public TableColumn(String name, ColumnType columnType, int length) {
        this.name = name;
        this.columnType = columnType;
        this.length = length;
        this.isNull = false;
        this.isPrimary = false;
        this.isAutoIncrement = false;
    }

    public TableColumn(String name, ColumnType columnType) {
        this.name = name;
        this.columnType = columnType;
        this.length = 0;
        this.isNull = false;
        this.isPrimary = false;
        this.isAutoIncrement = false;
    }

}
