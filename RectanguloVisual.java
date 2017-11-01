/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

import static JuegoVida.Juego.getColorVida;
import static JuegoVida.Juego.getColorMuerte;
import java.util.Observable;
import java.util.Observer;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Emi
 */
public class RectanguloVisual extends Rectangle implements Observer {

    public int fila;
    public int col;

    public RectanguloVisual(int fila, int col) {
        super();
        this.fila = fila;
        this.col = col;
    }

    @Override
    public void update(Observable o, Object arg) {
        Celula c = (Celula) o;
        if (c.isViva()) {
            System.out.println("Antes de cambiar: " + this.getFill().toString() + ". " + fila+" , "+col);
            this.setFill(Paint.valueOf(getColorVida()));

           System.out.println("Despues de cambiar: " + this.getFill().toString() + ". " + fila+" , "+col);
            System.out.println("Cambio: " + c.i + " , " + c.j + " a viva. Visual: " + this.fila + " , " + this.col);
        } else {
                 System.out.println("Antes de cambiar: " + this.getFill().toString() + ". " + fila+" , "+col);
            this.setFill(Paint.valueOf(getColorMuerte()));
           System.out.println("Despues de cambiar: " + this.getFill().toString() + ". " + fila+" , "+col);
            System.out.println("Cambio: " + c.i + " , " + c.j + " a muerta. Visual: " + this.fila + " , " + this.col);
        }
        //System.out.println("Cambie");

    }

}
