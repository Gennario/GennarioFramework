package cz.gennario.gennarioframework.mysql;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PolySQL {

    private SQLConnection sqlConnection;
    private Map<String, SQLTable> tables;

    public PolySQL(Section connectionSection) {
        SQLConnection.DatabaseType databaseType = SQLConnection.DatabaseType.valueOf(connectionSection.getString("type"));
        if (databaseType == null) throw new IllegalArgumentException("Invalid database type");

        switch (databaseType) {
            case MYSQL -> {
                Section mysql = connectionSection.getSection("mysql");
                sqlConnection = new SQLConnection(
                        mysql.getString("host"),
                        mysql.getInt("port"),
                        mysql.getString("username"),
                        mysql.getString("password"),
                        mysql.getString("database"),
                        mysql.getBoolean("autoReconnect", true),
                        mysql.getBoolean("useSSL", false)
                );
                tables = new HashMap<>();
            }
            case SQLITE -> {
                Section sqlite = connectionSection.getSection("sqlite");
                sqlConnection = new SQLConnection(
                        sqlite.getString("database")
                );
                tables = new HashMap<>();
            }
        }
    }

    public PolySQL(SQLConnection.DatabaseType databaseType, Section connectionSection) {
        switch (databaseType) {
            case MYSQL -> {
                sqlConnection = new SQLConnection(
                        connectionSection.getString("host"),
                        connectionSection.getInt("port"),
                        connectionSection.getString("username"),
                        connectionSection.getString("password"),
                        connectionSection.getString("database"),
                        connectionSection.getBoolean("autoReconnect", true),
                        connectionSection.getBoolean("useSSL", false)
                );
                tables = new HashMap<>();
            }
            case SQLITE -> {
                sqlConnection = new SQLConnection(
                        connectionSection.getString("database")
                );
                tables = new HashMap<>();
            }
        }
    }

    public PolySQL(String host, int port, String user, String password, String database, boolean autoReconnect, boolean useSSL) {
        sqlConnection = new SQLConnection(
                host,
                port,
                user,
                password,
                database,
                autoReconnect,
                useSSL
        );
        tables = new HashMap<>();
    }

    public PolySQL appendTable(SQLTable table) {
        table.setPolySQL(this);
        tables.put(table.getName(), table);
        return this;
    }

    public SQLTable getTable(String name) {
        return tables.get(name);
    }

}
