package org.jacpfx;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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

import java.util.concurrent.ExecutionException;

/**
 * Created by Andy Moncsek on 15.09.15.
 */
public class TaskDemo extends Application {

    public static void main(final String[] args) {
        Application.launch(args);
    }



    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Progress Controls - Task");

        final Task<String> task = createTaskInstance();

        final HBox hb = createProgressBar(task);

        final HBox meta = createButtonAndInfo(task);


        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.getChildren().addAll(hb,meta);
        scene.setRoot(vb);
        stage.show();
    }

    private HBox createButtonAndInfo(Task<String> task) {
        final HBox meta = new HBox();
        final Button start = new Button("start");
        final Label status = new Label("");

        start.setOnMouseClicked((val) -> new Thread(task).start());

        HBox.setMargin(status, new Insets(10));
        status.textProperty().bind(task.messageProperty());
        meta.getChildren().addAll(start, status);

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (val) -> {
            try {
                status.textProperty().unbind();
                status.setText(task.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        return meta;
    }

    private HBox createProgressBar(Task<String> task) {
        final Label label = new Label();
        label.setText("progress:" );

        final ProgressBar pb = new ProgressBar();
        pb.progressProperty().bind(task.progressProperty());

        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);

        hb.getChildren().addAll(label, pb);
        return hb;
    }

    private Task<String> createTaskInstance() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateMessage("start");
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(50);
                    updateMessage("progress: " + i);
                    updateProgress(i, 100);
                }
                return "finished";
            }
        };
    }
}
