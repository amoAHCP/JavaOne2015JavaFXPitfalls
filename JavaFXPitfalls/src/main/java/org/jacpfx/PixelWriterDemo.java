package org.jacpfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by Andy Moncsek on 16.09.15.
 */
public class PixelWriterDemo extends Application {

    public static final String FILE_JPG = "file:/Users/amo/Pictures/April_Mai/DSCF5453.jpg";

    public static void main(final String[] args) {
        Application.launch(args);
    }

    Label result = new Label("");
    VBox color = new VBox();
    VBox argb = new VBox();
    VBox setPixels = new VBox();
    VBox all = new VBox();
    VBox maskImageView = new VBox();

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 1280, 1024);
        stage.setScene(scene);
        stage.setTitle("Progress Controls - Task");
        final VBox vb = new VBox();

        VBox header = new VBox();
        header.getChildren().addAll(createButtons(), createOriginalImageView(), result);


        vb.setSpacing(5);
        vb.getChildren().addAll(header, createSamplePane());
        scene.setRoot(vb);
        stage.show();
    }

    private HBox createSamplePane() {
        HBox box = new HBox();
        box.setAlignment(Pos.BOTTOM_CENTER);
        box.setPrefHeight(150);
        box.setMaxHeight(150);
        color.setAlignment(Pos.BOTTOM_CENTER);
        argb.setAlignment(Pos.BOTTOM_CENTER);
        setPixels.setAlignment(Pos.BOTTOM_CENTER);
        all.setAlignment(Pos.BOTTOM_CENTER);
        maskImageView.setAlignment(Pos.BOTTOM_CENTER);
        HBox.setMargin(color, new Insets(10));
        HBox.setMargin(argb, new Insets(10));
        HBox.setMargin(all, new Insets(10));
        HBox.setMargin(setPixels, new Insets(10));
        HBox.setMargin(maskImageView, new Insets(10));
        box.getChildren().addAll(color, argb, all, setPixels, maskImageView);
        return box;
    }

    private StackPane createOriginalImageView() {
        StackPane originalPane = new StackPane();
        HBox originalStage = new HBox();
        HBox.setHgrow(originalPane, Priority.ALWAYS);
        originalStage.getChildren().add(originalPane);
        ImageView originalView = new ImageView(new Image(FILE_JPG, 0, 480, true, false, true));
        originalPane.getChildren().add(originalView);
        return originalPane;
    }

    private HBox createButtons() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        Button color = new Button("setColor");
        color.setOnAction((event) -> createImageBySetColor());
        Button argb = new Button("setARGB");
        argb.setOnAction((event) -> createImageByargb());
        Button setPixels = new Button("setPixels");
        setPixels.setOnAction((event) -> createImageSetPixelsInt((view, time) -> Platform.runLater(() -> {
            VBox.setMargin(view, new Insets(20));
            this.setPixels.getChildren().clear();
            this.setPixels.getChildren().addAll(view, new Label("by setPixels: " + time + " ms"));
        })));
        Button all = new Button("mask and getImage");
        all.setOnAction((event) -> createImageByMask());
        Button imgView = new Button("mask use ImageView");
        imgView.setOnAction((event) -> createImageViewByMask());
        HBox.setMargin(color, new Insets(30));
        HBox.setMargin(argb, new Insets(30));
        HBox.setMargin(setPixels, new Insets(30));
        HBox.setMargin(all, new Insets(30));
        HBox.setMargin(imgView, new Insets(30));
        box.getChildren().addAll(color, argb, all, setPixels, imgView);
        return box;
    }

    private void createImageBySetColor() {
        invokeLoop((writer, reader, x, y) -> {
            Color color = reader.getColor(x, y);
            writer.setColor(x, y, color);
        }, (view, elapsedTime) -> Platform.runLater(() -> {
            color.getChildren().clear();
            VBox.setMargin(view, new Insets(20));
            color.getChildren().addAll(view, new Label("by color: " + elapsedTime + " ms"));
        }));
    }

    private void createImageByargb() {
        invokeLoop((writer, reader, x, y) -> {
            int argb = reader.getArgb(x, y);
            writer.setArgb(x, y, argb);
        }, (view, elapsedTime) -> Platform.runLater(() -> {
            argb.getChildren().clear();
            VBox.setMargin(view, new Insets(20));
            argb.getChildren().addAll(view, new Label("by argb: " + elapsedTime + " ms"));
        }));

    }

    private void invokeLoop(final ReaderWrtiter readerWrtiter, NodeTime nodeTime) {
        runOffThread(() -> {
            final Image src = getFile();
            final PixelReader reader = src.getPixelReader();
            final int width = (int) src.getWidth();
            final int height = (int) src.getHeight();

            WritableImage dest = null;
            final long startTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                dest = invokeLoopBody(readerWrtiter, reader, width, height);
            }

            final long stopTime = System.currentTimeMillis();
            final long elapsedTime = stopTime - startTime;
            final ImageView originalView = new ImageView(dest);
            nodeTime.invoke(originalView, elapsedTime);

        });


    }

    private void runOffThread(Runnable r) {
        Thread t = new Thread(r);
        t.start();
    }

    private Image getFile() {
        return new Image(FILE_JPG, 0, 300, true, true, false);
    }

    private WritableImage invokeLoopBody(ReaderWrtiter readerWrtiter, PixelReader reader, int width, int height) {
        WritableImage dest = null;

        if (width > height) {
            int startX = width / 4;
            dest = new WritableImage(height, height + startX);
            final PixelWriter writer = dest.getPixelWriter();
            invokeLandscape(readerWrtiter, reader, width, height, writer);
        } else {
            int startY = (height / 4);
            dest = new WritableImage(width, width + startY);
            final PixelWriter writer = dest.getPixelWriter();
            invokePortrait(readerWrtiter, reader, width, height, writer);
        }
        return dest;
    }

    private void invokePortrait(ReaderWrtiter readerWrtiter, PixelReader reader, int width, int height, PixelWriter writer) {
        int startY = (height / 4);
        for (int x = 0; x < width; x++) {
            for (int y = startY; y < height - startY / 2 + 10; y++) {
                readerWrtiter.invoke(writer, reader, x, y);

            }
        }
    }

    private void invokeLandscape(ReaderWrtiter readerWrtiter, PixelReader reader, int width, int height, PixelWriter writer) {
        int startX = width / 4;
        for (int x = startX; x < width - startX / 2 + 10; x++) {
            for (int y = 0; y < height; y++) {
                readerWrtiter.invoke(writer, reader, x, y);

            }
        }
    }

    private interface ReaderWrtiter {
        void invoke(PixelWriter writer, PixelReader reader, int x, int y);
    }

    private interface NodeTime {
        void invoke(ImageView view, long elapsedTime);
    }

    private void createImageSetPixels(NodeTime nodeTime) {
        runOffThread(() -> {
            final Image src = getFile();
            final PixelReader reader = src.getPixelReader();
            WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraPreInstance();
           //WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance(); // false test

            final int width = (int) src.getWidth();
            final int height = (int) src.getHeight();
            final long startTime = System.currentTimeMillis();
            WritableImage dest = null;
            for (int i = 0; i < 1000; i++) {
                if (width > height) {
                    // landscape
                    byte[] rowBuffer = new byte[height * height * 3];

                    reader.getPixels(width / 4, 0, height, height, format, rowBuffer, 0, width * 4);
                    dest = new WritableImage(width, width);
                    final PixelWriter writer = dest.getPixelWriter();
                    writer.setPixels(0, 0, height, height, format, rowBuffer, 0, width * 4);
                } else {
                    // portrait
                    byte[] rowBuffer = new byte[width * width * 4];

                    reader.getPixels(0, height / 4, width, width, format, rowBuffer, 0, width * 4);
                    dest = new WritableImage(width, width);
                    final PixelWriter writer = dest.getPixelWriter();
                    writer.setPixels(0, 0, width, width, format, rowBuffer, 0, width * 4);
                }

            }

            final long stopTime = System.currentTimeMillis();
            final long elapsedTime = stopTime - startTime;

            final ImageView originalView = new ImageView(dest);
            nodeTime.invoke(originalView, elapsedTime);


        });
    }

    private void createImageSetPixelsInt(NodeTime nodeTime) {
        runOffThread(() -> {
            final Image src = getFile();
            final PixelReader reader = src.getPixelReader();
            WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();
            //WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance(); // false test

            final int width = (int) src.getWidth();
            final int height = (int) src.getHeight();
            final long startTime = System.currentTimeMillis();
            WritableImage dest = null;
            for (int i = 0; i < 1000; i++) {
                if (width > height) {
                    // landscape
                    int[] rowBuffer = new int[height * height * 3];

                    reader.getPixels(width / 4, 0, height, height, format, rowBuffer, 0, width * 4);
                    dest = new WritableImage(width, width);
                    final PixelWriter writer = dest.getPixelWriter();
                    writer.setPixels(0, 0, height, height, format, rowBuffer, 0, width * 4);
                } else {
                    // portrait
                    int[] rowBuffer = new int[width * width * 4];

                    reader.getPixels(0, height / 4, width, width, format, rowBuffer, 0, width * 4);
                    dest = new WritableImage(width, width);
                    final PixelWriter writer = dest.getPixelWriter();
                    writer.setPixels(0, 0, width, width, format, rowBuffer, 0, width * 4);
                }

            }

            final long stopTime = System.currentTimeMillis();
            final long elapsedTime = stopTime - startTime;

            final ImageView originalView = new ImageView(dest);
            nodeTime.invoke(originalView, elapsedTime);


        });
    }

    private void createImageViewByMask() {
        runOffThread(() -> {
            final Image src = getFile();

            ImageView dest = null;
            final int width = (int) src.getWidth();
            final int height = (int) src.getHeight();
            final long startTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                dest = postProcess(src, height, width);
            }

            final long stopTime = System.currentTimeMillis();
            final long elapsedTime = stopTime - startTime;
            final ImageView originalView = dest;

            Platform.runLater(() -> {
                maskImageView.getChildren().clear();
                maskImageView.getChildren().addAll(originalView, new Label("by mask: " + elapsedTime + " ms"));
            });
        });

    }

    private void createImageByMask() {
        final Image src = getFile();

        Image dest = null;
        final int width = (int) src.getWidth();
        final int height = (int) src.getHeight();
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            dest = postProcess(src, height, width).snapshot(null, null);
        }

        final long stopTime = System.currentTimeMillis();
        final long elapsedTime = stopTime - startTime;
        final ImageView originalView = new ImageView(dest);
        all.getChildren().clear();
        VBox.setMargin(originalView, new Insets(20));
        all.getChildren().addAll(originalView, new Label("by mask (snapshot): " + elapsedTime + " ms"));

    }


    public ImageView postProcess(Image image, double maxHight, double maxWidth) {
        ImageView maskView = new ImageView();
        maskView.setPreserveRatio(true);
        maskView.setSmooth(false);
        maskView.setImage(image);
        maskView.setClip(initLayer(image, Color.WHITE, 1.0, maxHight, maxWidth));

        return maskView;//.snapshot(null, null);
    }

    private Rectangle initLayer(Image image, Color color, double opacity, double maxHight, double maxWidth) {
        Rectangle rectangle = null;
        if (image.getWidth() > image.getHeight()) {
            rectangle = new Rectangle(maxWidth / 4, 0, maxHight, maxHight);
        } else {
            rectangle = new Rectangle(0, maxHight / 4, maxWidth, maxWidth);
        }

        rectangle.setFill(color);
        rectangle.setOpacity(opacity);
        return rectangle;
    }
}
