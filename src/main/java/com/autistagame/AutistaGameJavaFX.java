package com.autistagame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class AutistaGameJavaFX extends Application {

    // Player
    private Circle player;

    // Movimento
    private boolean movingUp;
    private boolean movingDown;
    private boolean movingLeft;
    private boolean movingRight;
    private double speed = 4.0;

    // Energia
    private double energy = 100.0;
    private Label energyLabel;

    // Grupos de pessoas (drenam energia)
    private final List<GrupoPessoa> grupos = new ArrayList<>();

    // Salas silenciosas (recuperam energia)
    private final List<SalaSilenciosa> salas = new ArrayList<>();

    // Paredes do labirinto
    private final List<Rectangle> paredes = new ArrayList<>();

    // Mesa de trabalho (objetivo)
    private Rectangle mesaDestino;

    // Tamanho da tela
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    // Tamanho da tela
//    private static final int WIDTH = 800;
//    private static final int HEIGHT = 600;

    // Configuração da Sala Silenciosa (pode ajustar depois)
    private static final double SALA_X = 120;
    private static final double SALA_Y = 400;
    private static final double SALA_WIDTH = 100;
    private static final double SALA_HEIGHT = 80;


    // Game loop
    private Timeline gameLoop;
    private boolean terminou = false;

    @Override
    public void start(Stage stage) {
        // Label de energia
        energyLabel = new Label("Energia: 100.0");
        energyLabel.setTextFill(Color.WHITE);
        energyLabel.setLayoutX(10);
        energyLabel.setLayoutY(10);

        // Player
        player = new Circle(20);
        player.setFill(Color.CORNFLOWERBLUE);
        // posição inicial segura, dentro do corredor inicial
        player.setCenterX(150);
        player.setCenterY(120);

        Pane root = new Pane();
        root.setPrefSize(WIDTH, HEIGHT);
        root.setStyle("-fx-background-color: #202020;");

        // UI primeiro
        root.getChildren().add(energyLabel);

        // Labirinto (paredes e mesa)
        criarLabirinto(root);

        // Grupos (pontos de drenagem)
        criarGrupos(root);

        // Sala silenciosa (dentro do labirinto, com porta acessível)
        criarSalasSilenciosas(root);

        // Player por cima de tudo
        root.getChildren().add(player);

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Teclado
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.W || code == KeyCode.UP) {
                movingUp = true;
            } else if (code == KeyCode.S || code == KeyCode.DOWN) {
                movingDown = true;
            } else if (code == KeyCode.A || code == KeyCode.LEFT) {
                movingLeft = true;
            } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
                movingRight = true;
            }
        });

        scene.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.W || code == KeyCode.UP) {
                movingUp = false;
            } else if (code == KeyCode.S || code == KeyCode.DOWN) {
                movingDown = false;
            } else if (code == KeyCode.A || code == KeyCode.LEFT) {
                movingLeft = false;
            } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
                movingRight = false;
            }
        });

        // garantir foco p/ teclado
        scene.getRoot().requestFocus();
        scene.setOnMouseClicked(e -> scene.getRoot().requestFocus());

        stage.setTitle("Autista Game - Escritório Labirinto");
        stage.setScene(scene);
        stage.show();

        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (terminou) return;

            double deltaSeconds = 16 / 1000.0;

            updatePlayerPosition();
            atualizarEnergia(deltaSeconds);
            verificarObjetivo();
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    // Labirinto + mesa de destino
    private void criarLabirinto(Pane root) {
        double t = 20; // espessura da parede

        // Moldura do labirinto (quadro principal)
        paredes.add(criarParede(root, 100, 80, 600, t));        // topo
        paredes.add(criarParede(root, 100, 80, t, 420));        // esquerda
        paredes.add(criarParede(root, 100, 480, 600, t));       // baixo
        paredes.add(criarParede(root, 700, 80, t, 420));        // direita

        // Corredores em zigue-zague
        // faixa 1 (entre topo e y=160) – abertura à esquerda (onde o player começa)
        paredes.add(criarParede(root, 180, 160, 520, t));       // horizontal

        // faixa 2 – abertura à direita
        paredes.add(criarParede(root, 100, 240, 520, t));

        // faixa 3 – abertura à esquerda
        paredes.add(criarParede(root, 180, 320, 520, t));

        // corredor vertical central ligando as faixas
       // paredes.add(criarParede(root, 580, 160, t, 160));       // meio direita
      //  paredes.add(criarParede(root, 200, 240, t, 160));       // meio esquerda

        // Mesa de trabalho (objetivo) – topo direito
        mesaDestino = new Rectangle(620, 400, 80, 40);
        mesaDestino.setFill(Color.BURLYWOOD);
        mesaDestino.setStroke(Color.SADDLEBROWN);
        root.getChildren().add(mesaDestino);

        // Note que NÃO mexemos na posição do player aqui
    }

    private Rectangle criarParede(Pane root, double x, double y, double width, double height) {
        Rectangle r = new Rectangle(x, y, width, height);
        r.setFill(Color.color(0.5, 0.5, 0.5, 0.9));
        root.getChildren().add(r);
        return r;
    }

    private void criarGrupos(Pane root) {
        // grupos posicionados nas curvas do zigue-zague
        grupos.add(new GrupoPessoa(root, 260, 120, 60, 2.0));  // faixa alta
        grupos.add(new GrupoPessoa(root, 560, 200, 70, 2.5));  // perto do meio
        grupos.add(new GrupoPessoa(root, 260, 280, 70, 3.0));  // faixa do meio
        grupos.add(new GrupoPessoa(root, 560, 360, 60, 2.0));  // faixa baixa
    }

    private void criarSalasSilenciosas(Pane root) {
        // Sala silenciosa dentro do quadro do labirinto.
        // Porta fica na borda inferior da sala (implementado na classe SalaSilenciosa).
        salas.add(new SalaSilenciosa(
                root,
                SALA_X,
                SALA_Y,
                SALA_WIDTH,
                SALA_HEIGHT,
                5.0 // regenPerSecond
        ));
    }


    private void updatePlayerPosition() {
        double dx = 0;
        double dy = 0;

        if (movingUp)    dy -= speed;
        if (movingDown)  dy += speed;
        if (movingLeft)  dx -= speed;
        if (movingRight) dx += speed;

        double oldX = player.getCenterX();
        double oldY = player.getCenterY();

        double newX = oldX + dx;
        double newY = oldY + dy;

        double radius = player.getRadius();

        // Limite da tela
        if (newX - radius < 0) newX = radius;
        if (newX + radius > WIDTH) newX = WIDTH - radius;
        if (newY - radius < 0) newY = radius;
        if (newY + radius > HEIGHT) newY = HEIGHT - radius;

        // Colisão com paredes da sala silenciosa (paredes + porta)
        for (SalaSilenciosa sala : salas) {
            if (sala.bloqueiaMovimento(oldX, oldY, newX, newY)) {
                newX = oldX;
                newY = oldY;
                break;
            }
        }

        // Colisão com as paredes do labirinto
        for (Rectangle parede : paredes) {
            if (colideComParede(newX, newY, radius, parede)) {
                newX = oldX;
                newY = oldY;
                break;
            }
        }

        player.setCenterX(newX);
        player.setCenterY(newY);
    }

    // Checa se o círculo (player) está colidindo com uma parede (retângulo)
    private boolean colideComParede(double cx, double cy, double r, Rectangle rect) {
        double rx = rect.getX();
        double ry = rect.getY();
        double rw = rect.getWidth();
        double rh = rect.getHeight();

        double closestX = clamp(cx, rx, rx + rw);
        double closestY = clamp(cy, ry, ry + rh);

        double dx = cx - closestX;
        double dy = cy - closestY;

        return (dx * dx + dy * dy) < (r * r);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // Drena e recupera energia
    private void atualizarEnergia(double deltaSeconds) {
        boolean drenando = false;
        boolean recuperando = false;

        for (GrupoPessoa g : grupos) {
            double dist = distancia(
                    player.getCenterX(), player.getCenterY(),
                    g.centerX, g.centerY
            );

            if (dist < g.raioZona) {
                drenando = true;
                double drain = g.drainPerSecond * deltaSeconds;
                energy -= drain;
            }
        }

        for (SalaSilenciosa sala : salas) {
            if (sala.contem(player.getCenterX(), player.getCenterY())) {
                recuperando = true;
                double regen = sala.regenPerSecond * deltaSeconds;
                energy += regen;
            }
        }

        if (energy < 0) energy = 0;
        if (energy > 100) energy = 100;

        String energiaFormatada = String.format("%.1f", energy);

        String status = "";
        if (drenando && !recuperando) {
            status = " (drenando...)";
        } else if (!drenando && recuperando) {
            status = " (recuperando...)";
        } else if (drenando && recuperando) {
            status = " (ambiente misto...)";
        }

        energyLabel.setText("Energia: " + energiaFormatada + status);

        if (energy <= 0 && !terminou) {
            gameOver();
        }
    }

    private void verificarObjetivo() {
        if (mesaDestino == null || terminou) return;

        double px = player.getCenterX();
        double py = player.getCenterY();

        double mx = mesaDestino.getX();
        double my = mesaDestino.getY();
        double mw = mesaDestino.getWidth();
        double mh = mesaDestino.getHeight();

        if (px >= mx && px <= mx + mw &&
                py >= my && py <= my + mh &&
                energy > 0) {

            terminou = true;
            gameLoop.stop();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Você chegou à sua mesa! 💛");
            alert.setHeaderText("Você conseguiu chegar ao seu espaço de trabalho.");
            alert.setContentText("Apesar dos estímulos ao redor, você encontrou um caminho possível.");
            alert.show(); // <-- aqui! (em vez de showAndWait)
        }
    }

    private void gameOver() {
        terminou = true;
        gameLoop.stop();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fim do dia");
        alert.setHeaderText("Você ficou sobrecarregada 💔");
        alert.setContentText("Sua energia social foi drenada. Talvez um caminho mais tranquilo ajude amanhã.");
        alert.show(); // não bloqueia o loop de animação
    }

    private double distancia(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
