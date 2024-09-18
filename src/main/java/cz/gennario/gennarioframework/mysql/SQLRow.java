package cz.gennario.gennarioframework.mysql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLRow {

    private SQLTable table;
    private Map<String, Object> data;

    public SQLRow(SQLTable table, ResultSet resultSet) {
        this.table = table;
        this.data = new HashMap<>();

        try {
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                data.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object get(String column) {
        return data.get(column);
    }

    public String getString(String column) {
        return (String) data.get(column);
    }

    public int getInt(String column) {
        return (int) data.get(column);
    }

    public double getDouble(String column) {
        return (double) data.get(column);
    }

    public boolean getBoolean(String column) {
        return (boolean) data.get(column);
    }

    public long getLong(String column) {
        return (long) data.get(column);
    }

    public float getFloat(String column) {
        return (float) data.get(column);
    }

    public short getShort(String column) {
        return (short) data.get(column);
    }

    public byte getByte(String column) {
        return (byte) data.get(column);
    }

    public byte[] getBytes(String column) {
        return (byte[]) data.get(column);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public SQLTable getTable() {
        return table;
    }

    public void set(String column, Object value) {
        String column1 = new ArrayList<>(data.keySet()).get(0);
        table.set(List.of(
                new SQLManipulationData(column, value)
        ), new SQLWhereData(column1, SQLWhereData.WhereOperator.EQUALS, String.valueOf(data.get(column1))));
    }

}
