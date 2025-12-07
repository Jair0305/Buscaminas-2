/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.buscaminas;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import com.formdev.flatlaf.FlatDarkLaf;

/**
 *
 * @author chama
 */
public class FrmJuego extends javax.swing.JFrame {

    int numFilas = 10;
    int numColumnas = 8;
    int numMinas = 10;

    JButton[][] botonesTablero;
    JLabel labelTiempo;
    JLabel labelClicks;
    JButton btnReset; // The classic smiley face button
    JPanel panelTableroVisual;
    JPanel panelInfo;

    Temporizador temporizador;
    boolean primerClick = false;
    int numeroDeClicks = 0;
    Tablero tablero;
    String dificultad = "Facil";
    int cellSize = 50; // Default cell size for zoom
    JLabel labelMinasRestantes;
    int minasMarcadas = 0;
    boolean juegoFinalizado = false;

    /**
     * Creates new form FrmJuego
     */

    public FrmJuego() {
        initComponents();

        // Custom Setup
        setTitle("Buscaminas Pro");
        // setSize(800, 600); // Removed fixed size to allow pack() to work
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize components
        labelTiempo = new JLabel("00:00");
        labelClicks = new JLabel("Clicks: 0");
        temporizador = new Temporizador(labelTiempo);

        // Header Panel
        panelInfo = new JPanel();
        panelInfo.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelInfo.setBackground(new Color(45, 45, 48)); // Darker background

        labelTiempo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        labelTiempo.setForeground(new Color(78, 201, 176)); // Cyan-ish
        labelTiempo.setIcon(new ImageIcon()); // Placeholder for icon if needed

        labelClicks.setFont(new Font("Segoe UI", Font.BOLD, 18));
        labelClicks.setForeground(new Color(220, 220, 220));

        // Reset Button (Classic Smiley)
        btnReset = new JButton("🙂");
        btnReset.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        btnReset.setFocusPainted(false);
        // btnReset.setBorderPainted(false); // Kept standard border
        // btnReset.setContentAreaFilled(false); // Kept standard background
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> juegoNuevo());

        panelInfo.add(labelTiempo);
        panelInfo.add(Box.createHorizontalStrut(20)); // Spacer
        panelInfo.add(btnReset);
        panelInfo.add(Box.createHorizontalStrut(20)); // Spacer
        panelInfo.add(labelClicks);

        // Mines Counter
        labelMinasRestantes = new JLabel("Minas: " + numMinas);
        labelMinasRestantes.setFont(new Font("Segoe UI", Font.BOLD, 18));
        labelMinasRestantes.setForeground(new Color(255, 100, 100));
        panelInfo.add(Box.createHorizontalStrut(20));
        panelInfo.add(labelMinasRestantes);

        add(panelInfo, BorderLayout.NORTH);

