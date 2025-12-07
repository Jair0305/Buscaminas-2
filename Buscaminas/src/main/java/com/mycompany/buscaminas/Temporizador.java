/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.buscaminas;

import javax.swing.*;

public class Temporizador {
    private Timer timer;
    private int secondsElapsed;
    private final JLabel label;

    public Temporizador(JLabel label) {
        this.label = label;
        this.secondsElapsed = 0;

        timer = new Timer(1000, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                secondsElapsed++;
                updateLabel();
            }
        });
    }

    public void start() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void stop() {
        timer.stop();
    }

    public void reset() {
        timer.stop();
        secondsElapsed = 0;
        updateLabel();
    }

    public void resetWithoutStart() {
        reset();
    }

    private void updateLabel() {
        long minutes = secondsElapsed / 60;
        long seconds = secondsElapsed % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public int getSeconds() {
        return secondsElapsed;
    }
}