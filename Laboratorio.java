/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

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
public class Laboratorio {

    //Variables utilizadas para la inicializacion del mundo
    static final int FILAS = 7;
    static final int COLUMNAS = 6;
    
    //La cantidad de Threads sera igual a la cantdad de matriz cuadradas de 3x3 mas las matrices cuyas cantidades de columnas y filas son las restantes para completar la matriz
    static final int CANTIDADTHREADS = (FILAS / 3 + FILAS % 3) * (COLUMNAS / 3 + COLUMNAS % 3);
   

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // int cantidadThreads = (4);
            CyclicBarrier esperarCambio = new CyclicBarrier(CANTIDADTHREADS + 1);
            CyclicBarrier esperarVerificacion = new CyclicBarrier(CANTIDADTHREADS + 1);

            //Declaro e instancio un objeto del tipo Mundo, el cual contiene la matriz con celulas, las barreras necesarias para sincronizar, y es la encargada de imprimir el estado del mundo por pantalla
            Mundo m = new Mundo(new Celula[FILAS][COLUMNAS], esperarVerificacion, esperarCambio);

            m.inicializar(10); //Inicializo el mundo con 10 celulas distribuidas aleatoriamente

            System.out.println(m.toString());

            new Thread(m).start();

            
            iniciarThreads(m, esperarVerificacion, esperarCambio);


        } catch (Exception ex) {
            Logger.getLogger(Laboratorio.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    public static void iniciarThreads(Mundo m, CyclicBarrier esperarVerificacion, CyclicBarrier esperarCambio) {
        /*Metodo encargado de inciar los threads dividiendo la matriz en submatrices cuadradas de 3x3.
          Cuando no alcanzan las celdas para crear una matriz de 3x3, se utilizaran todas las restantes
          sin importar el tamaño de la misma (Nunca sera una matriz de orden mayor a 3x3)*/
        for (int i = 0; i < m.getMundo().length; i = i + 3) { //Recorro los indices de filas y columnas para establecer cuales seran las divisiones de la matriz
            for (int j = 0; j < m.getMundo()[0].length; j = j + 3) {
                if (FILAS % 3 == 0) { //Verifico si la cantidad de filas es multiplo de 3
                    if (COLUMNAS % 3 == 0) { //Verifico si la cantidad de columnas es multiplo de 3
                        //Como se que la matriz es de orden cuadrado de nxm, donde n y m son multiplos de 3, divido la matriz sin precupaciones de que queden submatrices que no sean de 3x3
                        new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
                    } else { 
                        /*Si la cantidad de columnas no es multipl de 3, entonces debo verificar si la submatriz podra ser de orden 3x3
                        En caso contrario se creara una submatriz con la cantidad de columnas restantes para abarcar toda la matriz */
                        if (j + 3 > m.getMundo().length - 1) {//Si las columnas necesarias para completar la submatriz no existen, se toma hasta la ultima columna disponible
                            new Thread(new Submundo(m, i, i + 2, j, m.getMundo()[0].length - 1, esperarVerificacion, esperarCambio)).start();
                        } else { //Si puede ser de orden 3x3 entonces divido normalmente
                            new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
                        }
                    }
                } else { //En el caso que la cantidad de filas no sea multiplo de 3 entonces tendre que verificar que la submatriz pueda ser de 3x3 y sino abarcar la submatriz con las filas restantes de la matriz principal
                    if (COLUMNAS % 3 == 0) { //Si es multiplo de 3 en cuanto a columnas, no hace falta verificarlas, y procedo a verificar solo filas
                        if (i + 3 > m.getMundo().length - 1) { //Si las filas necesarias para completar la submatriz no existen, se toma hasta la ultima fila disponible
                            new Thread(new Submundo(m, i, m.getMundo().length - 1, j, j + 2, esperarVerificacion, esperarCambio)).start();
                        } else {
                            new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
                        }
                    } else { 
                        /*Si tanto las filas como columnas no son de una cantidad multiplo de 3 entonces debo verificar tanto en filas y columnas
                        que la submatriz pueda ser de 3x3, en caso contrario se tomaran las celdas restantes de la matriz */
                        if (i + 3 > m.getMundo().length - 1) { //Si las filas necesarias para completar la submatriz no existen, se toma hasta la ultima fila disponible
                            if (j + 3 > m.getMundo().length - 1) {//Si las columnas necesarias para completar la submatriz no existen, se toma hasta la ultima columna disponible
                                new Thread(new Submundo(m, i, m.getMundo().length - 1, j, m.getMundo()[0].length - 1, esperarVerificacion, esperarCambio)).start();
                            } else {
                                new Thread(new Submundo(m, i, m.getMundo().length - 1, j, j + 2, esperarVerificacion, esperarCambio)).start();
                            }
                        } else {
                            if (j + 3 > m.getMundo().length - 1) {//Si las columnas necesarias para completar la submatriz no existen, se toma hasta la ultima columna disponible
                                new Thread(new Submundo(m, i, i + 2, j, m.getMundo()[0].length - 1, esperarVerificacion, esperarCambio)).start();
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

//ALTERNATIVA PARA LA DIVISION DE LA MATRIZ -  public static void iniciarThreads(Mundo m, CyclicBarrier esperarVerificacion, CyclicBarrier esperarCambio)

            //Realizo la subdivision de la matriz en submatrices de 3x3
//            if (FILAS % 3 == 0) { //Verifico en primera instancia si es divisible por 3 en cuanto a filas
//                if (COLUMNAS % 3 == 0) { //Si tiene cantidad de filas divisible por 3...
//                    for (int i = 0; i < m.getMundo().length; i = i + 3) { //Procedo a crear los threads normalmente de 3 en 3
//                        for (int j = 0; j < m.getMundo()[0].length; j = j + 3) {
//                            new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
//                        }
//                    }
//                } else {
//                    /*Si no tiene cantidad de columnas divisibles por 3 debo verificar en cada iteracion que si la siguiente submatriz no abarca
//                      3 columnas, entonces tomara las columnas restantes de la matriz */
//                    for (int i = 0; i < m.getMundo().length; i = i + 3) {
//                        for (int j = 0; j < m.getMundo()[0].length; j = j + 3) {
//                            if (j + 3 > m.getMundo().length - 1) {
//                                new Thread(new Submundo(m, i, i + 2, j, m.getMundo()[0].length - 1, esperarVerificacion, esperarCambio)).start();
//                            } else {
//                                new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
//                            }
//                        }
//                    }
//                }
//            } else { //La matriz no tiene cantidad de filas divisible por 3 
//                if (COLUMNAS % 3 == 0) { //Si tiene cantidad divisible por 3 procedo como en el caso anterior
//                    for (int i = 0; i < m.getMundo().length; i = i + 3) {
//                        for (int j = 0; j < m.getMundo()[0].length; j = j + 3) {
//                            if (i + 3 > m.getMundo().length - 1) {
//                                new Thread(new Submundo(m, i, m.getMundo().length - 1, j, j + 2, esperarVerificacion, esperarCambio)).start();
//                            } else {
//                                new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
//                            }
//                        }
//                    }
//                } else {
//                    /*Si la cantidad de columnas no es divisible por 3 entonces
//                    debo verificar si la siguiente submatriz no abarca 3 columnas 
//                    y 3 filas, entonces tomara las columnas restantes de la matriz */
//                    for (int i = 0; i < m.getMundo().length; i = i + 3) {
//                        for (int j = 0; j < m.getMundo()[0].length; j = j + 3) {
//                            if (i + 3 > m.getMundo().length - 1) {
//                                if (j + 3 > m.getMundo().length - 1) {
//                                    new Thread(new Submundo(m, i, m.getMundo().length - 1, j, m.getMundo()[0].length - 1, esperarVerificacion, esperarCambio)).start();
//                                } else {
//                                    new Thread(new Submundo(m, i, m.getMundo().length - 1, j, j + 2, esperarVerificacion, esperarCambio)).start();
//                                }
//                            } else {
//                                if (j + 3 > m.getMundo().length - 1) {
//                                    new Thread(new Submundo(m, i, i + 2, j, m.getMundo()[0].length - 1, esperarVerificacion, esperarCambio)).start();
//                                } else {
//                                    new Thread(new Submundo(m, i, i + 2, j, j + 2, esperarVerificacion, esperarCambio)).start();
//                                }
//                            }
//                        }
//                    }
//                }
//            }
