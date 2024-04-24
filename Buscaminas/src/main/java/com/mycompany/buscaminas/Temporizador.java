/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.buscaminas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 *
 * @author jair0305
 */
public class Temporizador {
    

    private int tiempo = 0;
    
    public Temporizador()
    {
        Timer timer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tiempo++;
                if(tiempo > 999)
                {
                    ((Timer) e.getSource()).stop();
                }
                actualizarInterfazGrafica();
                
            }
        });
        
        timer.start();
    }
    
    public void actualizarInterfazGrafica()
    {
        double segundos = tiempo/100.0;
        System.out.println("Tiempo actual: " + String.format("%.2f", segundos) + " segundos");
    }
    
    public static void main(String[] args)
    {
        new Temporizador();
        try
        {
            Thread.sleep(10000);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
}
