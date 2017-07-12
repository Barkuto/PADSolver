package UI;

import Board.Orb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Iggie on 6/29/2017.
 */
class Resources {
    static BufferedImage bg1, bg2, fire, water, wood, light, dark, heart, poison, mortalpoison, jammer, bomb, blank;
    static int tileWidth, tileHeight;

    static void load() throws IOException {
        bg1 = ImageIO.read(Resources.class.getClassLoader().getResource("bg1.png"));
        bg2 = ImageIO.read(Resources.class.getClassLoader().getResource("bg2.png"));
        fire = ImageIO.read(Resources.class.getClassLoader().getResource("fire.png"));
        water = ImageIO.read(Resources.class.getClassLoader().getResource("water.png"));
        wood = ImageIO.read(Resources.class.getClassLoader().getResource("wood.png"));
        light = ImageIO.read(Resources.class.getClassLoader().getResource("light.png"));
        dark = ImageIO.read(Resources.class.getClassLoader().getResource("dark.png"));
        heart = ImageIO.read(Resources.class.getClassLoader().getResource("heart.png"));
        poison = ImageIO.read(Resources.class.getClassLoader().getResource("poison.png"));
        mortalpoison = ImageIO.read(Resources.class.getClassLoader().getResource("mortalpoison.png"));
        jammer = ImageIO.read(Resources.class.getClassLoader().getResource("jammer.png"));
        bomb = ImageIO.read(Resources.class.getClassLoader().getResource("bomb.png"));
        blank = ImageIO.read(Resources.class.getClassLoader().getResource("blank.png"));

        tileWidth = bg1.getWidth();
        tileHeight = bg1.getHeight();
    }

    static BufferedImage getOrbImage(Orb orb) {
        switch (orb) {
            case FIRE:
                return fire;
            case WATER:
                return water;
            case WOOD:
                return wood;
            case LIGHT:
                return light;
            case DARK:
                return dark;
            case HEAL:
                return heart;
            case POISON:
                return poison;
            case MORTALPOISON:
                return mortalpoison;
            case BOMB:
                return bomb;
            case JAMMER:
                return jammer;
            default:
                return blank;
        }
    }
}
