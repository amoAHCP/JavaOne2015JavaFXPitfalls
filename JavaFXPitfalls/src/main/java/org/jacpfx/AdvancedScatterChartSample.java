package org.jacpfx;

/**
 * Created by Andy Moncsek on 21.09.15.
 */
import com.sun.javafx.perf.PerformanceTracker;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * An advanced scatter chart with a variety of controls.
 *
 * @see javafx.scene.chart.ScatterChart
 * @see javafx.scene.chart.Chart
 * @see javafx.scene.chart.Axis
 * @see javafx.scene.chart.NumberAxis
 */
public class AdvancedScatterChartSample extends Application {
    private Label fpsLabel;
    private PerformanceTracker tracker;

    private void init(Stage primaryStage) {
        VBox root  = new VBox();
        fpsLabel = new Label("FPS:");
        fpsLabel.setStyle("-fx-font-size: 5em;-fx-text-fill: red;");
        fpsLabel.setOnMouseClicked((event) -> {
            tracker.resetAverageFPS();
        });


        FlowPane flow = new FlowPane();
        flow.setCache(true);
        flow.setCacheHint(CacheHint.SPEED);
        root.getChildren().addAll(fpsLabel,flow);
        Scene scene = new Scene(root, 500, 2000);
        createPerformanceTracker(scene);
        primaryStage.setScene(scene);
        List< ScatterChart<Number, Number>> result = new ArrayList<>();
        for(int i =0; i<10;i++) {
            ScatterChart<Number, Number> tmp = createChart();
            result.add(tmp);
        }
        flow.getChildren().setAll(result);
    }

    protected ScatterChart<Number, Number> createChart() {
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setSide(Side.TOP);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setSide(Side.RIGHT);
        final ScatterChart<Number,Number> sc = new ScatterChart<Number,Number>(xAxis,yAxis);
        // setup chart
        xAxis.setLabel("X Axis");
        yAxis.setLabel("Y Axis");
        // add starting data
        for (int s=0;s<5;s++) {
            XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
            series.setName("Data Series "+s);
            for (int i=0; i<30; i++) series.getData().add(new XYChart.Data<Number, Number>(Math.random()*98, Math.random()*98));
            sc.getData().add(series);
        }
        return sc;
    }

    public void createPerformanceTracker(Scene scene)
    {
        tracker = PerformanceTracker.getSceneTracker(scene);
        AnimationTimer frameRateMeter = new AnimationTimer()
        {

            @Override
            public void handle(long now)
            {

                float fps = getFPS();
                fpsLabel.setText(String.format("Current frame rate: %.0f fps", fps));

            }
        };

        frameRateMeter.start();
    }

    private float getFPS()
    {
        float fps = tracker.getAverageFPS();
        //tracker.resetAverageFPS();
        return fps;
    }

    private List<Path> getSubfolders(Path root) {
        final List<Path> roots = new ArrayList<>();
        try (DirectoryStream<Path> folders = Files.newDirectoryStream(root)) {
            // level one check, can have entries like floppy, so check level two
            for (final Path pathElement : folders) {
                roots.add(pathElement);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return roots;
    }


    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }
    public static void main(String[] args) { launch(args); }
}