package com.mycompany.buscaminas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    public void savePartida(String name, int clicks, Float tiempo, String dificultad) {
        String sql = "INSERT INTO Puntuaciones (nombre, clicks,  tiempo, dificultad, fecha, hora) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, clicks);
            ps.setFloat(3, tiempo);
            ps.setString(4, dificultad);
            ps.setDate(5, Date.valueOf(java.time.LocalDate.now()));
            ps.setTime(6, Time.valueOf(java.time.LocalTime.now()));

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Puntuacion> obtenerPuntuacionesPorDificultad(String dificultad) {
        List<Puntuacion> puntuaciones = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:buscaminas.db");
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Puntuaciones WHERE Dificultad = ? ORDER BY Tiempo ASC")) {

            pstmt.setString(1, dificultad);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Puntuacion puntuacion = new Puntuacion();
                puntuacion.setNombre(rs.getString("nombre"));
                puntuacion.setClicks(rs.getInt("clicks"));
                puntuacion.setTiempo(rs.getFloat("tiempo"));
                puntuacion.setDificultad(rs.getString("dificultad"));
                puntuacion.setFecha(rs.getDate("fecha").toLocalDate());
                puntuacion.setHora(rs.getTime("hora").toLocalTime());

                puntuaciones.add(puntuacion);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return puntuaciones;
    }
}