/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JuegoVida;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author Emi
 */
public class Controlador implements Initializable { //CONTROLLER

    @FXML
    private Button empezar;
    @FXML
    private Button pausa;
    @FXML
    private ComboBox tamaños;
    
    @FXML
    private GridPane matrizVisual;

//    @FXML
//    @FXML
//    @FXML
//    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO 
        matrizVisual.addColumn(2, new Label("hola"));
        
        
        
        tamaños.getItems().addAll("60x30", "50x50", "40x20");

        empezar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                JuegoVida.MundoNuevo.comenzarJuego();
            }
        });

        pausa.setDisable(true);
        pausa.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                JuegoVida.MundoNuevo.pausarJuego();
            }
        });
        
        
    }

}
