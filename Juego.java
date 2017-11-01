/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

//import static JuegoVida.Laboratorio.CANTIDADTHREADS;
//import static JuegoVida.Laboratorio.FILAS;
//import static JuegoVida.Laboratorio.iniciarThreads;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Emi
 */
public class Juego extends Application {

    //Variables utilizadas por IU
    private static Timeline animacion;
    private static GridPane gridPaneMatriz;
    private static RectanguloVisual[][] matrizVisual;
    private static Rectangle2D limitesPantallaPrincipal;
    private static Button botonEmpezar, botonPausar, botonStep;
    private static String colorVida, colorMuerte;
    private static int duracionFrame;
    private static boolean empezo = false;

    //Variables utilizadas para la inicializacion del mundo / Variables logicas
    private CyclicBarrier esperarCambio;
    private CyclicBarrier esperarVerificacion;
    //////////////////////////////////
    private static Mundo mundo;
    private static final int FILAS = 10;
    private static final int COLUMNAS = 10;
    private static final int THRESHOLD = 10;
    private static final int CANTIDADTHREADSAUX = (FILAS / 3 + FILAS % 3) * (COLUMNAS / 3 + COLUMNAS % 3); //Calculo cuantos threads necesitaria para utilizar threads con submundos de 3x3

    //Si la cantidad de threads calculada en CANTIDADTHREADSAUX supera el limite establecido, utilizo una division alternativa donde cada fila o columna es un thread
    private static final int CANTIDADTHREADS = CANTIDADTHREADSAUX < THRESHOLD ? CANTIDADTHREADSAUX : (FILAS < COLUMNAS ? FILAS : COLUMNAS);

    public static void main(String[] args) {
        Application.launch(args);
        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws Exception {
        duracionFrame = 500;
        colorVida = "#FF1493";
        colorMuerte = "#000000";
        ////////////////////////////////////LOGICA////////////////////////////////////
        mundo = new Mundo(FILAS, COLUMNAS);
        mundo.inicializarMundo();

        //System.out.println(imprimir(mundo));
        esperarCambio = new CyclicBarrier(CANTIDADTHREADS + 1);
        esperarVerificacion = new CyclicBarrier(CANTIDADTHREADS);

        /////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////VISUAL////////////////////////////////////
        BorderPane root = new BorderPane();
        //root.getStylesheets().add(Juego.class.getResource("Estilo.css").toExternalForm());

        limitesPantallaPrincipal = Screen.getPrimary().getVisualBounds();

        ////Matriz////
        gridPaneMatriz = new GridPane();
        gridPaneMatriz.setPrefSize(limitesPantallaPrincipal.getWidth() * 0.6, limitesPantallaPrincipal.getHeight() * 0.6);
        gridPaneMatriz.setPadding(new Insets(5, 20, 5, 20));
        //gridPaneMatriz.setGridLinesVisible(true);

        gridPaneMatriz.setAlignment(Pos.CENTER);

        crearMatrizVisual();
        root.setCenter(gridPaneMatriz);
        //////////////

        ////Animacion////
        animacion = new Timeline(new KeyFrame(Duration.millis(duracionFrame), (ActionEvent event) -> {
            try {

                esperarCambio.await();//Espero que todos los Threads cambian su submundo
                //System.out.println(imprimir(mundo)); //Muestro por pantalla el estado del mundo
                //actualizarMundoVisual();
                //Thread.sleep(1500); //Duermo para dar tiempo al usuario de ver el estado del mundo

            } catch (InterruptedException | BrokenBarrierException e) {
            } finally {
                if (esperarCambio.isBroken()) {
                    esperarCambio.reset();
                }
            }
        }));

        animacion.setCycleCount(Timeline.INDEFINITE);
        animacion.stop();
        ////////////////

        ////Botones////
        HBox barraSuperior = new HBox();

        botonEmpezar = new Button();
        botonEmpezar.setText("Empezar");
        botonEmpezar.setPrefSize(145, 40);
        botonEmpezar.setDisable(false);
        botonEmpezar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (mundo.getCantVivas() > 0) {
                    animacion.play();
                    System.out.println("EMPEZAR");
                    ((Button) e.getSource()).setDisable(true);
                    botonPausar.setDisable(false);
                    botonStep.setDisable(true);

                    if (!empezo) {
                        if (CANTIDADTHREADS == CANTIDADTHREADSAUX) {
                            iniciarThreads(mundo, esperarVerificacion, esperarCambio);
                        } else {
                            iniciarThreadsAlternativa(mundo, esperarVerificacion, esperarCambio);
                        }
                        empezo = true;
                    }

                    // buttonRestore.setDisable(true);
                    //buttonRedefine.setDisable(true);
                    //comboBox.setDisable(true);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Matriz vacia!!!");
                    alert.showAndWait();
                }
            }
        });

        botonPausar = new Button();
        botonPausar.setText("Pausar");
        botonPausar.setPrefSize(145, 40);
        botonPausar.setDisable(true);
        botonPausar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                animacion.stop();

                System.out.println("PAUSA");
                ((Button) e.getSource()).setDisable(true);
                botonEmpezar.setDisable(false);
                botonStep.setDisable(false);
                // buttonRestore.setDisable(true);
                //buttonRedefine.setDisable(true);
                //comboBox.setDisable(true);

            }
        });

        botonStep = new Button();
        botonStep.setText("Step");
        botonStep.setPrefSize(145, 40);
        botonStep.setDisable(false);
        botonStep.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
