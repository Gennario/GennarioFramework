package cz.gennario.gennarioframework.mysql;

import lombok.Data;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Data
public class SQLTable {

    private PolySQL polySQL;

    private String name;
    private TableColumn[] columns;
    private Map<String, SQLAutoSaveTask> autoSaves;

    public SQLTable(String name, TableColumn... columns) {
        this.name = name;
        this.columns = columns;
        this.autoSaves = new HashMap<>();
    }

    /**
     * Create the table if it does not exist
     * @throws SQLException If an error occurs
     */
    public void createIfNotExist() throws SQLException {
        if (polySQL == null) {
            Bukkit.getLogger().warning("Table " + name + " is not attached to any PolySQL instance! Please user polySQL.appendTable(table) method before any table SQL commands in your code!");
            return;
        }
        String cmd = "CREATE TABLE IF NOT EXISTS " + name + " (";
        String primaryKey = "";
        for (int i = 0; i < columns.length; i++) {
            TableColumn column = columns[i];
            if (column.getLength() > 0) {
                cmd += column.getName() + " " + column.getColumnType() + "(" + column.getLength() + ")";
            } else {
                cmd += column.getName() + " " + column.getColumnType();
            }
            if (!column.isNull()) {
                cmd += " NOT NULL";
            }
            if (column.isPrimary()) {
                primaryKey = column.getName();
            }
            if (column.isAutoIncrement()) {
                cmd += " AUTO_INCREMENT";
            }
            if (i < columns.length - 1) {
                cmd += ", ";
            }
        }
        if (!primaryKey.isEmpty()) {
            cmd += ", PRIMARY KEY (" + primaryKey + ")";
        }
        cmd += ");";

        polySQL.getSqlConnection().executeUpdate(cmd);
    }

    /**
     * Truncate the table
     */
    public void truncateSQL() {
        try {
            polySQL.getSqlConnection().executeUpdate("TRUNCATE TABLE " + name);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while truncating table " + name + ": " + e.getMessage());
        }
    }

    /**
     * Drop the table
     */
    public void dropTable() {
        try {
            polySQL.getSqlConnection().executeUpdate("DROP TABLE " + name);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while dropping table " + name + ": " + e.getMessage());
        }
    }

    // INSERT

    /**
     * Insert data into the table
     * @param values Values to insert
     * @throws SQLException
     */
    public void insert(Object... values) throws SQLException {
        if (polySQL == null) {
            Bukkit.getLogger().warning("Table " + name + " is not attached to any PolySQL instance! Please use polySQL.appendTable(table) method before any table SQL commands in your code!");
            return;
        }

        long nonAutoIncrementColumnsCount = Arrays.stream(columns).filter(column -> !column.isAutoIncrement()).count();
        if (values.length != nonAutoIncrementColumnsCount) {
            throw new IllegalArgumentException("The number of values provided does not match the number of non-auto-increment columns. Required: " + nonAutoIncrementColumnsCount + ", provided: " + values.length + ".");
        }

        StringBuilder cmd = new StringBuilder("INSERT INTO " + name + " (");

        boolean first = true;
        for (TableColumn column : columns) {
            if (column.isAutoIncrement()) {
                continue;
            }
            if (!first) {
                cmd.append(", ");
            }
            cmd.append(column.getName());
            first = false;
        }

        cmd.append(") VALUES (");

        for (int i = 0; i < values.length; i++) {
            cmd.append("'").append(values[i]).append("'");
            if (i < values.length - 1) {
                cmd.append(", ");
            }
        }

        cmd.append(")");

        polySQL.getSqlConnection().executeUpdate(cmd.toString());
    }

    // DELETE

    /**
     * Delete data from the table
     * @param whereData Where data
     */
    public void delete(SQLWhereData... whereData) {
        String cmd = "DELETE FROM " + name;
        cmd = generateWhereAddition(cmd, whereData);
        try {
            polySQL.getSqlConnection().executeUpdate(cmd);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while deleting data in table " + name + ": " + e.getMessage());
        }
    }

    // ROW EXIST

    public boolean rowExist(SQLWhereData... whereData) {
        String cmd = "SELECT * FROM " + name;
        cmd = generateWhereAddition(cmd, whereData);
        try {
            ResultSet resultSet = polySQL.getSqlConnection().executeQuery(cmd);
            return resultSet.next();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while checking if row exists in table " + name + ": " + e.getMessage());
        }
        return false;
    }

    // SET

    /**
     * Set data in the table
     * @param sqlManipulationData List of SQLManipulationData
     * @param whereData Where data
     */
    public void set(List<SQLManipulationData> sqlManipulationData, SQLWhereData... whereData) {
        String cmd = "UPDATE " + name + " SET ";
        for (int i = 0; i < sqlManipulationData.size(); i++) {
            SQLManipulationData data = sqlManipulationData.get(i);
            cmd += data.getColumn() + " = '" + data.getValue() + "'";
            if (i < sqlManipulationData.size() - 1) {
                cmd += ", ";
            }
        }
        cmd = generateWhereAddition(cmd, whereData);
        try {
            polySQL.getSqlConnection().executeUpdate(cmd);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while setting data in table " + name + ": " + e.getMessage());
        }
    }

