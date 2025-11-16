package com.autistagame;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// Representa uma sala silenciosa onde a energia é recuperada
public class SalaSilenciosa {
    double x;
    double y;
    double width;
    double height;
    double regenPerSecond;

    Rectangle salaRect;
    Rectangle portaRect;

    // porta
    double portaX;
    double portaY;
    double portaWidth;
    double portaHeight;

    public SalaSilenciosa(Pane root,
                          double x,
                          double y,
                          double width,
                          double height,
                          double regenPerSecond) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.regenPerSecond = regenPerSecond;

        // Sala (retângulo com borda)
        salaRect = new Rectangle(x, y, width, height);
        salaRect.setFill(Color.color(0.2, 0.4, 0.8, 0.2)); // azul transparente
        salaRect.setStroke(Color.LIGHTBLUE);

        // ========= PORTA NA LATERAL DIREITA, CENTRALIZADA NO EIXO Y =========

        portaWidth = 10;                // estreita na horizontal
        portaHeight = height / 3;       // "alta" na vertical (um terço da altura)

        // X: grudada na parede direita da sala
        portaX = x + width - portaWidth;

        // Y: centralizada verticalmente
        portaY = y + (height - portaHeight) / 2;

        portaRect = new Rectangle(portaX, portaY, portaWidth, portaHeight);
        portaRect.setFill(Color.color(0.2, 0.4, 0.8, 0.9)); // mais destacada

        root.getChildren().addAll(salaRect, portaRect);
    }

    public boolean contem(double px, double py) {
        return px >= x && px <= x + width &&
                py >= y && py <= y + height;
    }

    public boolean estaNaPorta(double px, double py) {
        return px >= portaX && px <= portaX + portaWidth &&
                py >= portaY && py <= portaY + portaHeight;
    }

    /**
     * Retorna true se esse movimento deveria ser BLOQUEADO pela parede.
     * Agora a passagem é liberada AO ATRAVESSAR A PAREDE DIREITA
     * NA REGIÃO DA PORTA (e bloqueada em qualquer outro lugar).
     */
    public boolean bloqueiaMovimento(double oldX, double oldY, double newX, double newY) {
        boolean oldInside = contem(oldX, oldY);
        boolean newInside = contem(newX, newY);

        // Se não mudou de "dentro" pra "fora" ou vice-versa, não cruzou a parede da sala
        if (oldInside == newInside) {
            return false;
        }

        // Parede direita da sala
        double xDireita = x + width;

        // Verifica se a linha do movimento cruza a parede direita
        boolean cruzaParedeDireita =
                (oldX <= xDireita && newX >= xDireita) ||
                        (oldX >= xDireita && newX <= xDireita);

        // Verifica se, ao cruzar, está na faixa vertical da porta
        boolean dentroFaixaPorta =
                (oldY >= portaY && oldY <= portaY + portaHeight) ||
                        (newY >= portaY && newY <= portaY + portaHeight);

        // Se cruzou a parede direita na faixa da porta → libera
        if (cruzaParedeDireita && dentroFaixaPorta) {
            return false; // pode passar (entrar ou sair pela lateral direita)
        }

        // Se cruzou a parede direita fora da porta → bloqueia
        if (cruzaParedeDireita) {
            return true;
        }

        // Cruzou alguma outra borda da sala (topo, esquerda ou baixo) → bloqueia
        return true;
    }
}
