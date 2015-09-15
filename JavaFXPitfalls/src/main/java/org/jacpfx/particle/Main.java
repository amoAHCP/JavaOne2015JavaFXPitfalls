package org.jacpfx.particle;

/**
 * Created by Andy Moncsek on 20.08.15.
 */


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Main extends Application {

    private static Random random = new Random();

    Canvas canvas;
    GraphicsContext graphicsContext;

    /**
     * Container for canvas and other nodes like attractors and repellers
     */
    Pane layerPane;

    List<Attractor> allAttractors = new ArrayList<>();
    List<Repeller> allRepellers = new ArrayList<>();
    List<Particle> allParticles = new ArrayList<>();

    AnimationTimer animationLoop;

    Scene scene;

    MouseGestures mouseGestures = new MouseGestures();

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();

        canvas = new Canvas(Settings.SCENE_WIDTH, Settings.SCENE_HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();

        layerPane = new Pane();
        layerPane.getChildren().addAll(canvas);

        root.setCenter(layerPane);

        scene = new Scene(root, Settings.SCENE_WIDTH, Settings.SCENE_HEIGHT, Settings.SCENE_COLOR);

        primaryStage.setScene(scene);
        primaryStage.show();

        // add content
        prepareObjects();

        // add mouse location listener
        addListeners();

        // run animation loop
        startAnimation();

    }

    private void prepareObjects() {

        // add attractors
        for (int i = 0; i < Settings.ATTRACTOR_COUNT; i++) {
            addAttractors();
        }

        // add repellers
        for (int i = 0; i < Settings.REPELLER_COUNT; i++) {
            addRepellers();
        }

    }

    private void startAnimation() {

        // start game
        animationLoop = new AnimationTimer() {

            double prev = 0;
            double frameCount = 0;

            @Override
            public void handle(long now) {

                if( now - prev > 1_000_000_000) {

                    System.out.println( "FPS: " + frameCount);

                    prev = now;
                    frameCount = 0;

                } else {

                    frameCount++;

                }

                // add new particles
                for (int i = 0; i < Settings.PARTICLES_PER_ITERATION; i++) {
                    addParticle();
                }

                // apply force: gravity
                allParticles.forEach(sprite -> {
                    sprite.applyForce(Settings.FORCE_GRAVITY);
                });

                // apply force: wind depending on attractor position
                for (Attractor attractor : allAttractors) {
                    double dx = Utils.map(attractor.getLocation().x, 0, Settings.SCENE_WIDTH, -0.2, 0.2);
                    Vector2D windForce = new Vector2D(dx, 0);
                    allParticles.stream().parallel().forEach(sprite -> {
                        sprite.applyForce(windForce);
                    });
                }

                // apply force: repeller
                for (Repeller repeller : allRepellers) {
                    allParticles.stream().parallel().forEach(sprite -> {
                        Vector2D force = repeller.repel(sprite);
                        sprite.applyForce(force);
                    });
                }

                // move sprite: apply acceleration, calculate velocity and location
                allParticles.stream().parallel().forEach(Sprite::move);

                // update in fx scene
                allParticles.forEach(Sprite::display);
                allAttractors.forEach(Sprite::display);
                allRepellers.forEach(Sprite::display);

                // draw all particles on canvas
                // -----------------------------------------
                graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // life span of particle
                allParticles.stream().parallel().forEach(Sprite::decreaseLifeSpan);

                // remove all particles that aren't visible anymore
                removeDeadParticles();

                // show number of particles
                graphicsContext.setFill(Color.WHITE);
                graphicsContext.fillText("Particles: " + allParticles.size(), 1, 10);

            }
        };

        animationLoop.start();

    }

    private void removeDeadParticles() {

        Iterator<Particle> iter = allParticles.iterator();
        while (iter.hasNext()) {

            Particle particle = iter.next();
            if (particle.isDead()) {

                //
                layerPane.getChildren().remove(particle);

                // remove from particle list
                iter.remove();
            }

        }

    }

    private void addParticle() {

        // random location
        double x = Settings.SCENE_WIDTH / 2 + random.nextDouble() * Settings.EMITTER_WIDTH - Settings.EMITTER_WIDTH / 2;
        double y = Settings.EMITTER_LOCATION_Y;

        // dimensions
        double width = Settings.PARTICLE_WIDTH;
        double height = Settings.PARTICLE_HEIGHT;

        // create motion data
        Vector2D location = new Vector2D(x, y);

        double vx = random.nextGaussian() * 0.3;
        double vy = random.nextGaussian() * 0.3 - 1.0;
        Vector2D velocity = new Vector2D(vx, vy);

        Vector2D acceleration = new Vector2D(0, 0);

        // create sprite and add to layer
        Particle sprite = new Particle(location, velocity, acceleration, width, height);

        // register sprite
        allParticles.add(sprite);

        layerPane.getChildren().add(sprite);
    }

    private void addAttractors() {

        // center attractor
        double x = Settings.SCENE_WIDTH / 2;
        double y = Settings.SCENE_HEIGHT - Settings.SCENE_HEIGHT / 4;

        // dimensions
        double width = 100;
        double height = 100;

        // create motion data
        Vector2D location = new Vector2D(x, y);
        Vector2D velocity = new Vector2D(0, 0);
        Vector2D acceleration = new Vector2D(0, 0);

        // create sprite and add to layer
        Attractor attractor = new Attractor(location, velocity, acceleration, width, height);

        // register sprite
        allAttractors.add(attractor);

        layerPane.getChildren().add(attractor);

    }

    private void addRepellers() {

        // center attractor
        double x = Settings.SCENE_WIDTH / 2;
        double y = Settings.SCENE_HEIGHT - Settings.SCENE_HEIGHT / 4 + 110;

        // dimensions
        double width = 100;
        double height = 100;

        // create motion data
        Vector2D location = new Vector2D(x, y);
        Vector2D velocity = new Vector2D(0, 0);
        Vector2D acceleration = new Vector2D(0, 0);

        // create sprite and add to layer
        Repeller repeller = new Repeller(location, velocity, acceleration, width, height);

        // register sprite
        allRepellers.add(repeller);

        layerPane.getChildren().add(repeller);
    }

    private void addListeners() {

        // move attractors via mouse
        for (Attractor attractor : allAttractors) {
            mouseGestures.makeDraggable(attractor);
        }

        // move attractors via mouse
        for (Repeller sprite : allRepellers) {
            mouseGestures.makeDraggable(sprite);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}