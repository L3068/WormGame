package com.example.wormgame;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * A játék szabályai, Android-függőség nélkül – így egységtesztelhető.
 *
 * A pálya egy {@code columns} x {@code rows} méretű rács, a kukac cellákból áll,
 * a lista első eleme a fej. A megjelenítés (Wormgame) csak lekérdezi az állapotot.
 */
public final class WormEngine {

    /** Egy cella a rácson. */
    public static final class Cell {
        public final int x;
        public final int y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cell)) return false;
            Cell other = (Cell) o;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    /** Mozgásirány. */
    public enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

        final int dx;
        final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        boolean isOpposite(Direction other) {
            return dx + other.dx == 0 && dy + other.dy == 0;
        }
    }

    /** A játék életciklusa. */
    public enum State { READY, RUNNING, PAUSED, GAME_OVER }

    /** Kezdeti lépésköz ezredmásodpercben. */
    public static final long START_DELAY_MILLIS = 260L;
    /** Ennél gyorsabb nem lesz a játék – e nélkül a sebesség 0-ra csökkenne. */
    public static final long MIN_DELAY_MILLIS = 70L;
    /** Ennyivel gyorsul minden megevett falat után. */
    public static final long SPEED_UP_MILLIS = 8L;

    private static final int START_LENGTH = 3;

    private final int columns;
    private final int rows;
    private final Random random;

    private final Deque<Cell> body = new ArrayDeque<>();
    private Direction direction;
    private Direction pendingDirection;
    private Cell food;
    private State state;
    private int score;

    public WormEngine(int columns, int rows) {
        this(columns, rows, new Random());
    }

    /** Teszteléshez: rögzített magú {@link Random}-mal determinisztikus a kajapakolás. */
    public WormEngine(int columns, int rows, Random random) {
        if (columns < START_LENGTH + 1 || rows < 3) {
            throw new IllegalArgumentException("A pálya túl kicsi: " + columns + "x" + rows);
        }
        this.columns = columns;
        this.rows = rows;
        this.random = random;
        reset();
    }

    /** Új játék: kukac középre, pontszám nullázva, kaja kisorsolva. */
    public void reset() {
        body.clear();
        int startY = rows / 2;
        int startX = columns / 2;
        for (int i = 0; i < START_LENGTH; i++) {
            body.addLast(new Cell(startX - i, startY));
        }
        direction = Direction.RIGHT;
        pendingDirection = Direction.RIGHT;
        score = 0;
        state = State.READY;
        placeFood();
    }

    /** Elindítja (vagy szünet után folytatja) a játékot. */
    public void start() {
        if (state == State.READY || state == State.PAUSED) {
            state = State.RUNNING;
        }
    }

    public void pause() {
        if (state == State.RUNNING) {
            state = State.PAUSED;
        }
    }

    /**
     * Új irány kérése. A visszafordulás (pl. jobbra menet közben balra) tilos,
     * mert azonnali önütközést okozna; ilyenkor a kérés eldobódik.
     *
     * <p>A kérés csak a következő {@link #step()}-nél lép életbe, így egy lépésen
     * belül két gyors gombnyomás sem tudja a kukacot magába fordítani.
     */
    public boolean requestDirection(Direction next) {
        if (next == null || next.isOpposite(direction) || next == direction) {
            return false;
        }
        pendingDirection = next;
        return true;
    }

    /**
     * Egy lépés előre.
     *
     * @return true, ha a kukac evett ebben a lépésben
     */
    public boolean step() {
        if (state != State.RUNNING) {
            return false;
        }
        direction = pendingDirection;

        Cell head = body.peekFirst();
        Cell next = new Cell(head.x + direction.dx, head.y + direction.dy);

        if (next.x < 0 || next.y < 0 || next.x >= columns || next.y >= rows) {
            state = State.GAME_OVER;
            return false;
        }
        // A farok abban a pillanatban odébb lép, amikor a fej odaér – ezért az
        // nem ütközés, kivéve ha éppen növünk (evés után marad a helyén).
        boolean eats = next.equals(food);
        if (hitsBody(next, !eats)) {
            state = State.GAME_OVER;
            return false;
        }

        body.addFirst(next);
        if (eats) {
            score++;
            placeFood();
        } else {
            body.removeLast();
        }
        return eats;
    }

    private boolean hitsBody(Cell candidate, boolean ignoreTail) {
        int index = 0;
        int last = body.size() - 1;
        for (Cell cell : body) {
            boolean isTail = index == last;
            if (!(ignoreTail && isTail) && cell.equals(candidate)) {
                return true;
            }
            index++;
        }
        return false;
    }

    /** Kaja kisorsolása egy szabad cellába. Ha nincs szabad cella, a játék megvan nyerve. */
    private void placeFood() {
        int free = columns * rows - body.size();
        if (free <= 0) {
            food = null;
            state = State.GAME_OVER;
            return;
        }
        int target = random.nextInt(free);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Cell cell = new Cell(x, y);
                if (body.contains(cell)) {
                    continue;
                }
                if (target == 0) {
                    food = cell;
                    return;
                }
                target--;
            }
        }
    }

    /** A következő lépésig hátralévő idő – minden falattal gyorsul, de van alsó korlát. */
    public long delayMillis() {
        return Math.max(MIN_DELAY_MILLIS, START_DELAY_MILLIS - (long) score * SPEED_UP_MILLIS);
    }

    /** A kukac cellái, fejtől farokig. */
    public List<Cell> body() {
        return new ArrayList<>(body);
    }

    public Cell head() {
        return body.peekFirst();
    }

    public Cell food() {
        return food;
    }

    public State state() {
        return state;
    }

    public int score() {
        return score;
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
    }

    public Direction direction() {
        return direction;
    }

    public boolean isGameOver() {
        return state == State.GAME_OVER;
    }

    /**
     * Csak tesztekhez: konkrét állapot beállítása, hogy a szabályok véletlen
     * kajaelhelyezés nélkül is ellenőrizhetők legyenek. A lista első eleme a fej.
     */
    void setBodyForTest(List<Cell> cells, Direction direction) {
        body.clear();
        body.addAll(cells);
        this.direction = direction;
        this.pendingDirection = direction;
        this.state = State.RUNNING;
    }

    /** Csak tesztekhez: a kaja adott cellába helyezése. */
    void setFoodForTest(Cell cell) {
        this.food = cell;
    }
}
