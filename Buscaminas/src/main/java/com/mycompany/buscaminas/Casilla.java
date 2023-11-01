package buscaminas;

public class Casilla {
	
	int posFila;
    int posColumna;
    boolean mina;
    int minasAlrededor;
    boolean Abierta;

    public Casilla(int posFila, int posColumna) {
        this.posFila = posFila;
        this.posColumna = posColumna;
    }
    
    

    public int getPosFila() {
        return posFila;
    }

    public void setPosFila(int posFila) {
        this.posFila = posFila;
    }

    public int getPosColumna() {
        return posColumna;
    }

    public void setPosColumna(int posColumna) {
        this.posColumna = posColumna;
    }

    public boolean isMina() {
        return mina;
    }

    public void setMina(boolean mina) {
        this.mina = mina;
    }
    
    
    
    public int getMinasAlrededor() {
		return minasAlrededor;
	}



	public void setMinasAlrededor(int minasAlrededor) {
		this.minasAlrededor = minasAlrededor;
	}



	public void inrementarNumeroMinasAlrededor()
    {
    	this.minasAlrededor++;
    }



	public boolean isAbierta() {
		return Abierta;
	}



	public void setAbierta(boolean abierta) {
		Abierta = abierta;
	}
}
