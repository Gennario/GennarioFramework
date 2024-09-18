package cz.gennario.gennarioframework.mysql;

import cz.gennario.gennarioframework.Main;

import java.sql.*;

public class SQLConnection {

    public enum DatabaseType {
        MYSQL,
        SQLITE
    }

    private String host;
    private int port;
    private String user;
    private String password;
    private String database;
    private boolean autoReconnect;
    private boolean useSSL;
    private Connection connection;

    private DatabaseType databaseType;

    // Constructor for MySQL
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

    // Constructor for SQLite
    public SQLConnection(String database) {
        this.database = database;

        this.databaseType = DatabaseType.SQLITE;

        connect();
    }

    private void connect() {
        try {
            switch (databaseType) {
                case MYSQL:
                    String mysqlUrl = String.format("jdbc:mysql://%s:%d/%s?autoReconnect=%s&useSSL=%s", host, port, database, autoReconnect, useSSL);
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(mysqlUrl, user, password);
                    break;
                case SQLITE:
                    String sqliteUrl = String.format("jdbc:sqlite:%s", database);
                    Class.forName("org.sqlite.JDBC");
                    connection = DriverManager.getConnection(sqliteUrl);
                    break;
            }
            System.out.println("Connection successful");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            System.err.println("Disabling the plugin...");
            Main.getInstance().getPluginLoader().disablePlugin(Main.getInstance());
        }
    }

    // Method to close the connection
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to check if the connection is valid
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to execute SELECT queries
    public ResultSet executeQuery(String query) throws SQLException {
        if (isConnectionValid()) {
            Statement statement = connection.createStatement();
            return statement.executeQuery(query);
        } else {
            // auto reconnect
            connect();
            return executeQuery(query);
        }
    }

    // Method to execute INSERT/UPDATE/DELETE queries
    public int executeUpdate(String query) throws SQLException {
        if (isConnectionValid()) {
            Statement statement = connection.createStatement();
            return statement.executeUpdate(query);
        } else {
            // auto reconnect
            connect();
            return executeUpdate(query);
        }
    }

    // Method to execute Prepared Statements for INSERT/UPDATE/DELETE
    public int executePreparedUpdate(String query, Object... parameters) throws SQLException {
        if (isConnectionValid()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            return preparedStatement.executeUpdate();
        } else {
            // auto reconnect
            connect();
            return executePreparedUpdate(query, parameters);
        }
    }

    // Method to execute Prepared Statements for SELECT
    public ResultSet executePreparedQuery(String query, Object... parameters) throws SQLException {
        if (isConnectionValid()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            return preparedStatement.executeQuery();
        } else {
            // auto reconnect
            connect();
            return executePreparedQuery(query, parameters);
        }
    }
}
