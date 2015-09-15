package org.jacpfx.particle;

/**
 * Created by Andy Moncsek on 20.08.15.
 */
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * A single particle with a per-frame reduced lifespan and now view. The particle is drawn on a canvas, it isn't actually a node
 */
public class Particle extends Sprite {

    private static Image image = null;

    ColorAdjust colorAdjust;

    public Particle( Vector2D location, Vector2D velocity, Vector2D acceleration, double width, double height) {
        super( location, velocity, acceleration, width, height);

        // effect for this particle
        colorAdjust = new ColorAdjust();
        setEffect(colorAdjust);

    }

    @Override
    public Node createView() {

        if( image == null) {
            image = Utils.createImage( Utils.createAlphaMaskedBall( width / 2));
        }

        return new ImageView( image );
    }

    public void decreaseLifeSpan() {
        lifeSpan--;
    }

    public void display() {

        super.display();

        // opacity
        double opacity = lifeSpan/lifeSpanMax;
        setOpacity( opacity);

        // color
//	    Color color = Color.BLUE;//interpolate(Color.RED, opacity);
        Color color;
        double threshold = 0.9;
        if( opacity >= threshold) {
            color = Color.YELLOW.interpolate(Color.WHITE, Utils.map( opacity, threshold, 1, 0, 1));
        } else {
            color = Color.RED.interpolate(Color.YELLOW, Utils.map( opacity, 0, threshold, 0, 1));
        }

//		color = Color.RED.interpolate(Color.YELLOW, opacity);

        // update colorAdjust
        // see http://stackoverflow.com/questions/31587092/how-to-use-coloradjust-to-set-a-target-color
        double hue = Utils.map( (color.getHue() + 180) % 360, 0, 360, -1, 1);
        colorAdjust.setHue(hue);

        // use saturation as it is
        double saturation = color.getSaturation();
        colorAdjust.setSaturation(saturation);

        // we use WHITE in the masked ball creation => inverse brightness
        double brightness = Utils.map( color.getBrightness(), 0, 1, -1, 0);
        colorAdjust.setBrightness(brightness);

    }

}