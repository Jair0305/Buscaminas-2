package com.mycompany.buscaminas;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Tablero {

    Casilla[][] casillas;

    int numFilas;
    int numColumnas;
    int numMinas;

    int numCasillasAbiertas;
    boolean juegoTerminado;

    private Consumer<List<Casilla>> eventoPartidaPerdida;
    public Consumer<List<Casilla>> eventoPartidaGanada;
    private Consumer<Casilla> eventoCasillaAbierta;

    public Tablero(int numFilas, int numColumnas, int numMinas) {
        this.numFilas = numFilas;
        this.numColumnas = numColumnas;
        this.numMinas = numMinas;

        initCasillas();
    }

    public void initCasillas() {
        casillas = new Casilla[this.numFilas][this.numColumnas];

        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                casillas[i][j] = new Casilla(i, j);
            }
        }

    }

    private void generacionDeMinas(int primeraFila, int primeraColumna) {
        // Paso 1: Generar minas iniciales aleatoriamente
        int minasGeneradas = 0;
        while (minasGeneradas < numMinas) {
            int posFila = (int) (Math.random() * casillas.length);
            int posColumna = (int) (Math.random() * casillas[0].length);

            if (!casillas[posFila][posColumna].isMina()
                    && !estaAlrededor(primeraFila, primeraColumna, posFila, posColumna)) {
                casillas[posFila][posColumna].setMina(true);
                minasGeneradas++;
            }
        }

        // Paso 2: Hacer el tablero resoluble mediante relocación de minas
        hacerResoluble(primeraFila, primeraColumna);
    }

    /**
     * Algoritmo de relocación de minas para garantizar solubilidad.
     * Simula el juego y cuando se atasca, mueve minas problemáticas.
     */
    private void hacerResoluble(int primeraFila, int primeraColumna) {
        int maxIteraciones = 10000;
        int iteracion = 0;

        while (iteracion < maxIteraciones) {
            iteracion++;

            // Recalcular números
            limpiarNumeros();
            actualizarNumeroMinasAlrededor();

            // Simular juego con solver
            boolean[][] revelado = new boolean[numFilas][numColumnas];
            boolean[][] marcadoMina = new boolean[numFilas][numColumnas];

            // Revelar primera casilla y propagar ceros
            revelarSimulado(primeraFila, primeraColumna, revelado);

            boolean progreso = true;
            while (progreso) {
                progreso = false;

                // Aplicar reglas simples
                for (int i = 0; i < numFilas; i++) {
                    for (int j = 0; j < numColumnas; j++) {
                        if (!revelado[i][j])
                            continue;
                        int num = casillas[i][j].getMinasAlrededor();
                        if (num == 0)
                            continue;

                        List<int[]> ocultos = new java.util.ArrayList<>();
                        int marcadas = 0;

                        for (Casilla c : obtenerCasillaAlrededor(i, j)) {
                            int vf = c.getPosFila(), vc = c.getPosColumna();
                            if (marcadoMina[vf][vc])
                                marcadas++;
                            else if (!revelado[vf][vc])
                                ocultos.add(new int[] { vf, vc });
                        }

                        int faltantes = num - marcadas;

                        // Todos ocultos son minas
                        if (faltantes == ocultos.size() && faltantes > 0) {
                            for (int[] v : ocultos) {
                                if (!marcadoMina[v[0]][v[1]]) {
                                    marcadoMina[v[0]][v[1]] = true;
                                    progreso = true;
                                }
                            }
                        }

                        // Todos ocultos son seguros
                        if (faltantes == 0 && !ocultos.isEmpty()) {
                            for (int[] v : ocultos) {
                                revelarSimulado(v[0], v[1], revelado);
                                progreso = true;
                            }
                        }
                    }
                }
            }

            // Verificar si ganamos
            int seguras = 0;
            for (int i = 0; i < numFilas; i++) {
                for (int j = 0; j < numColumnas; j++) {
                    if (!casillas[i][j].isMina() && revelado[i][j])
                        seguras++;
                }
            }

            int totalSeguras = (numFilas * numColumnas) - numMinas;
            if (seguras == totalSeguras) {
                System.out.println("Tablero resoluble generado en " + iteracion + " iteración(es).");
                return; // ¡Éxito!
            }

            // No se pudo resolver: encontrar y reubicar una mina problemática
            // Buscar una celda oculta en la frontera que cause ambigüedad
            List<int[]> frontera = new java.util.ArrayList<>();
            for (int i = 0; i < numFilas; i++) {
                for (int j = 0; j < numColumnas; j++) {
                    if (!revelado[i][j] && !marcadoMina[i][j] && tieneVecinoRevelado(i, j, revelado)) {
                        frontera.add(new int[] { i, j });
                    }
                }
            }

            if (frontera.isEmpty()) {
                // No hay frontera, hay celdas aisladas - mover minas de ahí
                for (int i = 0; i < numFilas; i++) {
                    for (int j = 0; j < numColumnas; j++) {
                        if (!revelado[i][j] && casillas[i][j].isMina()) {
                            moverMinaA(i, j, primeraFila, primeraColumna, revelado, marcadoMina);
                            break;
                        }
                    }
                }
            } else {
                // Buscar una mina en la frontera y moverla
                boolean movida = false;
                for (int[] pos : frontera) {
                    if (casillas[pos[0]][pos[1]].isMina()) {
                        moverMinaA(pos[0], pos[1], primeraFila, primeraColumna, revelado, marcadoMina);
                        movida = true;
                        break;
                    }
                }

                if (!movida) {
                    // No hay minas en la frontera, el problema está en otro lado
                    // Mover una mina random de área no revelada a área revelada-segura
                    for (int i = 0; i < numFilas; i++) {
                        for (int j = 0; j < numColumnas; j++) {
                            if (casillas[i][j].isMina() && !marcadoMina[i][j]) {
                                moverMinaA(i, j, primeraFila, primeraColumna, revelado, marcadoMina);
                                break;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("ADVERTENCIA: No se logró hacer resoluble en " + maxIteraciones + " iteraciones.");
    }

    private void revelarSimulado(int fila, int columna, boolean[][] revelado) {
        if (fila < 0 || fila >= numFilas || columna < 0 || columna >= numColumnas)
            return;
        if (revelado[fila][columna] || casillas[fila][columna].isMina())
            return;

        revelado[fila][columna] = true;

        if (casillas[fila][columna].getMinasAlrededor() == 0) {
            for (Casilla c : obtenerCasillaAlrededor(fila, columna)) {
                revelarSimulado(c.getPosFila(), c.getPosColumna(), revelado);
            }
        }
    }

    private boolean tieneVecinoRevelado(int fila, int columna, boolean[][] revelado) {
        for (Casilla c : obtenerCasillaAlrededor(fila, columna)) {
            if (revelado[c.getPosFila()][c.getPosColumna()])
                return true;
        }
        return false;
    }

    private void moverMinaA(int filaOrigen, int colOrigen, int primeraFila, int primeraColumna,
            boolean[][] revelado, boolean[][] marcadoMina) {
        // Quitar mina de origen
        casillas[filaOrigen][colOrigen].setMina(false);

        // Buscar nueva posición válida (no revelada, no marcada, no alrededor del
        // primer click)
        java.util.List<int[]> candidatos = new java.util.ArrayList<>();
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                if (!casillas[i][j].isMina() && !revelado[i][j] && !marcadoMina[i][j]
                        && !estaAlrededor(primeraFila, primeraColumna, i, j)
                        && (i != filaOrigen || j != colOrigen)) {
                    candidatos.add(new int[] { i, j });
                }
            }
        }

        if (candidatos.isEmpty()) {
            // No hay donde mover, restaurar
            casillas[filaOrigen][colOrigen].setMina(true);
            return;
        }

        // Elegir posición aleatoria
        int[] nuevo = candidatos.get((int) (Math.random() * candidatos.size()));
        casillas[nuevo[0]][nuevo[1]].setMina(true);
    }

    private void limpiarMinas() {
        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                casillas[i][j].setMina(false);
            }
        }
    }

    private void limpiarNumeros() {
        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                casillas[i][j].setMinasAlrededor(0);
            }
        }
    }

    private boolean estaAlrededor(int filaCentral, int columnaCentral, int fila, int columna) {
        return Math.abs(filaCentral - fila) <= 1 && Math.abs(columnaCentral - columna) <= 1;
    }

    public void mostrarTablero() {
        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                System.out.print(casillas[i][j].isMina() ? "*" : "0");
            }
            System.out.println("");
        }
    }

    public void mostrarPistas() {
        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                System.out.print(casillas[i][j].getMinasAlrededor());
            }
            System.out.println("");
        }
    }

    private void actualizarNumeroMinasAlrededor() {
        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                if (casillas[i][j].isMina()) {
                    List<Casilla> casillasAlrededor = obtenerCasillaAlrededor(i, j);
                    casillasAlrededor.forEach((c) -> c.incrementarNumeroMinasAlrededor());
                }
            }
        }
    }

    public List<Casilla> obtenerCasillaAlrededor(int posFila, int posColumna) {
        List<Casilla> listaCasillas = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            int tmpPosFila = posFila;
            int tmpPosColumna = posColumna;
            switch (i) {
                case 0:
                    tmpPosFila--;
                    break;
                case 1:
                    tmpPosFila--;
                    tmpPosColumna++;
                    break;
                case 2:
                    tmpPosColumna++;
                    break;
                case 3:
                    tmpPosColumna++;
                    tmpPosFila++;
                    break;
                case 4:
                    tmpPosFila++;
                    break;
                case 5:
                    tmpPosFila++;
                    tmpPosColumna--;
                    break;
                case 6:
                    tmpPosColumna--;
                    break;
                case 7:
                    tmpPosFila--;
                    tmpPosColumna--;
                    break;
            }

            if (tmpPosFila >= 0 && tmpPosFila < this.casillas.length && tmpPosColumna >= 0
                    && tmpPosColumna < this.casillas[0].length) {
                listaCasillas.add(this.casillas[tmpPosFila][tmpPosColumna]);
            }
        }
        return listaCasillas;
    }

    List<Casilla> obtenerCasillasConMinas() {
        List<Casilla> casillasConMinas = new LinkedList<>();
        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                if (casillas[i][j].isMina()) {
                    casillasConMinas.add(casillas[i][j]);
                }
            }
        }

        return casillasConMinas;
    }

    public void seleccionarCasilla(int posFila, int posColumna) {
        if (this.casillas[posFila][posColumna].isBandera()) {
            return;
        }

        if (numCasillasAbiertas == 0) {
            generacionDeMinas(posFila, posColumna);
        }

        eventoCasillaAbierta.accept(this.casillas[posFila][posColumna]);
        if (this.casillas[posFila][posColumna].isMina()) {
            eventoPartidaPerdida.accept(obtenerCasillasConMinas());

        } else if (this.casillas[posFila][posColumna].getMinasAlrededor() == 0) {
            marcarCasillaAbierta(posFila, posColumna);
            List<Casilla> casillasAlrededor = obtenerCasillaAlrededor(posFila, posColumna);
            for (Casilla casilla : casillasAlrededor) {
                if (!casilla.isAbierta()) {
                    seleccionarCasilla(casilla.getPosFila(), casilla.getPosColumna());
                }
            }
        } else {
            marcarCasillaAbierta(posFila, posColumna);
        }

        if (partidaGanada()) {
            eventoPartidaGanada.accept(obtenerCasillasConMinas());
        }
    }

    void marcarCasillaAbierta(int posFila, int posColumna) {
        if (!this.casillas[posFila][posColumna].isAbierta()) {
            numCasillasAbiertas++;
            this.casillas[posFila][posColumna].setAbierta(true);
        }
    }

    boolean partidaGanada() {
        return numCasillasAbiertas >= ((numFilas * numColumnas) - numMinas);
    }

    public static void main(String[] args) {
        Tablero tablero = new Tablero(6, 6, 5);
        tablero.mostrarTablero();
        System.out.println("---");
        tablero.mostrarPistas();
    }

    public Consumer<List<Casilla>> getEventoPartidaPerdida() {
        return eventoPartidaPerdida;
    }

    public void setEventoPartidaPerdida(Consumer<List<Casilla>> eventoPartidaPerdida) {
        this.eventoPartidaPerdida = eventoPartidaPerdida;
    }

    public Consumer<Casilla> getEventoCasillaAbierta() {
        return eventoCasillaAbierta;
    }

    public void setEventoCasillaAbierta(Consumer<Casilla> eventoCasillaAbierta) {
        this.eventoCasillaAbierta = eventoCasillaAbierta;
    }

    public Consumer<List<Casilla>> getEventoPartidaGanada() {
        return eventoPartidaGanada;
    }

    public void setEventoPartidaGanada(Consumer<List<Casilla>> eventoPartidaGanada) {
        this.eventoPartidaGanada = eventoPartidaGanada;
    }

    public void marcarCasilla(int posFila, int posColumna) {
        this.casillas[posFila][posColumna].setBandera(true);
    }

    public void desmarcarCasilla(int posFila, int posColumna) {
        this.casillas[posFila][posColumna].setBandera(false);
    }
}
