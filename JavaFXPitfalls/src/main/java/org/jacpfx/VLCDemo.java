package org.jacpfx;

import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Andy Moncsek on 19.08.15.
 */
public class VLCDemo
        extends Application {

    public static void main(final String[] args) {
        Application.launch(args);
    }

    private DirectMediaPlayerComponent mp;

    @Override
    public void start(Stage primaryStage) throws Exception {
        NativeLibrary.addSearchPath("vlc", "/Applications/VLC.app/Contents/MacOS/lib/");

        StackPane stack = new StackPane();
        final Canvas canvas = new Canvas(1920, 1080);
        final Label label = new Label("");
        final VBox vBox = new VBox();
        vBox.getChildren().add(label);
        label.setStyle("-fx-font: 36px \"Segoe UI Semibold\";-fx-text-fill: white;");
        StackPane.setMargin(label, new Insets(0, 10, 0, 0));
        stack.getChildren().addAll(canvas, vBox);
        PixelFormat format = canvas.getGraphicsContext2D().getPixelWriter().getPixelFormat();
        PixelFormat.Type type = format.getType();
        System.out.println(">>> " + canvas.getGraphicsContext2D().getPixelWriter().getPixelFormat());
        Scene scene = new Scene(stack);
        final PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        final WritablePixelFormat<ByteBuffer> byteBgraInstance = PixelFormat.getByteBgraPreInstance();//PixelFormat.getByteBgraInstance();

        mp = new DirectMediaPlayerComponent(formatCallback) {

            private AtomicLong totalTime = new AtomicLong(0);
            private long totalFrames;
            private long tooLateFrames;

            @Override
            public void display(DirectMediaPlayer mediaPlayer,
                                Memory[] nativeBuffers, final BufferFormat bufferFormat) {

                final Memory nativeBuffer = nativeBuffers[0];
                final ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
                totalFrames++;
                Platform.runLater(() -> {
                            long startTime = System.currentTimeMillis();
                            pixelWriter.setPixels(0, 0, 1920, 1080, byteBgraInstance, byteBuffer, 1920 * 4);
                            long renderTime = System.currentTimeMillis() - startTime;
                            totalTime.set(totalTime.longValue()+renderTime);
                            String s = String.format("Frames: %4d   Avg.time: %4.1f ms   Frames>20ms: %d   (Max)FPS: %3.0f fps\n", totalFrames, (double) totalTime.longValue() / totalFrames, tooLateFrames, 1000.0 / ((double) totalTime.longValue() / totalFrames));
                            label.setText(s);
                        }
                );
            }
        };

        mp.getMediaPlayer().playMedia("/Users/amo/Downloads/H264_AAC_(720p)(mkvmerge).mkv");

        primaryStage.setScene(scene);
        primaryStage.show();
    }


    /**
     * Callback to get the buffer format to use for video playback.
     */
    private final BufferFormatCallback formatCallback = new BufferFormatCallback() {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {

            int width = 1920;
            int height = 1080;

            return new RV32BufferFormat(width, height);
        }
    };
}
