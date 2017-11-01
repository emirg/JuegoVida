/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

import java.util.Random;

/**
 *
 * @author Emi
 */
public class Mundo {

    private Celula[][] mundo;
    private int cantVivas;

    public Mundo(int filas, int cols) {
        this.mundo = new Celula[filas][cols];
        this.cantVivas = 0;
    }

    public Mundo(Celula[][] mundo) {
        this.mundo = mundo;
        this.cantVivas = 0;
    }

    public void setMundo(Celula[][] mundo) {
        this.mundo = mundo;
    }

    public void setCantVivas(int cantVivas) {
        this.cantVivas = cantVivas;
    }

    public Celula[][] getMundo() {
        return mundo;
    }

    public int getCantVivas() {
        return cantVivas;
    }

    public  void cambiarCantVivas(boolean estado) {
        cantVivas += (estado) ? 1 : -1;
    }

    public void inicializarMundo() {
        for (int i = 0; i < mundo.length; i++) {
            for (int j = 0; j < mundo[0].length; j++) {
                mundo[i][j] = new Celula(i, j);
            }
        }
    }

}
