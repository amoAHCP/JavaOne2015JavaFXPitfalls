package org.jacpfx;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jacpfx.concurrency.FXWorker;

/**
 * Created by Andy Moncsek on 15.09.15.
 */
public class ServiceChainFXWorkerDemo extends Application {

    public static void main(final String[] args) {
        Application.launch(args);
    }

    Label status;

    ProgressBar pb;
    HBox hb;




    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 200);
        stage.setScene(scene);
        stage.setTitle("Progress Controls - Service");

        final FXWorker<?> handler = FXWorker.instance();

        handler
                .supplyOnExecutorThread(() -> longRunningTask1(handler))
                .onError(throwable -> "")
                .consumeOnFXThread(stringVal -> {
                    Button b = new Button(stringVal);
                    b.setStyle("-fx-background-color: red");
                    hb.getChildren().add(b);
                })
                .onError(throwable -> null)
                .consumeOnExecutorThread((v) -> longRunningTask2())
                .onError(throwable -> null)
                .supplyOnExecutorThread(() -> longRunningTask3(handler));

        final HBox hb = createProgressBar(handler);

        final HBox meta = createButtonAndInfo(handler);


        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.getChildren().addAll(hb, meta);
        scene.setRoot(vb);
        stage.show();
    }

    private String longRunningTask3(FXWorker<?> handler) {
        handler.updateMessage("start");
        for (int i = 99; i < 200; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.updateMessage("progress: " + i);
            handler.updateProgress(i);
        }
        return "finished - Step 3";
    }

    private void longRunningTask2() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String longRunningTask1(FXWorker<?> handler) {
        handler.updateMessage("start");
        for (int i = 99; i < 200; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.updateMessage("progress: " + i);
            handler.updateProgress(i);
        }
        return "finished - Step 2";
    }

    private HBox createButtonAndInfo(FXWorker<?> handler) {
        final HBox meta = new HBox();
        final Button start = new Button("start");
        status = new Label("");

        HBox.setMargin(status, new Insets(10));
        status.textProperty().bind(handler.messageProperty());

        start.setOnMouseClicked((val) -> {
            handler.execute(value -> {
                status.textProperty().unbind();
                start.setText(value.toString());
                start.setStyle("-fx-background-color: blue");
            });
        });
        meta.getChildren().addAll(start, status);
        return meta;
    }


    private HBox createProgressBar(FXWorker<?> handler) {
        final Label label = new Label();
        label.setText("progress:");

        pb = new ProgressBar();
        pb.progressProperty().bind(handler.progressProperty());

        hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);

        hb.getChildren().addAll(label, pb);
        return hb;
    }


    private Service<String> createServiceOne() {
        return new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return createTaskInstance();
            }
        };
    }

    private Service<String> createServiceTwo() {
        return new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return createTaskInstance2();
            }
        };
    }

    private Task<String> createTaskInstance() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateMessage("start");
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(50);
                    updateMessage("progress: " + i);
                    updateProgress(i, 200);
                }
                return "finished - Step 1";
            }
        };
    }

    private Task<String> createTaskInstance2() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateMessage("start");
                for (int i = 99; i < 200; i++) {
                    Thread.sleep(50);
                    updateMessage("progress: " + i);
                    updateProgress(i, 200);
                }
                return "finished - Step 2";
            }
        };
    }
}
