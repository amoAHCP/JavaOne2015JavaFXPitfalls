package org.jacpfx;

import javafx.application.Application;
import javafx.concurrent.Service;
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

/**
 * Created by Andy Moncsek on 15.09.15.
 */
public class ServiceChainDemo extends Application {

    public static void main(final String[] args) {
        Application.launch(args);
    }


    ProgressBar pb;


    private void createServiceChain(Service<String> service, Label status) {
        service.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (val) -> {
            status.textProperty().unbind();
            status.setText("update UI and wait for Service 2 invoke");
            Service<String> service2 = createServiceTwo();

            service2.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (val2) -> {
                status.textProperty().unbind();
                status.setText(service2.getValue());

            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            status.textProperty().bind(service2.messageProperty());
            pb.progressProperty().unbind();
            pb.progressProperty().bind(service2.progressProperty());
            service2.start();
        });
    }

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 200);
        stage.setScene(scene);
        stage.setTitle("Progress Controls - Service");

        final Service<String> task = createServiceOne();

        final HBox hb = createProgressBar(task);

        final HBox meta = createButtonAndInfo(task);


        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.getChildren().addAll(hb, meta);
        scene.setRoot(vb);
        stage.show();
    }

    private HBox createButtonAndInfo(Service<String> service) {
        final HBox meta = new HBox();
        final Button start = new Button("start");
        final Label status = new Label("");

        HBox.setMargin(status, new Insets(10));
        status.textProperty().bind(service.messageProperty());

        start.setOnMouseClicked((val) -> {
            if (!service.isRunning()) {
                service.reset();
                service.start();
                status.textProperty().bind(service.messageProperty());
            }
        });
        createServiceChain(service, status);
        meta.getChildren().addAll(start, status);
        return meta;
    }


    private HBox createProgressBar(Service<String> service) {
        final Label label = new Label();
        label.setText("progress:" );

        pb = new ProgressBar();
        pb.progressProperty().bind(service.progressProperty());

        final HBox hb = new HBox();
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