//                System.out.println(animacion.getCurrentTime().toMillis());
//                System.out.println(animacion.getStatus());

                if (!empezo) {
                    if (CANTIDADTHREADS == CANTIDADTHREADSAUX) {
                        iniciarThreads(mundo, esperarVerificacion, esperarCambio);
                    } else {
                        iniciarThreadsAlternativa(mundo, esperarVerificacion, esperarCambio);
                    }
                    empezo = true;
                }

                esperarCambio.reset();

//                
//                animacion.jumpTo(animacion.getCurrentTime().add(Duration.millis(duracionFrame)));
                botonEmpezar.setDisable(false);
                botonPausar.setDisable(true);
                // buttonRestore.setDisable(true);
                //buttonRedefine.setDisable(true);
                //comboBox.)setDisable(true);

            }
        });

        barraSuperior.getChildren().addAll(botonEmpezar, botonPausar, botonStep);
        root.setTop(barraSuperior);

        ///////////////
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        /////////////////////////////////////////////////////////////////////////////
    }

    public static void crearMatrizVisual() {

        matrizVisual = new RectanguloVisual[FILAS][COLUMNAS];

        for (int i = 0; i < matrizVisual.length; i++) {
            for (int j = 0; j < matrizVisual[0].length; j++) {
                matrizVisual[i][j] = new RectanguloVisual(i, j);
                matrizVisual[i][j].setAccessibleHelp(i + "," + j);
                matrizVisual[i][j].setFill(Paint.valueOf(colorMuerte));
                matrizVisual[i][j].setWidth(limitesPantallaPrincipal.getWidth() * 0.03);
                matrizVisual[i][j].setHeight(limitesPantallaPrincipal.getWidth() * 0.03);
                matrizVisual[i][j].setStroke(Paint.valueOf("#ffffff"));
                matrizVisual[i][j].setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        String[] coords = ((RectanguloVisual) event.getSource()).getAccessibleHelp().split(",");
                        int i = Integer.valueOf(coords[0]);
                        System.out.println(i);
                        int j = Integer.valueOf(coords[1]);
                        System.out.println(j);
                        if (mundo.getMundo()[i][j].isViva()) {
                            mundo.getMundo()[i][j].setViva(false);
                            mundo.cambiarCantVivas(false);
                        } else {
                            mundo.getMundo()[i][j].setViva(true);
                            mundo.cambiarCantVivas(true);
                        }
                    }
                });

                mundo.getMundo()[i][j].addObserver(matrizVisual[i][j]);
                gridPaneMatriz.add(matrizVisual[i][j], j, i);

            }
        }

    }

    public static String getColorVida() {
        return colorVida;
    }

    public static String getColorMuerte() {
        return colorMuerte;
    }

    public static synchronized String imprimir(Mundo mundo) {
        Celula[][] matriz = mundo.getMundo();
        String resultado = "--------------------------------------------------------------------" + "\n";
        try {

            for (int i = 0; i < matriz.length; i++) {

                for (int j = 0; j < matriz[0].length; j++) {

                    if (matriz[i][j].isViva()) {
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

    public static void iniciarThreads(Mundo mundo, CyclicBarrier esperarVerificacion, CyclicBarrier esperarCambio) {
        /*Metodo encargado de inciar los threads dividiendo la matriz en submatrices cuadradas de 3x3.
          Cuando no alcanzan las celdas para crear una matriz de 3x3, se utilizaran todas las restantes
          sin importar el tamaño de la misma (Nunca sera una matriz de orden mayor a 3x3)*/
        Celula[][] m = mundo.getMundo();
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

    public static void iniciarThreadsAlternativa(Mundo mundo, CyclicBarrier esperarVerificacion, CyclicBarrier esperarCambio) {
        Celula[][] m = mundo.getMundo();
        if (FILAS < COLUMNAS) {
            for (int i = 0; i < m.length; i++) {
                new Thread(new Submundo(m, i, i, 0, COLUMNAS - 1, esperarVerificacion, esperarCambio)).start();

            }
        } else {
            for (int i = 0; i < m[0].length; i++) {
                new Thread(new Submundo(m, 0, FILAS - 1, i, i, esperarVerificacion, esperarCambio)).start();

            }
        }

    }

    public static void comenzarJuego() {
        animacion.play();
    }

    public static void pausarJuego() {
        animacion.pause();
    }

}
