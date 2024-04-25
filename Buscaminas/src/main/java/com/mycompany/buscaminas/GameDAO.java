package com.mycompany.buscaminas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GameDAO {
    public void savePartida(String name, int clicks, Float tiempo, String dificultad) {
        String sql = "INSERT INTO Partida (name, clicks,  tiempo, dificultad) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, clicks);
            ps.setFloat(3, tiempo);
            ps.setString(4, dificultad);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}