        panelTableroVisual = new JPanel();
        panelTableroVisual.setBackground(new Color(30, 30, 30));
        // Use visible border for the board itself
        panelTableroVisual.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));

        // Wrapper for centering and preventing stretch
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(30, 30, 30)); // Match background
        wrapperPanel.add(panelTableroVisual);

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        // Zoom Support
        scrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                int rotation = e.getWheelRotation();
                if (rotation < 0) {
                    cellSize = Math.min(cellSize + 5, 100); // Zoom In
                } else {
                    cellSize = Math.max(cellSize - 5, 20); // Zoom Out
                }
                updateZoom();
            } else {
                // Propagate to parent for normal scrolling if needed, or let JScrollPane handle
                // it
                // By default JScrollPane handles it if not consumed.
                // We just need to ensure we don't consume it if Ctrl isn't down.
                e.getComponent().getParent().dispatchEvent(e);
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        juegoNuevo();
    }

    void descargarControles() {
        if (panelTableroVisual != null) {
            panelTableroVisual.removeAll();
        }
    }

    private void juegoNuevo() {
        descargarControles();
        cargarControles();
        crearTablero();

        panelTableroVisual.revalidate();
        panelTableroVisual.repaint();

        primerClick = false;
        temporizador.stop();
        temporizador.reset();
        labelTiempo.setText("00:00");
        numeroDeClicks = 0;
        labelClicks.setText("Clicks: 0");
        if (btnReset != null)
            btnReset.setText("🙂");

        minasMarcadas = 0;
        actualizarContadorMinas();
        juegoFinalizado = false;
    }

    private void actualizarContadorMinas() {
        if (labelMinasRestantes != null) {
            labelMinasRestantes.setText("Minas: " + (numMinas - minasMarcadas));
        }
    }

    private void crearTablero() {
        tablero = new Tablero(numFilas, numColumnas, numMinas);
        tablero.setEventoPartidaPerdida(new Consumer<List<Casilla>>() {
            @Override
            public void accept(List<Casilla> mines) {
                // Reveal all mines and show false flags
                for (int i = 0; i < numFilas; i++) {
                    for (int j = 0; j < numColumnas; j++) {
                        Casilla c = tablero.casillas[i][j];
                        JButton btn = botonesTablero[i][j];

                        // If it's a mine
                        if (c.isMina()) {
                            // If it wasn't flagged, show it
                            if (!c.isBandera()) {
                                btn.setText("💣");
                                btn.setForeground(Color.WHITE);

                                // Determine if this was the clicked mine (the one that exploded)
                                // The exploded mine is usually the one that is newly "Opened" or we can infer
                                // it
                                // But simpler is: checking if it's the one we just clicked?
                                // Actually, we can just make ALL unflagged mines visible.
                                // The clicked one is technically "Open".
                                if (c.isAbierta()) {
                                    btn.setBackground(new Color(200, 60, 60)); // Red background for the killer
                                } else {
                                    btn.setBackground(new Color(100, 100, 100)); // Grey for others
                                }
                            }
                        }
                        // If it's NOT a mine but HAS a flag -> False Flag
                        else if (c.isBandera()) {
                            btn.setText("❌"); // Cross
                            btn.setForeground(new Color(255, 60, 60));
                        }
                    }
                }

                deshabilitarBotones();
                temporizador.stop();
                if (btnReset != null)
                    btnReset.setText("😵");
                JOptionPane.showMessageDialog(null, "¡Boom! Has perdido.");
            }
        });
        tablero.setEventoPartidaGanada(new Consumer<List<Casilla>>() {

            @Override
            public void accept(List<Casilla> t) {
                for (Casilla casillaConMina : t) {
                    JButton btn = botonesTablero[casillaConMina.getPosFila()][casillaConMina.getPosColumna()];
                    btn.setText("🚩");
                    btn.setForeground(Color.GREEN);
                }
                deshabilitarBotones();
                temporizador.stop();
                if (btnReset != null)
                    btnReset.setText("😎");
                actualizarContadorMinas(); // Ensure final state
                String nombreJugador = JOptionPane
                        .showInputDialog("¡Felicidades! Ganaste.\nTiempo: " + labelTiempo.getText()
                                + "s | Clicks: " + numeroDeClicks + "\nIngresa tu nombre:");
                GameDAO gameDAO = new GameDAO();
                try {
                    gameDAO.savePartida(Objects.requireNonNullElse(nombreJugador, "Jugador"), numeroDeClicks,
                            (float) temporizador.getSeconds(), dificultad);
                } catch (Exception e) {
                }
            }
        });

        tablero.setEventoCasillaAbierta(new Consumer<Casilla>() {

            @Override
            public void accept(Casilla t) {
                JButton btn = botonesTablero[t.getPosFila()][t.getPosColumna()];

                // Keep enabled to preserve custom background color in all LookAndFeels
                // We handle "not clickable" in the listener logic
                btn.setFocusable(false);
                btn.setBackground(new Color(36, 36, 36)); // Dark, flat "ground" color for revealed
                btn.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45))); // Subtle flat border

                int minas = t.getMinasAlrededor();
                if (minas > 0) {
                    // Start with simple text to respect setFont() size changes from Zoom
                    btn.setText(String.valueOf(minas));
                    // Explicitly set text color just in case (though HTML usually handles it)
                    btn.setForeground(getColorForMines(minas));
                } else {
                    btn.setText("");
                }
            }

            private Color getColorForMines(int minas) {
                switch (minas) {
                    case 1:
                        return new Color(80, 150, 255); // Blue
                    case 2:
                        return new Color(80, 255, 100); // Green
                    case 3:
                        return new Color(255, 80, 80); // Red
                    case 4:
                        return new Color(180, 100, 255); // Purple
                    case 5:
                        return new Color(255, 180, 50); // Orange
                    default:
                        return Color.WHITE;
                }
            }

        });
    }

    private void deshabilitarBotones() {
        juegoFinalizado = true;
        // Do NOT disable buttons to keep their colors/icons visible
        // Just set the flag to ignore input
    }

    private void cargarControles() {
        panelTableroVisual.removeAll();
        panelTableroVisual.setLayout(new GridLayout(numFilas, numColumnas, 0, 0)); // No gaps, borders handle separation

        botonesTablero = new JButton[numFilas][numColumnas];
        // Use Segoe UI Emoji explicitly for Windows to ensure flags/bombs show
        Font fontCasilla = new Font("Segoe UI Emoji", Font.BOLD, 24);
        Color colorUnpressed = new Color(110, 110, 110);
        // Or lighter for contrast: new Color(160, 160, 160)?
        // User asked for "Super upgrade" - let's go with a sleek "Metal" dark look but
        // distinct from "Ground".

        for (int i = 0; i < botonesTablero.length; i++) {
            for (int j = 0; j < botonesTablero[i].length; j++) {
                botonesTablero[i][j] = new JButton();
                botonesTablero[i][j].setName(i + "," + j);

                // CRITICAL: Force styling
                botonesTablero[i][j].setBorderPainted(true);
                botonesTablero[i][j].setFocusPainted(false);
                botonesTablero[i][j].setContentAreaFilled(true);
                botonesTablero[i][j].setOpaque(true);

                botonesTablero[i][j].setBackground(colorUnpressed);
                botonesTablero[i][j].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

                botonesTablero[i][j].setForeground(Color.WHITE);
                botonesTablero[i][j].setFont(fontCasilla);

                // Hover & O-Face Reaction
                final int r = i;
                final int c = j;
                botonesTablero[i][j].addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent evt) {
                        if (!tablero.casillas[r][c].isAbierta()) {
                            botonesTablero[r][c].setBackground(new Color(130, 130, 130));
                        }
                    }

                    public void mouseExited(MouseEvent evt) {
                        if (!tablero.casillas[r][c].isAbierta()) {
                            botonesTablero[r][c].setBackground(colorUnpressed);
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        // Classic "O" Face when pressing
                        if (btnReset != null && !juegoFinalizado && !tablero.juegoTerminado) {
                            btnReset.setText("😮");
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        // Return to normal
                        if (btnReset != null && !juegoFinalizado && !tablero.juegoTerminado) {
                            btnReset.setText("🙂");
                        }

                        if (SwingUtilities.isRightMouseButton(e)) {
                            btnClickDerecho(e);
                        }
                    }
                });

                botonesTablero[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        btnClick(e);
                    }
                });

                // Enforce square size
                botonesTablero[i][j].setPreferredSize(new Dimension(50, 50));

                panelTableroVisual.add(botonesTablero[i][j]);
            }
        }

        panelTableroVisual.revalidate();
        // pack(); // No longer pack main frame every time, allow scroll
        updateZoom(); // Apply size
    }

    private void updateZoom() {
        if (botonesTablero == null)
            return;

        Font font = new Font("Segoe UI Emoji", Font.BOLD, (int) (cellSize * 0.45));

        for (int i = 0; i < botonesTablero.length; i++) {
            for (int j = 0; j < botonesTablero[i].length; j++) {
                if (botonesTablero[i][j] != null) {
                    botonesTablero[i][j].setPreferredSize(new Dimension(cellSize, cellSize));
                    botonesTablero[i][j].setFont(font);
                    // Also update text if "Mines around" is visible?
                    // Actually the font set handles the size, but if they are HTML formatted...
                    // Our HTML string uses <font size='5'> which is fixed.
                    // Let's remove HTML font sizing in listener and rely on component font?
                    // Or regenerate the HTML string.
                    // Simpler: Just rely on component font and remove HTML tags in creation if
                    // possible,
                    // OR specifically update HTML with font variable.
                    // For now, let's just resize the button itself, the text might be small/large.
                }
            }
        }
        panelTableroVisual.revalidate();
        panelTableroVisual.repaint();
    }

    private void btnClick(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        String[] coordenada = btn.getName().split(",");
        int posFila = Integer.parseInt(coordenada[0]);
        int posColumna = Integer.parseInt(coordenada[1]);

        if (juegoFinalizado)
            return;

        Casilla currentCasilla = tablero.casillas[posFila][posColumna];

        if (currentCasilla.isBandera() || currentCasilla.isInterrogacion()) {
            return;
        }

        // CHORDING LOGIC (Classic Minesweeper feature)
        if (currentCasilla.isAbierta()) {
            int minas = currentCasilla.getMinasAlrededor();
            if (minas > 0) {
                // Count flags around
                List<Casilla> neighbors = tablero.obtenerCasillaAlrededor(posFila, posColumna);
                long flagsAround = neighbors.stream().filter(Casilla::isBandera).count();

                if (flagsAround == minas) {
                    boolean somethingOpened = false;
                    for (Casilla neighbor : neighbors) {
                        if (!neighbor.isAbierta() && !neighbor.isBandera()) {
                            tablero.seleccionarCasilla(neighbor.getPosFila(), neighbor.getPosColumna());
                            somethingOpened = true;
                        }
                    }
                    if (somethingOpened) {
                        numeroDeClicks++;
                        if (labelClicks != null)
                            labelClicks.setText("Clicks: " + numeroDeClicks);
                    }
                }
            }
            return;
        }

        if (!primerClick) {
            temporizador.start();
            primerClick = true;
            numeroDeClicks++;
        }
        tablero.seleccionarCasilla(posFila, posColumna);
        numeroDeClicks++;
        if (labelClicks != null)
            labelClicks.setText("Clicks: " + numeroDeClicks);
    }

    private void btnClickDerecho(MouseEvent e) {
        if (juegoFinalizado)
            return;

        JButton btn = (JButton) e.getSource();
        String[] coordenada = btn.getName().split(",");
        int posFila = Integer.parseInt(coordenada[0]);
        int posColumna = Integer.parseInt(coordenada[1]);

        // Only allow flag toggle if NOT open
        if (!tablero.casillas[posFila][posColumna].isAbierta()) {
            if (tablero.casillas[posFila][posColumna].isBandera()) {
                tablero.casillas[posFila][posColumna].setBandera(false);
                minasMarcadas--;
                actualizarContadorMinas();
                tablero.casillas[posFila][posColumna].setInterrogacion(true);
                btn.setText("❓");
                btn.setForeground(Color.WHITE);
            } else if (tablero.casillas[posFila][posColumna].isInterrogacion()) {
                tablero.casillas[posFila][posColumna].setInterrogacion(false);
                btn.setText("");
                btn.setForeground(Color.WHITE);
            } else {
                tablero.casillas[posFila][posColumna].setBandera(true);
                minasMarcadas++;
                actualizarContadorMinas();
                btn.setText("🚩");
                btn.setForeground(new Color(255, 60, 60));
            }
        }
    }

    public void mostrarPuntuaciones(String dificultad) {
        JDialog dialog = new JDialog(this, "Puntuaciones - " + dificultad, true);

        GameDAO gameDAO = new GameDAO();
        List<Puntuacion> puntuacionesList = gameDAO.obtenerPuntuacionesPorDificultad(dificultad);

        if (puntuacionesList.size() > 10) {
            puntuacionesList = puntuacionesList.subList(0, 10);
        }

        Object[][] puntuaciones = new Object[puntuacionesList.size()][6];
        for (int i = 0; i < puntuacionesList.size(); i++) {
            Puntuacion puntuacion = puntuacionesList.get(i);
            puntuaciones[i][0] = puntuacion.getNombre();
            puntuaciones[i][1] = puntuacion.getClicks();
            puntuaciones[i][2] = puntuacion.getTiempo();
            puntuaciones[i][3] = puntuacion.getDificultad();
            puntuaciones[i][4] = puntuacion.getFecha();
            puntuaciones[i][5] = puntuacion.getHora();
        }

        String[] columnNames = { "Nombre", "Clicks", "Tiempo", "Dificultad", "Fecha", "Hora" };

        JTable table = new JTable(puntuaciones, columnNames);

        JScrollPane scrollPane = new JScrollPane(table);

        dialog.add(scrollPane);

        dialog.setSize(600, 300);

        dialog.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        menuDificultad = new javax.swing.JMenu();
        facil = new javax.swing.JMenuItem();
        medio = new javax.swing.JMenuItem();
        dificil = new javax.swing.JMenuItem();
        tamano = new javax.swing.JMenuItem();
        superHeroe = new javax.swing.JMenuItem();
        alien = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        juegoNuevo = new javax.swing.JMenuItem();
        menuPuntuaciones = new javax.swing.JMenu();
        puntuacionesFacil = new javax.swing.JMenuItem();
        puntuacionesMedio = new javax.swing.JMenuItem();
        puntuacionesDificil = new javax.swing.JMenuItem();
        puntuacionesPersonalizado = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        menuDificultad.setText("Dificultad");
        menuPuntuaciones.setText("Puntuaciones");

        puntuacionesDificil.setText("Dificil");

        puntuacionesMedio.setText("Medio");

        puntuacionesFacil.setText("Facil");

        puntuacionesPersonalizado.setText("Personalizado");

        puntuacionesFacil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarPuntuaciones("Facil");
            }
        });

        puntuacionesMedio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarPuntuaciones("Medio");
            }
        });

        puntuacionesDificil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarPuntuaciones("Dificil");
            }
        });

        puntuacionesPersonalizado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarPuntuaciones("Personalizado");
            }
        });

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

        superHeroe.setText("SuperHeroe");
        superHeroe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                superHeroeActionPerformed(evt);
            }
        });
        menuDificultad.add(superHeroe);

        alien.setText("Extraterrestre");
        alien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extraterrestreActionPerformed(evt);
            }
        });
        menuDificultad.add(alien);

        tamano.setText("Personalizado");
        tamano.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tamanoActionPerformed(evt);
            }
        });

        menuPuntuaciones.add(puntuacionesFacil);
        menuPuntuaciones.add(puntuacionesMedio);
        menuPuntuaciones.add(puntuacionesDificil);
        menuPuntuaciones.add(puntuacionesPersonalizado);

        menuDificultad.add(tamano);

        jMenuBar1.add(menuDificultad);
        jMenuBar1.add(menuPuntuaciones);

        jMenu1.setText("Juego");

        juegoNuevo.setText("Nuevo");
        juegoNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                juegoNuevoActionPerformed(evt);
            }
        });
        jMenu1.add(juegoNuevo);

        // Add Accelerators for Pro play
        juegoNuevo.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        facil.setAccelerator(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        medio.setAccelerator(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        dificil.setAccelerator(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_DOWN_MASK));

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 607, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 444, Short.MAX_VALUE));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void facilActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_facilActionPerformed
        this.numColumnas = 9;
        this.numFilas = 9;
        this.numMinas = 10;
        this.dificultad = "Principiante";
        juegoNuevo();
    }// GEN-LAST:event_facilActionPerformed

    private void tamanoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_tamanoActionPerformed
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField txtAncho = new JTextField("20");
        JTextField txtAlto = new JTextField("20");
        JTextField txtMinas = new JTextField("50");

        panel.add(new JLabel("Alto (Filas):"));
        panel.add(txtAlto);
        panel.add(new JLabel("Ancho (Columnas):"));
        panel.add(txtAncho);
        panel.add(new JLabel("Minas:"));
        panel.add(txtMinas);

        int result = JOptionPane.showConfirmDialog(null, panel, "Configuración Personalizada",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                this.numFilas = Integer.parseInt(txtAlto.getText());
                this.numColumnas = Integer.parseInt(txtAncho.getText());
                this.numMinas = Integer.parseInt(txtMinas.getText());
                this.dificultad = "Personalizado (" + numFilas + "x" + numColumnas + ")";
                juegoNuevo();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Por favor ingresa números válidos.");
            }
        }
    }// GEN-LAST:event_tamanoActionPerformed

    private void juegoNuevoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_juegoNuevoActionPerformed
        juegoNuevo();
    }// GEN-LAST:event_juegoNuevoActionPerformed

    private void medioActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_medioActionPerformed
        this.numColumnas = 16;
        this.numFilas = 16;
        this.numMinas = 40;
        this.dificultad = "Intermedio";
        juegoNuevo();
    }// GEN-LAST:event_medioActionPerformed

    private void dificilActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_dificilActionPerformed
        this.numColumnas = 30;
        this.numFilas = 16;
        this.numMinas = 99;
        this.dificultad = "Avanzado";
        juegoNuevo();
    }// GEN-LAST:event_dificilActionPerformed

    // NEW DIFFICULTIES

    private void superHeroeActionPerformed(java.awt.event.ActionEvent evt) {
        this.numColumnas = 50;
        this.numFilas = 50;
        this.numMinas = 500;
        this.dificultad = "SuperHeroe";
        juegoNuevo();
    }

    private void extraterrestreActionPerformed(java.awt.event.ActionEvent evt) {
        this.numColumnas = 100;
        this.numFilas = 100;
        this.numMinas = 2000;
        this.dificultad = "Extraterrestre";
        juegoNuevo();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // Customize some colors
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        // </editor-fold>

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
    private javax.swing.JMenu menuPuntuaciones;
    private javax.swing.JMenuItem puntuacionesFacil;
    private javax.swing.JMenuItem puntuacionesMedio;
    private javax.swing.JMenuItem puntuacionesPersonalizado;
    private javax.swing.JMenuItem puntuacionesDificil;
    private javax.swing.JMenuItem tamano;
    private javax.swing.JMenuItem superHeroe;
    private javax.swing.JMenuItem alien;
    // End of variables declaration//GEN-END:variables
}
