/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

import java.util.Observable;

/*
Alumno: Emiliano Rios Gavagnin
Legajo: FAI-1113
Trabajo: Juego de la vida
 */
/**
 *
 * @author Emi
 */
public class Celula extends Observable {

    private boolean viva; //Booleano que indica si la celula esta viva o muerta; viva -> true, muerta -> false
    private boolean cambiar; //Booleano que indica si la celula debe cambiar de estado durante el proceso de cambio del mundo
    public int i;
    public int j;

    public Celula() {
        this.viva = false;
        this.cambiar = false;
    }

    public Celula(int i, int j) {
        this.viva = false;
        this.cambiar = false;
        this.i = i;
        this.j = j;
    }

    public Celula(boolean estado) {

        this.viva = estado;
        this.cambiar = false;
    }

    public void setViva(boolean estado) {
        this.viva = estado;
        setChanged();
        notifyObservers();

    }

    public boolean isViva() {
        return viva;
    }

    public boolean getCambiar() {
        return cambiar;
    }

    public void setCambiar(boolean cambiar) {
        this.cambiar = cambiar;
    }

    public void verificarSiNecesitaCambiar(int cantVivas) {
        if (!this.isViva()) { //Compruebo el estado de vida de la celula, y segun ello y la cantidad de vecinas vivas procedo a comprobar si necesito cambiar su estado o no
            //Si esta muerta...
            if (cantVivas == 3) {
                this.setCambiar(true);
            }
        } else {
            //Si esta viva...
            if (cantVivas != 2 && cantVivas != 3) {
                this.setCambiar(true);
            }

        }
    }

}
