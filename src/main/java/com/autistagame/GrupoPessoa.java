package com.autistagame;


import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class GrupoPessoa {
    double centerX;
    double centerY;
    double raioZona;
    double drainPerSecond;

    Circle grupoCircle; // círculo das pessoas
    Circle zonaCircle;  // aura que drena energia

    public GrupoPessoa(Pane root,
                       double centerX,
                       double centerY,
                       double raioZona,
                       double drainPerSecond) {

        this.centerX = centerX;
        this.centerY = centerY;
        this.raioZona = raioZona;
        this.drainPerSecond = drainPerSecond;

        // círculo do grupo (pessoas)
        grupoCircle = new Circle(15);
        grupoCircle.setFill(Color.LIGHTGRAY);
        grupoCircle.setCenterX(centerX);
        grupoCircle.setCenterY(centerY);

        // aura de barulho / estímulo social
        zonaCircle = new Circle(raioZona);
        zonaCircle.setFill(Color.color(1, 1, 0, 0.15)); // amarelo transparente
        zonaCircle.setStroke(Color.YELLOW);
        zonaCircle.setCenterX(centerX);
        zonaCircle.setCenterY(centerY);

        // adicionar na tela (a aura primeiro, depois o grupo)
        root.getChildren().addAll(zonaCircle, grupoCircle);
    }
}
