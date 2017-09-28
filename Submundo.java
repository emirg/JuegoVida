/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Alumno: Emiliano Rios Gavagnin
Legajo: FAI-1113
Trabajo: Juego de la vida
*/

/**
 *
 * @author Emi
 */
public class Submundo implements Runnable {

    private final CyclicBarrier esperarVerificacion;
    private final CyclicBarrier esperarCambio;
    private final Mundo mundo;

    //Variables utilizadas para determinar los limites de cada submundo
    private final int inicioFila;
    private final int inicioCol;
    private final int finFila;
    private final int finCol;

    public Submundo(Mundo mundo, int inicioFila, int finFila, int inicioCol, int finCol, CyclicBarrier esperarVerificacion, CyclicBarrier esperarCambio) {
        this.esperarVerificacion = esperarVerificacion;
        this.esperarCambio = esperarCambio;
        this.mundo = mundo;
        this.inicioFila = inicioFila;
        this.inicioCol = inicioCol;
        this.finFila = finFila;
        this.finCol = finCol;
    }

    private void verificarVecinas(int i, int j) {

        int n = -1; //Usada para iterar sobre las filas
        int m = -1; //Usada para iterar sobre las columnas

        int cantVivas = 0; //Usada para contar la cantidad de celulas vecinas vivas

        for (int k = 0; k <= 8; k++) { //Itero sobre todas las vecinas de una celula[i][j] y compruebo el estado de las mismas para indicar si esta debe cambiar o no
            int longitudFilas = mundo.getMundo().length - 1, longitudCol = mundo.getMundo()[0].length - 1;
            int zero = 0;
            int fila = n + i; //Variable utilizada para indicar el indice de la fila de la celula vecina
            int col = m + j;//Variable utilizada para indicar el indice de la columna de la celula vecina

            if (n != 0 || m != 0) { //Si la celula[fila][col] != celula[i][j] (No me interesa saber el estado de la celula actual, por ahora...)
                fila = (fila <= -1) ? longitudFilas : ((fila >= mundo.getMundo().length) ? 0 : fila); //Si la variable fila es una posicion invalida, entonces le asigno el valor correspondiente a su otro extremo (Es decir, actua de forma de esfera)
                col = (col <= -1) ? longitudCol : ((col >= mundo.getMundo()[0].length) ? 0 : col); //Si la variable columna es una posicion invalida, entonces le asigno el valor correspondiente a su otro extremo (Es decir, actua de forma de esfera)
                if (mundo.getMundo()[fila][col].getEstado()) { //Si la celula vecina esta viva la sumo a un contador
                    cantVivas++;
                }
            }

            n = (m == 1) ? n + 1 : n; //Si termine de recorrer las columnas, aumento en 1 la fila, sino dejo la variable como esta para seguir en la misma fila
            m = (m == 1) ? (-1) : m + 1; //Si termine de recorrer las columnas, vuelvo a m = -1 para la nueva fila, sino aumento en 1 la columna

        }

        Celula actual = mundo.getMundo()[i][j];
        actual.verificarSiNecesitaCambiar(cantVivas);
     //   if (!actual.getEstado()) { //Compruebo el estado de vida de la celula, y segun ello y la cantidad de vecinas vivas procedo a comprobar si necesito cambiar su estado o no
            //Si esta muerta...
       //     if (cantVivas == 3) {
         //       actual.setCambiar(true);
           // }
       // } else {
            //Si esta viva...
         //   if (cantVivas != 2 && cantVivas != 3) {
           //     actual.setCambiar(true);
           // }

        //}

    }

    private void verificarSubmundo() {

        for (int i = inicioFila; i <= finFila; i++) {
            for (int j = inicioCol; j <= finCol; j++) {
                verificarVecinas(i, j); //Verifico las vecinas de cada celula del submundo
            }
        }

    }

    private void cambiarSubmundo() {
        //Se encarga de modificar cada submundo segun el estado del atributo "cambiar" de Celula
        //Se podria hacer synchronized, pero como cada submundo solo cambia las celulas de su submundo, no hay riesgo que otro submundo cambie otra celula que no sea de su espacio
        for (int i = inicioFila; i <= finFila; i++) {
            for (int j = inicioCol; j <= finCol; j++) {
                Celula temp = mundo.getMundo()[i][j];
                if (temp.getCambiar()) { //Si necesita cambiar...
                    temp.setEstado(!temp.getEstado());
                    temp.setCambiar(false);
                }
            }
        }

    }

    @Override
    public void run() {
        while (true) {
            verificarSubmundo(); //Verifico el submundo y establezco cuales deben cambiar
            try {

                esperarVerificacion.await();

            } catch (InterruptedException | BrokenBarrierException e) {
                Logger.getLogger(Submundo.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                if (esperarVerificacion.isBroken()) {
                    esperarVerificacion.reset();
                }

                cambiarSubmundo(); //Reviso cuales celulas debo cambiar y las cambio

                try {

                    esperarCambio.await();

                } catch (InterruptedException | BrokenBarrierException e) {
                } finally {
                    if (esperarCambio.isBroken()) {
                        esperarCambio.reset();
                    }
                }

            }
        }
    }

}
