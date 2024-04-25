package com.mycompany.buscaminas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
            String sql = "CREATE TABLE IF NOT EXISTS Partida (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " name text NOT NULL,\n"
                    + " clicks integer NOT NULL,\n"
                    + " tiempo integer NOT NULL,\n"
                    + " dificultad text NOT NULL\n"
                    + ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}