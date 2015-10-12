package org.jacpfx;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.Test;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;
import org.testfx.framework.junit.ApplicationTest;

/**
 * Created by Andy Moncsek on 16.09.15.
 */
@State(Scope.Thread)
public class PixelWriterTest extends ApplicationTest {

    Pane mainPane = new Pane();

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(mainPane, 800, 600);
        stage.setScene(scene);
        //stage.show();
    }

   @Test
    public void testAAAWarmup() {
        Image src = new Image("file:/Users/amo/Pictures/April_Mai/DSCF5453.jpg");
        PixelReader reader = src.getPixelReader();

        int width = (int) src.getWidth();
        int height = (int) src.getHeight();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 2; i++) {
            WritableImage dest = new WritableImage(width, height);
            PixelWriter writer = dest.getPixelWriter();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // reading a pixel from src image,
                    // then writing a pixel to dest image
                    Color color = reader.getColor(x, y);
                    writer.setColor(x, y, color);

                    // this way is also OK
                    int argb = reader.getArgb(x, y);
                    writer.setArgb(x, y, argb);
                }
            }

        }


    }

    @Test
    public void testSetColor() {
        Image src = new Image("file:/Users/amo/Pictures/April_Mai/DSCF5453.jpg");
        PixelReader reader = src.getPixelReader();

        int width = (int) src.getWidth();
        int height = (int) src.getHeight();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            WritableImage dest = new WritableImage(width, height);
            PixelWriter writer = dest.getPixelWriter();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // reading a pixel from src image,
                    // then writing a pixel to dest image
                    Color color = reader.getColor(x, y);
                    writer.setColor(x, y, color);

                    // this way is also OK
//            int argb = reader.getArgb(x, y);
//            writer.setArgb(x, y, argb);
                }
            }

        }


        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime + " ms");
    }

    @Test
    public void testsetARGB() throws RunnerException {
        Image src = new Image("file:/Users/amo/Pictures/April_Mai/DSCF5453.jpg");
        PixelReader reader = src.getPixelReader();

        int width = (int) src.getWidth();
        int height = (int) src.getHeight();

        long startTime = System.currentTimeMillis();
        testARGB(reader,width,height);


        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime + " ms-");
    }




    private void testARGB(PixelReader reader, int width, int height) {
        for (int i = 0; i < 10; i++) {
            WritableImage dest = new WritableImage(width, height);
            PixelWriter writer = dest.getPixelWriter();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // reading a pixel from src image,
                    // then writing a pixel to dest image
                    //Color color = reader.getColor(x, y);
                    //writer.setColor(x, y, color);

                    // this way is also OK
                    int argb = reader.getArgb(x, y);
                    writer.setArgb(x, y, argb);


                }
            }

        }
    }


}
