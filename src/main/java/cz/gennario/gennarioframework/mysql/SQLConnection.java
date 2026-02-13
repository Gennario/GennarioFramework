package cz.gennario.gennarioframework.mysql;

import cz.gennario.gennarioframework.Main;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLConnection {

    public enum DatabaseType {
        MYSQL,
        SQLITE
    }

    private static final Logger LOGGER = Main.getInstance().getLogger();
    private static final int CONNECTION_TIMEOUT = 2;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String database;
    private final boolean autoReconnect;
    private final boolean useSSL;
    private final DatabaseType databaseType;

    private Connection connection;

    public SQLConnection(String host, int port, String user, String password, String database, boolean autoReconnect, boolean useSSL) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
        this.autoReconnect = autoReconnect;
        this.useSSL = useSSL;
        this.databaseType = DatabaseType.MYSQL;
        connect();
    }

    public SQLConnection(String database) {
        this.host = null;
        this.port = 0;
        this.user = null;
        this.password = null;
        this.database = database;
        this.autoReconnect = false;
        this.useSSL = false;
        this.databaseType = DatabaseType.SQLITE;
        connect();
    }

    private void connect() {
        try {
            String url = switch (databaseType) {
                case MYSQL -> {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    yield String.format("jdbc:mysql://%s:%d/%s?autoReconnect=%s&useSSL=%s", host, port, database, autoReconnect, useSSL);
                }
                case SQLITE -> {
                    Class.forName("org.sqlite.JDBC");
                    yield String.format("jdbc:sqlite:%s", database);
                }
            };
            connection = databaseType == DatabaseType.MYSQL
                    ? DriverManager.getConnection(url, user, password)
                    : DriverManager.getConnection(url);
            LOGGER.info("Database connection successful");
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to the database: " + e.getMessage());
        }
    }

    private void ensureConnection() throws SQLException {
        if (!isConnectionValid()) {
            for (int i = 0; i < MAX_RECONNECT_ATTEMPTS; i++) {
                connect();
                if (isConnectionValid()) return;
            }
            throw new SQLException("Failed to reconnect to database after " + MAX_RECONNECT_ATTEMPTS + " attempts");
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }

    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(CONNECTION_TIMEOUT);
        } catch (SQLException e) {
            return false;
        }
    }

    public ResultSet executeQuery(String query) throws SQLException {
        ensureConnection();
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public int executeUpdate(String query) throws SQLException {
        ensureConnection();
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(query);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
