package Board;

/**
 * Created by Iggie on 6/20/2017.
 */
public enum Orb {
    /**
     * An Enum that represents each type of Orb.
     * Each Orb has it's own letter to represent itself.
     * The valid Orb letters are R,B,G,L,D,H,P,M,J,X and the blank char ' '.
     */
    FIRE('R'), WATER('B'), WOOD('G'), LIGHT('L'), DARK('D'), HEAL('H'), POISON('P'), MORTALPOISON('M'), JAMMER('J'), BOMB('X'), BLANK(' ');

    private char letter;

    Orb(char letter) {
        this.letter = letter;
    }

    public char letter() {
        return this.letter;
    }

    public static Orb orbFromLetter(char letter) {
        Orb[] values = Orb.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].letter == letter) {
                return values[i];
            }
        }
        return null;
    }

    public static Orb randomElementOrb() {
        return Orb.values()[(int) ((Math.random() * 100) % 6)];
    }

    public static Orb randomOrb() {
        return Orb.values()[(int) ((Math.random() * 100) % (Orb.values().length - 1))];
    }
}
