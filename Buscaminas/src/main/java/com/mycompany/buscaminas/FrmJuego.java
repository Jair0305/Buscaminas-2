/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.buscaminas;

import buscaminas.Casilla;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author chama
 */
public class FrmJuego extends javax.swing.JFrame {

    int numFilas = 25;
	int numColumnas = 25;
	int numMinas = 5;
	JButton[][] botonesTablero;
    Temporizador temporizador;
	
	Tablero tablero;
	
    /**
     * Creates new form FrmJuego
     */
    public FrmJuego() {
        initComponents();
        temporizador = new Temporizador();
        juegoNuevo();
    }
    
    void descargarControles()
    {
        if(botonesTablero!=null)
        {
            for(int i = 0; i < botonesTablero.length; i++)
            {
                for(int j = 0; j < botonesTablero[i].length; j++)
                {
                    if(botonesTablero[i][j]!=null)
                    {
                        getContentPane().remove(botonesTablero[i][j]);
                    }
                }
            }
        }
    }
    
    private void juegoNuevo()
    {
        descargarControles();
        cargarControles();
        crearTablero();
        repaint();
    }
    
    private void crearTablero()
    {
    	tablero = new Tablero(numFilas, numColumnas, numMinas);
    	tablero.setEventoPartidaPerdida(new Consumer<List<Casilla>>() {
			
                @Override
                public void accept(List<Casilla> t) 
                {
                    for(Casilla casillasConMina: t)
                    {
                            botonesTablero[casillasConMina.getPosFila()][casillasConMina.getPosColumna()].setText("*");
                    }
                    deshabilitarBotones();
                }   
        });
    	tablero.setEventoPartidaGanada(new Consumer<List<Casilla>>() {
		
    		@Override
    		public void accept(List<Casilla> t)
    		{
    			for(Casilla casillaConMina: t)
    			{
    				botonesTablero[casillaConMina.getPosFila()][casillaConMina.getPosColumna()].setText(":)");
    			}
                        deshabilitarBotones();
    		}
    	});
    	
    	tablero.setEventoCasillaAbierta(new Consumer<Casilla>() {
    		
    		@Override
    		public void accept(Casilla t)
    		{
    			botonesTablero[t.getPosFila()][t.getPosColumna()].setEnabled(false);
    			botonesTablero[t.getPosFila()][t.getPosColumna()].setText(t.getMinasAlrededor()==0?"":t.getMinasAlrededor()+"");
    		}
    	});
    }
    
    private void deshabilitarBotones() {
        for(int i = 0; i < botonesTablero.length; i++) {
            for(int j = 0; j < botonesTablero[i].length; j++) {
                botonesTablero[i][j].setEnabled(false);
            }
        }
    }

    
    private void cargarControles()
    {
    	int posXReferencia = 25;
    	int posYReferencia=25;
    	int anchoControl=30;
    	int altoControl=30;
    	
    	botonesTablero = new JButton[numFilas][numColumnas];
    	for(int i = 0; i < botonesTablero.length; i++)
    	{
    		for(int j = 0; j<botonesTablero[i].length;j++)
    		{
    			botonesTablero[i][j] = new JButton();
    			botonesTablero[i][j].setName(i+","+j);
    			botonesTablero[i][j].setBorder(null);
    			if(i==0 && j==0)
    			{
    				botonesTablero[i][j].setBounds(posXReferencia,posYReferencia,anchoControl,altoControl);
 
    			}else if( i==0 && j!=0)
    			{
    				botonesTablero[i][j].setBounds(botonesTablero[i][j-1].getX()+botonesTablero[i][j-1].getWidth(),posYReferencia,anchoControl,altoControl);
    			}else
    			{
    				botonesTablero[i][j].setBounds(botonesTablero[i-1][j].getX(),botonesTablero[i-1][j].getY() + botonesTablero[i-1][j].getHeight(),anchoControl,altoControl);
    			}
    			botonesTablero[i][j].addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						btnClick(e);
						
					}
				});
    			getContentPane().add(botonesTablero[i][j]);
    		}
    	} 
        this.setSize(botonesTablero[numFilas-1][numColumnas-1].getX() + botonesTablero[numFilas-1][numColumnas-1].getWidth()+40,
                botonesTablero[numFilas-1][numColumnas-1].getY() + botonesTablero[numFilas-1][numColumnas-1].getHeight() + 90);
    }
    
    private void btnClick(ActionEvent e)
    {
    	JButton btn = (JButton)e.getSource();
    	String[] coordenada = btn.getName().split(",");
    	int posFila=Integer.parseInt(coordenada[0]);
    	int posColumna=Integer.parseInt(coordenada[1]);
    	tablero.seleccionarCasilla(posFila, posColumna);
        temporizador.actualizarInterfazGrafica();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        menuDificultad = new javax.swing.JMenu();
        facil = new javax.swing.JMenuItem();
        medio = new javax.swing.JMenuItem();
        dificil = new javax.swing.JMenuItem();
        tamano = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        juegoNuevo = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        menuDificultad.setText("Dificultad");

        facil.setText("Facil");
        facil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                facilActionPerformed(evt);
            }
        });
        menuDificultad.add(facil);

        medio.setText("Medio");
        medio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                medioActionPerformed(evt);
            }
        });
        menuDificultad.add(medio);

        dificil.setText("Dificl");
        dificil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dificilActionPerformed(evt);
            }
        });
        menuDificultad.add(dificil);

        tamano.setText("tamano");
        tamano.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tamanoActionPerformed(evt);
            }
        });
        menuDificultad.add(tamano);

        jMenuBar1.add(menuDificultad);

        jMenu1.setText("Juego");

        juegoNuevo.setText("Nuevo");
        juegoNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                juegoNuevoActionPerformed(evt);
            }
        });
        jMenu1.add(juegoNuevo);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 607, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 444, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void facilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_facilActionPerformed
        // TODO add your handling code here:
        this.numColumnas = 10;
        this.numFilas = 8;
        this.numMinas = 10;
        juegoNuevo();
    }//GEN-LAST:event_facilActionPerformed

    private void tamanoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tamanoActionPerformed
        // TODO add your handling code here:
        int num = Integer.parseInt(JOptionPane.showInputDialog("Digite el tamanho de la matriz"));
        
        this.numColumnas = num;
        this.numFilas = num;
        this.numMinas = num;
        juegoNuevo();
    }//GEN-LAST:event_tamanoActionPerformed

    private void juegoNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_juegoNuevoActionPerformed
        juegoNuevo();
    }//GEN-LAST:event_juegoNuevoActionPerformed

    private void medioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_medioActionPerformed
        this.numColumnas = 15;
        this.numFilas = 18;
        this.numMinas = 40;
        juegoNuevo();
    }//GEN-LAST:event_medioActionPerformed

    private void dificilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dificilActionPerformed
        this.numColumnas = 24;
        this.numFilas = 20;
        this.numMinas = 99;
        juegoNuevo();
    }//GEN-LAST:event_dificilActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmJuego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmJuego().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem dificil;
    private javax.swing.JMenuItem facil;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem juegoNuevo;
    private javax.swing.JMenuItem medio;
    private javax.swing.JMenu menuDificultad;
    private javax.swing.JMenuItem tamano;
    // End of variables declaration//GEN-END:variables
}
