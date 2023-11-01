package buscaminas;

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
    
    public void initCasillas()
    {
        casillas = new Casilla[this.numFilas][this.numColumnas];
        
        for(int i = 0; i < casillas.length; i++)
        {
            for(int j = 0; j<casillas[i].length;j++)
            {
                casillas[i][j] = new Casilla(i,j);
            }
        }
        
        generacionDeMinas();
    }
    
    private void generacionDeMinas()
    {
        int minasGeneradas = 0;
        
        while(minasGeneradas!=numMinas)
        {
            int posFila =(int)(Math.random()*casillas.length);
            int posColumna =(int)(Math.random()*casillas[0].length);
            
            if(!casillas[posFila][posColumna].isMina())
            {
                casillas[posFila][posColumna].setMina(true);
                minasGeneradas++;
            }
        }
        actualizarNumeroMinasAlrededor();
    }
    
    public void mostrarTablero()
    {
        for(int i = 0; i < casillas.length; i++)
        {
            for(int j = 0; j<casillas[i].length;j++)
            {
                System.out.print(casillas[i][j].isMina()?"*":"0");
            }
            System.out.println("");
        }
    }
    
    public void mostrarPistas()
    {
        for(int i = 0; i < casillas.length; i++)
        {
            for(int j = 0; j<casillas[i].length;j++)
            {
                System.out.print(casillas[i][j].getMinasAlrededor());
            }
            System.out.println("");
        }
    }
    
    private void actualizarNumeroMinasAlrededor()
    {
    	for(int i = 0; i < casillas.length; i++)
        {
            for(int j = 0; j<casillas[i].length;j++)
            {
                if(casillas[i][j].isMina())
                {
                	List<Casilla> casillasAlrededor = obtenerCasillaAlrededor(i, j);
                	casillasAlrededor.forEach((c)->c.inrementarNumeroMinasAlrededor());
                }
            }
        }
    }
    
    private List<Casilla> obtenerCasillaAlrededor(int posFila, int posColumna)
    {
    	List<Casilla> listaCasillas = new LinkedList<>();
    	for(int i = 0; i<8;i++)
    	{
    		int tmpPosFila=posFila;
    		int tmpPosColumna=posColumna;
    		switch(i)
    		{
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
    		
    		if(tmpPosFila>=0 && tmpPosFila<this.casillas.length && tmpPosColumna>=0 && tmpPosColumna<this.casillas[0].length)
    		{
    			listaCasillas.add(this.casillas[tmpPosFila][tmpPosColumna]);
    		}
    	}
    	return listaCasillas;
    }
    
    List<Casilla> obtenerCasillasConMinas()
    {
    	List<Casilla> casillasConMinas = new LinkedList<>();
		for(int i = 0; i < casillas.length; i++)
        {
            for(int j = 0; j<casillas[i].length;j++)
            {
                if(casillas[i][j].isMina())
                {
                	casillasConMinas.add(casillas[i][j]);
                }
            }
        }
		
		return casillasConMinas;
    }
    
    public void seleccionarCasilla(int posFila, int posColumna)
    {
    	eventoCasillaAbierta.accept(this.casillas[posFila][posColumna]);
    	if(this.casillas[posFila][posColumna].isMina())
    	{
    		eventoPartidaPerdida.accept(obtenerCasillasConMinas());
    		
    	}else if(this.casillas[posFila][posColumna].getMinasAlrededor() == 0)
    	{
    		marcarCasillaAbierta(posFila, posColumna);
    		List<Casilla> casillasAlrededor = obtenerCasillaAlrededor(posFila, posColumna);
    		for(Casilla casilla: casillasAlrededor)
    		{
    			if(!casilla.isAbierta())
    			{
    				seleccionarCasilla(casilla.getPosFila(), casilla.getPosColumna());	
    			}
    		}
    	}else
    	{
    		marcarCasillaAbierta(posFila, posColumna);
    	}
 
    	if(partidaGanada())
    	{
    		eventoPartidaGanada.accept(obtenerCasillasConMinas());
    	}
    }
    
    void marcarCasillaAbierta(int posFila, int posColumna)
    {
    	if(!this.casillas[posFila][posColumna].isAbierta())
    	{
    		numCasillasAbiertas++;
    		this.casillas[posFila][posColumna].setAbierta(true);
    	}
    }
    
    boolean partidaGanada()
    {
    	return numCasillasAbiertas>=((numFilas*numColumnas)-numMinas);
    }
    
    public static void main(String[] args) {
        Tablero tablero = new Tablero(6,6,5);
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
	
	
    
    
}
