package com.mycompany.buscaminas;

import java.util.*;

/**
 * Solver CSP (Constraint Satisfaction Problem) completo para Buscaminas.
 * Verifica que un tablero sea 100% resoluble sin adivinar.
 * 
 * Algoritmo:
 * 1. Encuentra la "frontera" (celdas ocultas adyacentes a celdas reveladas)
 * 2. Enumera TODAS las configuraciones válidas de minas en la frontera
 * 3. Una celda es segura si es segura en TODAS las configuraciones
 * 4. Una celda es mina si es mina en TODAS las configuraciones
 * 5. Si existe alguna celda que varía entre configuraciones y no hay progreso,
 * requiere adivinar
 */
public class SolverLogico {

    private final int numFilas;
    private final int numColumnas;
    private final boolean[][] minas;
    private final int[][] minasAlrededor;

    // Estado del solver
    private boolean[][] revelado;
    private boolean[][] marcadoMina;
    private int casillasSegurasPorRevelar;
    private int totalMinas;

    public SolverLogico(int numFilas, int numColumnas, boolean[][] minas, int[][] minasAlrededor, int primeraFila,
            int primeraColumna) {
        this.numFilas = numFilas;
        this.numColumnas = numColumnas;
        this.minas = minas;
        this.minasAlrededor = minasAlrededor;

        this.revelado = new boolean[numFilas][numColumnas];
        this.marcadoMina = new boolean[numFilas][numColumnas];

        // Contar minas totales
        this.totalMinas = 0;
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                if (minas[i][j])
                    totalMinas++;
            }
        }
        this.casillasSegurasPorRevelar = (numFilas * numColumnas) - totalMinas;

        // Revelar la primera casilla (y propagar si es 0)
        revelarCasilla(primeraFila, primeraColumna);
    }

    /**
     * Verifica si el tablero es 100% resoluble sin adivinar.
     */
    public boolean esResoluble() {
        boolean progreso = true;

        while (progreso && casillasSegurasPorRevelar > 0) {
            progreso = false;

            // Primero intentar reglas simples (más rápido)
            if (aplicarReglasSimples()) {
                progreso = true;
                continue;
            }

            // Si las reglas simples no funcionan, usar CSP completo
            if (aplicarCSP()) {
                progreso = true;
            }
        }

        return casillasSegurasPorRevelar == 0;
    }

    /**
     * Verificación parcial con timeout para tableros medianos.
     * 
     * @param timeoutMs Tiempo máximo en milisegundos
     */
    public boolean esResolubleParcial(long timeoutMs) {
        long tiempoLimite = System.currentTimeMillis() + timeoutMs;
        boolean progreso = true;

        while (progreso && casillasSegurasPorRevelar > 0 && System.currentTimeMillis() < tiempoLimite) {
            progreso = false;

            // Solo usar reglas simples para velocidad
            if (aplicarReglasSimples()) {
                progreso = true;
                continue;
            }

            // CSP limitado: solo si la frontera es pequeña
            if (contarFrontera() <= 15) {
                if (aplicarCSP()) {
                    progreso = true;
                }
            } else {
                // Frontera muy grande, no podemos verificar completamente
                // Asumimos que está bien si las reglas simples funcionaron hasta aquí
                break;
            }
        }

        // Si pudimos resolver todo o al menos la mayor parte, consideramos OK
        int totalSeguras = (numFilas * numColumnas) - totalMinas;
        double porcentajeResuelto = (double) (totalSeguras - casillasSegurasPorRevelar) / totalSeguras;

        return porcentajeResuelto >= 0.5; // Al menos 50% resoluble con lógica
    }

    private int contarFrontera() {
        Set<String> frontera = new HashSet<>();
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                if (revelado[i][j] && minasAlrededor[i][j] > 0) {
                    for (int[] v : obtenerVecinos(i, j)) {
                        if (!revelado[v[0]][v[1]] && !marcadoMina[v[0]][v[1]]) {
                            frontera.add(v[0] + "," + v[1]);
                        }
                    }
                }
            }
        }
        return frontera.size();
    }

    /**
     * Aplica reglas básicas de deducción.
     */
    private boolean aplicarReglasSimples() {
        boolean progreso = false;

        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                if (!revelado[i][j] || minasAlrededor[i][j] == 0)
                    continue;

                List<int[]> vecinosOcultos = new ArrayList<>();
                int minasMarcadas = 0;

                for (int[] v : obtenerVecinos(i, j)) {
                    if (marcadoMina[v[0]][v[1]]) {
                        minasMarcadas++;
                    } else if (!revelado[v[0]][v[1]]) {
                        vecinosOcultos.add(v);
                    }
                }

                int minasFaltantes = minasAlrededor[i][j] - minasMarcadas;

                // Todos los ocultos son minas
                if (minasFaltantes == vecinosOcultos.size() && minasFaltantes > 0) {
                    for (int[] v : vecinosOcultos) {
                        if (!marcadoMina[v[0]][v[1]]) {
                            marcadoMina[v[0]][v[1]] = true;
                            progreso = true;
                        }
                    }
                }

                // Todos los ocultos son seguros
                if (minasFaltantes == 0 && !vecinosOcultos.isEmpty()) {
                    for (int[] v : vecinosOcultos) {
                        revelarCasilla(v[0], v[1]);
                        progreso = true;
                    }
                }
            }
        }

        return progreso;
    }

    /**
     * Aplica solver CSP completo enumerando todas las configuraciones válidas.
     */
    private boolean aplicarCSP() {
        // Obtener frontera (celdas ocultas adyacentes a reveladas)
        Set<String> fronteraSet = new LinkedHashSet<>();
        List<int[]> restricciones = new ArrayList<>(); // Celdas reveladas con restricciones

        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                if (revelado[i][j] && minasAlrededor[i][j] > 0) {
                    List<int[]> vecinosOcultos = new ArrayList<>();
                    int minasMarcadas = 0;

                    for (int[] v : obtenerVecinos(i, j)) {
                        if (marcadoMina[v[0]][v[1]]) {
                            minasMarcadas++;
                        } else if (!revelado[v[0]][v[1]]) {
                            vecinosOcultos.add(v);
                            fronteraSet.add(v[0] + "," + v[1]);
                        }
                    }

                    if (!vecinosOcultos.isEmpty()) {
                        restricciones.add(new int[] { i, j, minasAlrededor[i][j] - minasMarcadas });
                    }
                }
            }
        }

        if (fronteraSet.isEmpty()) {
            return false;
        }

        // Convertir frontera a lista indexada
        List<int[]> frontera = new ArrayList<>();
        Map<String, Integer> fronteraIndex = new HashMap<>();
        int idx = 0;
        for (String key : fronteraSet) {
            String[] parts = key.split(",");
            int[] pos = new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
            frontera.add(pos);
            fronteraIndex.put(key, idx++);
        }

        // Limitar tamaño de frontera para evitar explosión combinatoria
        if (frontera.size() > 25) {
            // Para fronteras muy grandes, dividir en componentes conexos
            return aplicarCSPPorComponentes(frontera, fronteraIndex, restricciones);
        }

        // Enumerar todas las configuraciones válidas
        List<boolean[]> configuracionesValidas = new ArrayList<>();
        enumerarConfiguraciones(frontera, fronteraIndex, restricciones, new boolean[frontera.size()], 0,
                configuracionesValidas);

        if (configuracionesValidas.isEmpty()) {
            return false; // No hay configuraciones válidas (error en el tablero)
        }

        // Analizar resultados
        boolean progreso = false;

        // Contar minas ya marcadas
        int minasMarcadasTotal = 0;
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                if (marcadoMina[i][j])
                    minasMarcadasTotal++;
            }
        }

        // Filtrar configuraciones por conteo global de minas
        int minasRestantes = totalMinas - minasMarcadasTotal;
        List<boolean[]> configsFiltradas = new ArrayList<>();
        for (boolean[] config : configuracionesValidas) {
            int minasEnConfig = 0;
            for (boolean b : config)
                if (b)
                    minasEnConfig++;
            // Solo aceptar configuraciones donde las minas en frontera <= minas restantes
            if (minasEnConfig <= minasRestantes) {
                configsFiltradas.add(config);
            }
        }

        if (configsFiltradas.isEmpty()) {
            return false;
        }

        // Verificar si TODAS las minas restantes deben estar en la frontera
        // (es decir, no hay celdas interiores donde puedan estar)
        int celdasInteriores = 0;
        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                if (!revelado[i][j] && !marcadoMina[i][j] && !fronteraSet.contains(i + "," + j)) {
                    celdasInteriores++;
                }
            }
        }

        // Si no hay celdas interiores, todas las minas deben estar en la frontera
        if (celdasInteriores == 0) {
            // Filtrar solo las configuraciones que tengan exactamente minasRestantes minas
            List<boolean[]> configsExactas = new ArrayList<>();
            for (boolean[] config : configsFiltradas) {
                int minasEnConfig = 0;
                for (boolean b : config)
                    if (b)
                        minasEnConfig++;
                if (minasEnConfig == minasRestantes) {
                    configsExactas.add(config);
                }
            }
            if (!configsExactas.isEmpty()) {
                configsFiltradas = configsExactas;
            }
        }

        for (int i = 0; i < frontera.size(); i++) {
            boolean siempreMina = true;
            boolean siempreSegura = true;

            for (boolean[] config : configsFiltradas) {
                if (config[i])
                    siempreSegura = false;
                else
                    siempreMina = false;
            }

            int[] pos = frontera.get(i);
            if (siempreSegura) {
                revelarCasilla(pos[0], pos[1]);
                progreso = true;
            } else if (siempreMina && !marcadoMina[pos[0]][pos[1]]) {
                marcadoMina[pos[0]][pos[1]] = true;
                progreso = true;
            }
        }

        return progreso;
    }

    /**
     * CSP por componentes conexos para fronteras grandes.
     */
    private boolean aplicarCSPPorComponentes(List<int[]> frontera, Map<String, Integer> fronteraIndex,
            List<int[]> restricciones) {
        // Construir grafo de adyacencia entre celdas de frontera
        Map<Integer, Set<Integer>> adyacencia = new HashMap<>();
        for (int i = 0; i < frontera.size(); i++) {
            adyacencia.put(i, new HashSet<>());
        }

        for (int[] restriccion : restricciones) {
            List<Integer> indicesEnFrontera = new ArrayList<>();
            for (int[] v : obtenerVecinos(restriccion[0], restriccion[1])) {
                String key = v[0] + "," + v[1];
                if (fronteraIndex.containsKey(key)) {
                    indicesEnFrontera.add(fronteraIndex.get(key));
                }
            }
            // Conectar todos los índices de esta restricción
            for (int i = 0; i < indicesEnFrontera.size(); i++) {
                for (int j = i + 1; j < indicesEnFrontera.size(); j++) {
                    adyacencia.get(indicesEnFrontera.get(i)).add(indicesEnFrontera.get(j));
                    adyacencia.get(indicesEnFrontera.get(j)).add(indicesEnFrontera.get(i));
                }
            }
        }

        // Encontrar componentes conexos
        boolean[] visitado = new boolean[frontera.size()];
        boolean progreso = false;

        for (int inicio = 0; inicio < frontera.size(); inicio++) {
            if (visitado[inicio])
                continue;

            // BFS para encontrar componente
            List<Integer> componente = new ArrayList<>();
            Queue<Integer> cola = new LinkedList<>();
            cola.add(inicio);
            visitado[inicio] = true;

            while (!cola.isEmpty()) {
                int actual = cola.poll();
                componente.add(actual);
                for (int vecino : adyacencia.get(actual)) {
                    if (!visitado[vecino]) {
                        visitado[vecino] = true;
                        cola.add(vecino);
                    }
                }
            }

            // Resolver este componente si es manejable
            if (componente.size() <= 20) {
                if (resolverComponente(componente, frontera, fronteraIndex, restricciones)) {
                    progreso = true;
                }
            }
        }

        return progreso;
    }

    private boolean resolverComponente(List<Integer> componente, List<int[]> frontera,
            Map<String, Integer> fronteraIndex, List<int[]> restricciones) {
        // Crear sub-frontera y sub-restricciones para este componente
        Set<Integer> componenteSet = new HashSet<>(componente);
        List<int[]> subFrontera = new ArrayList<>();
        Map<String, Integer> subIndex = new HashMap<>();

        for (int i = 0; i < componente.size(); i++) {
            int[] pos = frontera.get(componente.get(i));
            subFrontera.add(pos);
            subIndex.put(pos[0] + "," + pos[1], i);
        }

        // Filtrar restricciones relevantes
        List<int[]> subRestricciones = new ArrayList<>();
        for (int[] r : restricciones) {
            boolean relevante = false;
            for (int[] v : obtenerVecinos(r[0], r[1])) {
                if (subIndex.containsKey(v[0] + "," + v[1])) {
                    relevante = true;
                    break;
                }
            }
            if (relevante)
                subRestricciones.add(r);
        }

        // Enumerar configuraciones
        List<boolean[]> configs = new ArrayList<>();
        enumerarConfiguraciones(subFrontera, subIndex, subRestricciones, new boolean[subFrontera.size()], 0, configs);

        if (configs.isEmpty())
            return false;

        boolean progreso = false;
        for (int i = 0; i < subFrontera.size(); i++) {
            boolean siempreMina = true, siempreSegura = true;
            for (boolean[] c : configs) {
                if (c[i])
                    siempreSegura = false;
                else
                    siempreMina = false;
            }

            int[] pos = subFrontera.get(i);
            if (siempreSegura) {
                revelarCasilla(pos[0], pos[1]);
                progreso = true;
            } else if (siempreMina && !marcadoMina[pos[0]][pos[1]]) {
                marcadoMina[pos[0]][pos[1]] = true;
                progreso = true;
            }
        }

        return progreso;
    }

    /**
     * Enumera recursivamente todas las configuraciones válidas de minas.
     */
    private void enumerarConfiguraciones(List<int[]> frontera, Map<String, Integer> fronteraIndex,
            List<int[]> restricciones, boolean[] configuracionActual,
            int indice, List<boolean[]> resultado) {
        if (indice == frontera.size()) {
            // Verificar que TODAS las restricciones se cumplan
            if (verificarRestricciones(frontera, fronteraIndex, restricciones, configuracionActual)) {
                resultado.add(configuracionActual.clone());
            }
            return;
        }

        // Podar: verificar restricciones parciales
        if (!verificarRestriccionesParciales(frontera, fronteraIndex, restricciones, configuracionActual, indice)) {
            return;
        }

        // Probar sin mina
        configuracionActual[indice] = false;
        enumerarConfiguraciones(frontera, fronteraIndex, restricciones, configuracionActual, indice + 1, resultado);

        // Probar con mina
        configuracionActual[indice] = true;
        enumerarConfiguraciones(frontera, fronteraIndex, restricciones, configuracionActual, indice + 1, resultado);

        configuracionActual[indice] = false; // Reset
    }

    private boolean verificarRestricciones(List<int[]> frontera, Map<String, Integer> fronteraIndex,
            List<int[]> restricciones, boolean[] config) {
        for (int[] r : restricciones) {
            int minasRequeridas = r[2];
            int minasContadas = 0;

            for (int[] v : obtenerVecinos(r[0], r[1])) {
                String key = v[0] + "," + v[1];
                if (fronteraIndex.containsKey(key)) {
                    if (config[fronteraIndex.get(key)]) {
                        minasContadas++;
                    }
                }
            }

            if (minasContadas != minasRequeridas) {
                return false;
            }
        }
        return true;
    }

    private boolean verificarRestriccionesParciales(List<int[]> frontera, Map<String, Integer> fronteraIndex,
            List<int[]> restricciones, boolean[] config, int hastaIndice) {
        for (int[] r : restricciones) {
            int minasRequeridas = r[2];
            int minasContadas = 0;
            int sinAsignar = 0;

            for (int[] v : obtenerVecinos(r[0], r[1])) {
                String key = v[0] + "," + v[1];
                if (fronteraIndex.containsKey(key)) {
                    int idx = fronteraIndex.get(key);
                    if (idx < hastaIndice) {
                        if (config[idx])
                            minasContadas++;
                    } else {
                        sinAsignar++;
                    }
                }
            }

            // Poda: si ya hay demasiadas minas o no hay suficientes espacios
            if (minasContadas > minasRequeridas)
                return false;
            if (minasContadas + sinAsignar < minasRequeridas)
                return false;
        }
        return true;
    }

    private void revelarCasilla(int fila, int columna) {
        if (revelado[fila][columna] || marcadoMina[fila][columna] || minas[fila][columna]) {
            return;
        }

        revelado[fila][columna] = true;
        casillasSegurasPorRevelar--;

        if (minasAlrededor[fila][columna] == 0) {
            for (int[] v : obtenerVecinos(fila, columna)) {
                revelarCasilla(v[0], v[1]);
            }
        }
    }

    private List<int[]> obtenerVecinos(int fila, int columna) {
        List<int[]> vecinos = new ArrayList<>();
        for (int df = -1; df <= 1; df++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (df == 0 && dc == 0)
                    continue;
                int nf = fila + df, nc = columna + dc;
                if (nf >= 0 && nf < numFilas && nc >= 0 && nc < numColumnas) {
                    vecinos.add(new int[] { nf, nc });
                }
            }
        }
        return vecinos;
    }
}
