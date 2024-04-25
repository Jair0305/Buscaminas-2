/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.buscaminas;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Temporizador {
    private ScheduledExecutorService scheduler;
    private final AtomicLong milisegundosTranscurridos = new AtomicLong(0);
    private final JLabel label;

    public Temporizador(JLabel label) {
        this.label = label;
    }

    public void start() {
        scheduler = Executors.newScheduledThreadPool(1); // Create a new ScheduledExecutorService
        final Runnable timer = new Runnable() {
            public void run() {
                long milisegundos = milisegundosTranscurridos.incrementAndGet();
                long segundos = milisegundos / 1000;
                long milisegundosRestantes = milisegundos % 1000;
                label.setText(segundos + "." + milisegundosRestantes);
                if(segundos == 999)
                {
                    stop();
                }
            }
        };
        scheduler.scheduleAtFixedRate(timer, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void stop(){
        if(scheduler != null){
            scheduler.shutdownNow();
        }
    }

    public void reset(){
        milisegundosTranscurridos.set(0);
    }


    public void resetWithoutStart(){
        milisegundosTranscurridos.set(0);

    }
}