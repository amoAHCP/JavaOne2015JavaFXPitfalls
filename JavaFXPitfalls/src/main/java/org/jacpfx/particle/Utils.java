package org.jacpfx.particle;

/**
 * Created by Andy Moncsek on 20.08.15.
 */
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

public class Utils {

    /**
     * Clamp value between min and max
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static double clamp(double value, double min, double max) {

        if (value < min)
            return min;

        if (value > max)
            return max;

        return value;
    }

    /**
     * Map value of a given range to a target range
     * @param value
     * @param currentRangeStart
     * @param currentRangeStop
     * @param targetRangeStart
     * @param targetRangeStop
     * @return
     */
    public static double map(double value, double currentRangeStart, double currentRangeStop, double targetRangeStart, double targetRangeStop) {
        return targetRangeStart + (targetRangeStop - targetRangeStart) * ((value - currentRangeStart) / (currentRangeStop - currentRangeStart));
    }

    /**
     * Snapshot an image out of a node, consider transparency.
     *
     * @param node
     * @return
     */
    public static Image createImage(Node node) {

        WritableImage wi;

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        int imageWidth = (int) node.getBoundsInLocal().getWidth();
        int imageHeight = (int) node.getBoundsInLocal().getHeight();

        wi = new WritableImage(imageWidth, imageHeight);
        node.snapshot(parameters, wi);

        return wi;

    }

    /**
     * Create an alpha masked ball with gradient colors from White to Black/Transparent. Used e. g. for particles.
     * May only be visible when you have a background other than white, ie use black
     *
     * @param radius
     * @return
     */
    public static Node createAlphaMaskedBall( double radius) {

        Circle ball = new Circle(radius);

        RadialGradient gradient1 = new RadialGradient(0,
                .1,
                0,
                0,
                radius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE.deriveColor(1,1,1,1)),
                new Stop(1, Color.BLACK.deriveColor(1,1,1,0)));

        ball.setFill(gradient1);

        return ball;
    }
}