    // SELECT

    /**
     * Select all rows from the table
     * @return List of SQLRows
     */
    public List<SQLRow> selectAllRows() {
        List<String> columns = new ArrayList<>();
        for (TableColumn column : this.columns) {
            columns.add(column.getName());
        }
        return select(columns, 0);
    }

    /**
     * Select all rows from the table
     * @param whereData Where data
     * @return List of SQLRows
     */
    public List<SQLRow> select(SQLWhereData... whereData) {
        List<String> columns = new ArrayList<>();
        for (TableColumn column : this.columns) {
            columns.add(column.getName());
        }
        return select(columns, 0, whereData);
    }

    /**
     * Select rows from the table
     * @param columns Columns to select
     * @param whereData Where data
     * @return List of SQLRows
     */
    public List<SQLRow> select(List<String> columns, SQLWhereData... whereData) {
        return select(columns, 0, whereData);
    }

    /**
     * Select rows from the table
     * @param columns Columns to select
     * @param limit Limit of rows
     * @param whereData Where data
     * @return List of SQLRows
     */
    public List<SQLRow> select(List<String> columns, int limit, SQLWhereData... whereData) {
        List<SQLRow> rows = new ArrayList<>();

        String cmd = "SELECT ";
        for (int i = 0; i < columns.size(); i++) {
            cmd += columns.get(i);
            if (i < columns.size() - 1) {
                cmd += ", ";
            }
        }
        cmd += " FROM " + name;
        cmd = generateWhereAddition(cmd, whereData);
        if(limit > 0) {
            cmd += " LIMIT " + limit;
        }

        try {
            ResultSet resultSet = polySQL.getSqlConnection().executeQuery(cmd);
            while (resultSet.next()) {
                SQLRow row = new SQLRow(this, resultSet);
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while selecting data in table " + name + ": " + e.getMessage());
        }
        return rows;
    }

    // TOP

    /**
     * Enum for top sort direction
     */
    public enum TopSortDirection {
        BIGGEST_TO_LOWEST("DESC"),
        LOWEST_TO_BIGGEST("ASC");

        private String direction;

        TopSortDirection(String direction) {
            this.direction = direction;
        }

        public String getDirection() {
            return direction;
        }
    }

    /**
     * Get top rows from the table
     * @param byColumn Column to sort by
     * @param topSortDirection Direction of sorting
     * @param limit Limit of rows
     * @param whereData Where data
     * @return List of SQLRows
     */
    public List<SQLRow> getTop(String byColumn, TopSortDirection topSortDirection, int limit, SQLWhereData... whereData) {
        String cmd = "SELECT * FROM " + name;
        cmd = generateWhereAddition(cmd, whereData);
        cmd += " ORDER BY " + byColumn + " " + topSortDirection.getDirection() + " LIMIT " + limit;
        try {
            ResultSet resultSet = polySQL.getSqlConnection().executeQuery(cmd);
            List<SQLRow> rows = new ArrayList<>();
            while (resultSet.next()) {
                SQLRow row = new SQLRow(this, resultSet);
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while selecting top data in table " + name + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // AUTOSAVE

    /**
     * Append an autosave task to the table
     * @param id ID of the autosave task
     * @param runnable Runnable task
     */
    public void appendAutoSave(String id, SQLAutoSaveTask runnable) {
        autoSaves.put(id, runnable);
    }

    /**
     * Remove an autosave task from the table
     * @param id ID of the autosave task
     */
    public void removeAutoSave(String id) {
        SQLAutoSaveTask remove = autoSaves.remove(id);
        remove.setRunning(false);
    }

    /**
     * Get an autosave task from the table
     * @param id ID of the autosave task
     * @return SQLAutoSaveTask
     */
    public SQLAutoSaveTask getAutoSave(String id) {
        return autoSaves.get(id);
    }

    // UTILS

    /**
     * Count rows in the table
     * @return Number of rows
     */
    public int countRows() {
        try {
            ResultSet resultSet = polySQL.getSqlConnection().executeQuery("SELECT COUNT(*) FROM " + name);
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Error while counting rows in table " + name + ": " + e.getMessage());
        }
        return 0;
    }

    private String generateWhereAddition(String cmd, SQLWhereData... data) {
        if (data.length > 0) {
            cmd += " WHERE ";
            for (int i = 0; i < data.length; i++) {
                SQLWhereData whereData = data[i];
                cmd += whereData.getColumn() + " " + whereData.getOperator().getOperator() + " '" + whereData.getValue() + "'";
                if (i < data.length - 1) {
                    cmd += " AND ";
                }
            }
        }
        return cmd;
    }

}
