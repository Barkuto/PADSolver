package UI;

import org.w3c.dom.css.Rect;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Created by Iggie on 7/1/2017.
 */
public class DrawUtils {

    // https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
    static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    // http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/Producesacopyofthesuppliedimage.htm
    static BufferedImage makeTranslucent(BufferedImage source, float alpha) {
        GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage newImage = configuration.createCompatibleImage(source.getWidth(), source.getHeight(), Transparency.TRANSLUCENT);

        Graphics2D graphics = newImage.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, alpha));
        graphics.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);
        graphics.dispose();
        return newImage;
    }

    // https://stackoverflow.com/questions/2027613/how-to-draw-a-directed-arrow-line-in-java
    static Shape createArrowShape(Point fromPt, Point toPt) {
        Polygon arrowPolygon = new Polygon();
        arrowPolygon.addPoint(-6, 1);// Left bottom corner
        arrowPolygon.addPoint(3, 1);// Left upper corner
        arrowPolygon.addPoint(3, 2);// Left upper arrow
        arrowPolygon.addPoint(6, 0);// Arrow point
        arrowPolygon.addPoint(3, -2);// Right upper arrow
        arrowPolygon.addPoint(3, -1);// Right upper corner
        arrowPolygon.addPoint(-6, -1);// Right bottom corner


        Point midPoint = new Point((int) ((fromPt.x + toPt.x) / 2.0), (int) ((fromPt.y + toPt.y) / 2.0));

        double rotate = Math.atan2(toPt.y - fromPt.y, toPt.x - fromPt.x);

        AffineTransform transform = new AffineTransform();
        transform.translate(midPoint.x, midPoint.y);
        double ptDistance = fromPt.distance(toPt);
        double scale = ptDistance / 12.0; // 12 because it's the length of the arrow polygon.
        transform.scale(scale, scale);
        transform.rotate(rotate);

        return transform.createTransformedShape(arrowPolygon);
    }
}
