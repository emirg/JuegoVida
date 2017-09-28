/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


/*
Alumno: Emiliano Rios Gavagnin
Legajo: FAI-1113
Trabajo: Juego de la vida
*/

/**
 *
 * @author Emi
 */


public class Mundo implements Runnable {

    private Celula[][] mundo;
    private final CyclicBarrier esperarCambio;
    private final CyclicBarrier esperarVerificacion;

    public Mundo(Celula[][] mundo, CyclicBarrier esperarVerificacion, CyclicBarrier esperarCambio) {
        this.mundo = mundo;
        this.esperarCambio = esperarCambio;
        this.esperarVerificacion = esperarVerificacion;
    }

    public void setMundo(Celula[][] mundo) {
        this.mundo = mundo;
    }

    public Celula[][] getMundo() {
        return mundo;
    }

    public void inicializar(int cantCelulasIniciales) {
        Random n = new Random(); //Utilizo un random para indicar si la celula dada sera inicializada con vida o no
        int cantVivas = 0;
        for (Celula[] mundo1 : mundo) {
            for (int j = 0; j < mundo[0].length; j++) {
                boolean viva = n.nextBoolean();
                if (viva && cantVivas < cantCelulasIniciales) { //Si todavia no hay suficientes celulas vivas como requeridas y el random es true, entonces inicializo viva
                    mundo1[j] = new Celula(true);
                    cantVivas++;
                } else {
                    mundo1[j] = new Celula(false);
                }
            }
        }
    }

    @Deprecated
    public void porDefecto() { //Deprecated ya que es una inicializacion hardcodeada solo valida para matrices de 6x6
        //Oscilador Sapo
        Celula[][] mundo1 = {{new Celula(false), new Celula(false), new Celula(false), new Celula(false), new Celula(false), new Celula(false)},
        {new Celula(false), new Celula(false), new Celula(false), new Celula(false), new Celula(false), new Celula(false)},
        {new Celula(false), new Celula(false), new Celula(true), new Celula(true), new Celula(true), new Celula(false)},
        {new Celula(false), new Celula(true), new Celula(true), new Celula(true), new Celula(false), new Celula(false)},
        {new Celula(false), new Celula(false), new Celula(false), new Celula(false), new Celula(false), new Celula(false)},
        {new Celula(false), new Celula(false), new Celula(false), new Celula(false), new Celula(false), new Celula(false)}};

        this.mundo = mundo1;

    }

    @Deprecated
    private void cambiarMundo() { //Deprecated ya que delego la responsabilidad de actualizacion a cada submundo

        for (Celula[] mundo1 : mundo) {
            for (int j = 0; j < mundo[0].length; j++) {
                Celula actual = mundo1[j];
                if (actual.getCambiar()) {
                    actual.setEstado(!actual.getEstado());
                    actual.setCambiar(false);
                }
            }
        }
    }

    @Deprecated
    private synchronized void imprimir() { //Deprecated ya que utilizo toString para imprimir por pantalla
        try {
            System.out.println("--------------------------------------------------------------------");
            for (int i = 0; i < 6; i++) {

                for (int j = 0; j < 6; j++) {

                    if (mundo[i][j].getEstado()) {
                        System.out.print((char) 2550 + " "); //Viva
                    } else {
                        System.out.print("*" + " "); //Muerta
                    }
                }

                System.out.println("\n");
            }

            System.out.println("--------------------------------------------------------------------");

        } catch (Exception e) {
        }
    }

    @Override
    public synchronized String toString() {
        String resultado = "--------------------------------------------------------------------" + "\n";
        try {

            for (int i = 0; i < 6; i++) {

                for (int j = 0; j < 6; j++) {

                    if (mundo[i][j].getEstado()) {
                        resultado = resultado + ((char) 2550 + " "); //Viva
                    } else {
                        resultado = resultado + "*" + " "; //Muerta
                    }
                }

                resultado = resultado + "\n";
            }

            resultado = resultado + "--------------------------------------------------------------------";

        } catch (Exception e) {
        }
        return resultado;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //System.out.println("Mundo esperando");
                esperarVerificacion.await(); //Espero que todos los Threads terminen con la verificacion del mundo. Esta barrera sin embargo no es absolutamente necesaria
            } catch (InterruptedException | BrokenBarrierException e) {
            } finally {
                if (esperarVerificacion.isBroken()) {
                    esperarVerificacion.reset();
                }

                try {
                    esperarCambio.await();//Espero que todos los Threads cambian su submundo
                    Thread.sleep(1500); //Duermo para dar tiempo al usuario de ver el estado del mundo
                    System.out.println(this.toString()); //Muestro por pantalla el estado del mundo

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
