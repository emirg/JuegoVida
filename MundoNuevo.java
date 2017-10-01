/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

//import static JuegoVida.Laboratorio.CANTIDADTHREADS;
//import static JuegoVida.Laboratorio.FILAS;
//import static JuegoVida.Laboratorio.iniciarThreads;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author Emi
 */
public class MundoNuevo {
    //Variables utilizadas para la inicializacion del mundo

    static final int FILAS = 6;
    static final int COLUMNAS = 6;

    //La cantidad de Threads sera igual a la cantdad de matriz cuadradas de 3x3 mas las matrices cuyas cantidades de columnas y filas son las restantes para completar la matriz
    static final int CANTIDADTHREADS = (FILAS / 3 + FILAS % 3) * (COLUMNAS / 3 + COLUMNAS % 3);

    public static void main(String[] args) {

        Celula[][] mundo = new Celula[FILAS][COLUMNAS];
        inicializarMundo(mundo, 10);
        System.out.println(imprimir(mundo));

        CyclicBarrier esperarCambio = new CyclicBarrier(CANTIDADTHREADS + 1);
        CyclicBarrier esperarVerificacion = new CyclicBarrier(CANTIDADTHREADS + 1);

        iniciarThreads(mundo, esperarVerificacion, esperarCambio);

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
                    System.out.println(imprimir(mundo)); //Muestro por pantalla el estado del mundo

                } catch (InterruptedException | BrokenBarrierException e) {
                } finally {
                    if (esperarCambio.isBroken()) {
                        esperarCambio.reset();
                    }
                }
            }

        }
    }

    public static void inicializarMundo(Celula[][] mundo, int cantCelulasIniciales) {
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

    public static synchronized String imprimir(Celula[][] mundo) {
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

    public static void iniciarThreads(Celula[][] m, CyclicBarrier esperarVerificacion, CyclicBarrier esperarCambio) {
        /*Metodo encargado de inciar los threads dividiendo la matriz en submatrices cuadradas de 3x3.
          Cuando no alcanzan las celdas para crear una matriz de 3x3, se utilizaran todas las restantes
          sin importar el tamaño de la misma (Nunca sera una matriz de orden mayor a 3x3)*/
        for (int i = 0; i < m.length; i = i + 3) { //Recorro los indices de filas y columnas para establecer cuales seran las divisiones de la matriz
            for (int j = 0; j < m[0].length; j = j + 3) {
                if (FILAS % 3 == 0) { //Verifico si la cantidad de filas es multiplo de 3
                    if (COLUMNAS % 3 == 0) { //Verifico si la cantidad de columnas es multiplo de 3
                        //Como se que la matriz es de orden cuadrado de nxm, donde n y m son multiplos de 3, divido la matriz sin precupaciones de que queden submatrices que no sean de 3x3
                        new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
                    } else {
                        /*Si la cantidad de columnas no es multipl de 3, entonces debo verificar si la submatriz podra ser de orden 3x3
                        En caso contrario se creara una submatriz con la cantidad de columnas restantes para abarcar toda la matriz */
                        if (j + 3 > m.length - 1) {//Si las columnas necesarias para completar la submatriz no existen, se toma hasta la ultima columna disponible
                            new Thread(new Submundo(m, i, i + 2, j, m[0].length - 1, esperarVerificacion, esperarCambio)).start();
                        } else { //Si puede ser de orden 3x3 entonces divido normalmente
                            new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
                        }
                    }
                } else { //En el caso que la cantidad de filas no sea multiplo de 3 entonces tendre que verificar que la submatriz pueda ser de 3x3 y sino abarcar la submatriz con las filas restantes de la matriz principal
                    if (COLUMNAS % 3 == 0) { //Si es multiplo de 3 en cuanto a columnas, no hace falta verificarlas, y procedo a verificar solo filas
                        if (i + 3 > m.length - 1) { //Si las filas necesarias para completar la submatriz no existen, se toma hasta la ultima fila disponible
                            new Thread(new Submundo(m, i, m.length - 1, j, j + 2, esperarVerificacion, esperarCambio)).start();
                        } else {
                            new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
                        }
                    } else {
                        /*Si tanto las filas como columnas no son de una cantidad multiplo de 3 entonces debo verificar tanto en filas y columnas
                        que la submatriz pueda ser de 3x3, en caso contrario se tomaran las celdas restantes de la matriz */
                        if (i + 3 > m.length - 1) { //Si las filas necesarias para completar la submatriz no existen, se toma hasta la ultima fila disponible
                            if (j + 3 > m.length - 1) {//Si las columnas necesarias para completar la submatriz no existen, se toma hasta la ultima columna disponible
                                new Thread(new Submundo(m, i, m.length - 1, j, m[0].length - 1, esperarVerificacion, esperarCambio)).start();
                            } else {
                                new Thread(new Submundo(m, i, m.length - 1, j, j + 2, esperarVerificacion, esperarCambio)).start();
                            }
                        } else {
                            if (j + 3 > m.length - 1) {//Si las columnas necesarias para completar la submatriz no existen, se toma hasta la ultima columna disponible
                                new Thread(new Submundo(m, i, i + 2, j, m[0].length - 1, esperarVerificacion, esperarCambio)).start();
                            } else {
                                new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
                            }
                        }

                    }
                }
            }
        }
    }
}
