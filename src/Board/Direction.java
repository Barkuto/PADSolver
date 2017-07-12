package Board;

/**
 * Created by Iggie on 6/20/2017.
 */
public enum Direction {
    UP, DOWN, LEFT, RIGHT, UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT;

    /**
     * Checks if this Direction is opposite to the given Direction.
     *
     * @param otherDirection Other Direction to compare with.
     * @return true if opposite, false otherwise.
     */
    public boolean isOppositeTo(Direction otherDirection) {
        return this == UP && otherDirection == DOWN ||
                this == DOWN && otherDirection == UP ||
                this == LEFT && otherDirection == RIGHT ||
                this == RIGHT && otherDirection == LEFT ||
                this == UPLEFT && otherDirection == DOWNRIGHT ||
                this == DOWNRIGHT && otherDirection == UPLEFT ||
                this == UPRIGHT && otherDirection == DOWNLEFT ||
                this == DOWNLEFT && otherDirection == UPRIGHT;
    }

    /**
     * @return Returns a random straight Direction( UP, DOWN, LEFT, RIGHT ).
     */
    public static Direction random4Dir() {
        return values()[(int) (Math.random() * 100) % (values().length - 4)];
    }

    /**
     * @return Returns a random Direction.
     */
    public static Direction random8Dir() {
        return values()[(int) (Math.random() * 100) % values().length];
    }
}
