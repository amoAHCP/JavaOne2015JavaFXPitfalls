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
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * An advanced scatter chart with a variety of controls.
 *
 * @see ScatterChart
 * @see javafx.scene.chart.Chart
 * @see javafx.scene.chart.Axis
 * @see NumberAxis
 */
public class AdvancedSimpleScatterChartSample extends Application {
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
        List<ImageView> result = new ArrayList<>();
        for(int i =0; i<10;i++) {
            ScatterChart<Number, Number> tmp = createChart();
            flow.getChildren().add(tmp);
            ImageView view = new ImageView(tmp.snapshot(null,null));
            view.setPreserveRatio(true);
            view.setSmooth(true);
            view.setPickOnBounds(true);
            result.add(view);
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

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }
    public static void main(String[] args) { launch(args); }
}