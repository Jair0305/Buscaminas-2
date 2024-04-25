package com.mycompany.buscaminas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:buscaminas.db";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL);
            createTablesIfNotExist(connection);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    private static void createTablesIfNotExist(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS Puntuaciones (\n"
                    + " id INTEGER PRIMARY KEY,\n"
                    + " nombre TEXT NOT NULL,\n"
                    + " clicks INTEGER NOT NULL,\n"
                    + " tiempo REAL NOT NULL,\n"
                    + " dificultad TEXT NOT NULL,\n"
                    + " fecha DATE NOT NULL,\n"
                    + " hora TIME NOT NULL\n"
                    + ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}