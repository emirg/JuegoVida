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
    private static GridPane gridPaneMatriz; //GridPane que contendra los elementos RectanguloVisual
    private static RectanguloVisual[][] matrizVisual; //Matriz de RectanguloVisual (seran Observers y son los encargados de cambiar cuando hay cambios en la matriz logica)
    private static Rectangle2D limitesPantallaPrincipal; //Utilizado para no exceder los limites
    private static Button botonEmpezar, botonPausar, botonStep; //Botones para empezar el juego, pausar y avanzar por generacion (1 sola actualizacion)
    private static String colorVida, colorMuerte; //Colores predeterminados 
    private static int duracionFrame; //Tiempo que se tarda en pasar de una generacion en otra
    private static boolean empezo = false; //Variable utilizada para que el boton empezar no vuelva a crear threads una vez pausado el juego

    //Variables utilizadas para la inicializacion del mundo / Variables logicas
    private CyclicBarrier esperarCambio; //Barrera que espera al cambio del mundo
    private CyclicBarrier esperarVerificacion; //Barrera que espera la verificacion de cada submundo 
    //////////////////////////////////
    private static Mundo mundo;
    private static final int FILAS = 10;
    private static final int COLUMNAS = 10;
   private static final int CANTIDADTHREADS = FILAS; 
   
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
        mundo.inicializarMundo(); //Inicializo el mundo (matriz) logico

        //System.out.println(imprimir(mundo));
        esperarCambio = new CyclicBarrier(CANTIDADTHREADS + 1); //Seteo la barrera con uno mas para incluir al thread principal en la espera
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

        crearMatrizVisual(); //Creo la matriz visual (matriz de rectanguloVisuales), y la linkeo con el mundo logico y el gridPane
        root.setCenter(gridPaneMatriz);
        //////////////

        ////Animacion////
        ///Creo la animacion que sera la encargada de controlar el progreso del mundo acorde a la duracion del frame
        animacion = new Timeline(new KeyFrame(Duration.millis(duracionFrame), (ActionEvent event) -> {
            try {

                esperarCambio.await();//Espero que todos los Threads cambian su submundo
    
            } catch (InterruptedException | BrokenBarrierException e) {}
            finally {
                if (esperarCambio.isBroken()) {
                    esperarCambio.reset();
                }
            }
        }));

        animacion.setCycleCount(Timeline.INDEFINITE);
        animacion.stop();
        ////////////////

        ////Botones////
        HBox barraSuperior = new HBox(); //Creo un HBox que contendra los botones principales en la parte superior de la ventana

        botonEmpezar = new Button();
        botonEmpezar.setText("Empezar"); //Boton encargado de empezar el juego y reanudarlo en caso de que este en pause
        botonEmpezar.setPrefSize(145, 40);
        botonEmpezar.setDisable(false);
        botonEmpezar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (mundo.getCantVivas() > 0) {
                    animacion.play();
                    //System.out.println("EMPEZAR");
                    ((Button) e.getSource()).setDisable(true);
                    botonPausar.setDisable(false);
                    botonStep.setDisable(true);

                    if (!empezo) { //Verifico que haya empezado para saber si es necesario inicializar threads
                    	iniciarThreads(mundo, esperarVerificacion, esperarCambio);
                        empezo = true;
                    }

                    // buttonRestore.setDisable(true);
                    //buttonRedefine.setDisable(true);
                    //comboBox.setDisable(true);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Matriz vacia!!!"); //En caso de que se quiera empezar el juego sin celulas vivas, se da una advertencia
                    alert.showAndWait();
                }
            }
        });

        botonPausar = new Button();
        botonPausar.setText("Pausar"); //Boton encargado de pausar la animacion y el progreso del juego. Inicialmente estara deshabilitado hasta que se empiece el juego
        botonPausar.setPrefSize(145, 40);
        botonPausar.setDisable(true);
        botonPausar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                animacion.stop();

                ((Button) e.getSource()).setDisable(true);
                botonEmpezar.setDisable(false);
                botonStep.setDisable(false);


            }
        });

        botonStep = new Button();
        botonStep.setText("Step"); //Boton encargado de avanzar el juego en un solo paso, es decir, mostrar como queda el mundo despues de una sola actualizacion
        botonStep.setPrefSize(145, 40);
        botonStep.setDisable(false);
        botonStep.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {


                if (!empezo) { //Inicialmente estara habilitado, por lo que presionar el boton "step" antes del "empezar" incializara los threads
                	iniciarThreads(mundo, esperarVerificacion, esperarCambio);
                    empezo = true;
                }

                esperarCambio.reset();


                botonEmpezar.setDisable(false);
                botonPausar.setDisable(true);


            }
        });

        barraSuperior.getChildren().addAll(botonEmpezar, botonPausar, botonStep); //Agrego los botones al HBox superior
        root.setTop(barraSuperior); //Agrego el HBox superior al root 

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
            	//Inicializo el elemento RectanguloVisual correspondiente a la posicion [i,j] con todas sus caracteristicas
                matrizVisual[i][j] = new RectanguloVisual();
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

                mundo.getMundo()[i][j].addObserver(matrizVisual[i][j]); //Agrego el objeto inicializado como un Observer de la celula logica en la posicion [i,j]
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
