package com.example.snakegame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.example.snakegame.SnakeEngine.Cell;
import com.example.snakegame.SnakeEngine.Direction;
import com.example.snakegame.SnakeEngine.State;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** A játékszabályok tesztjei – emulátor nélkül futnak. */
public class SnakeEngineTest {

    private SnakeEngine engine;

    @Before
    public void setUp() {
        // Rögzített mag: a kaja helye determinisztikus.
        engine = new SnakeEngine(10, 10, new Random(42));
        engine.start();
    }

    @Test
    public void newGameStartsWithThreeSegmentsAndZeroScore() {
        assertEquals(3, engine.body().size());
        assertEquals(0, engine.score());
        assertEquals(Direction.RIGHT, engine.direction());
    }

    @Test
    public void stepMovesHeadWithoutChangingLength() {
        Cell head = engine.head();
        int length = engine.body().size();

        engine.step();

        assertEquals(head.x + 1, engine.head().x);
        assertEquals(head.y, engine.head().y);
        assertEquals(length, engine.body().size());
    }

    @Test
    public void hittingRightWallEndsTheGame() {
        // A jobb falig lépkedünk: a pálya szélessége 10, tehát biztosan kifutunk.
        for (int i = 0; i < 20 && !engine.isGameOver(); i++) {
            engine.step();
        }
        assertEquals(State.GAME_OVER, engine.state());
    }

    /** Regresszió: korábban a jobb oldali ütközés a magassággal számolt. */
    @Test
    public void wallCollisionUsesWidthNotHeight() {
        SnakeEngine wide = new SnakeEngine(20, 6, new Random(1));
        wide.start();
        int steps = 0;
        while (!wide.isGameOver() && steps < 100) {
            wide.step();
            steps++;
        }
        assertTrue("A kukacnak a 20 széles pályán túl kell jutnia a 6. oszlopon", steps > 6);
    }

    @Test
    public void hittingTopWallEndsTheGame() {
        engine.requestDirection(Direction.UP);
        for (int i = 0; i < 20 && !engine.isGameOver(); i++) {
            engine.step();
        }
        assertEquals(State.GAME_OVER, engine.state());
    }

    @Test
    public void gameOverStopsFurtherMovement() {
        while (!engine.isGameOver()) {
            engine.step();
        }
        Cell head = engine.head();

        engine.step();

        assertEquals(head, engine.head());
    }

    @Test
    public void cannotTurnBackOnItself() {
        assertFalse(engine.requestDirection(Direction.LEFT));
        engine.step();
        assertEquals(Direction.RIGHT, engine.direction());
    }

    @Test
    public void turningIsAppliedOnNextStep() {
        assertTrue(engine.requestDirection(Direction.DOWN));
        Cell head = engine.head();

        engine.step();

        assertEquals(head.x, engine.head().x);
        assertEquals(head.y + 1, engine.head().y);
    }

    /** Két gyors gombnyomás egy lépésen belül sem fordíthatja magába a kukacot. */
    @Test
    public void twoTurnsWithinOneStepCannotCauseSelfCollision() {
        engine.requestDirection(Direction.UP);
        engine.requestDirection(Direction.LEFT); // az UP-hoz képest nem ellentétes, de a RIGHT-hoz igen
        engine.step();

        assertNotEquals(State.GAME_OVER, engine.state());
    }

    @Test
    public void eatingGrowsTheSnakeAndIncreasesScore() {
        int length = engine.body().size();
        eatOnce(engine);

        assertEquals(1, engine.score());
        assertEquals(length + 1, engine.body().size());
    }

    @Test
    public void foodNeverSpawnsOnTheSnake() {
        for (int i = 0; i < 5; i++) {
            List<Cell> body = engine.body();
            assertFalse("A kaja a kukacon jelent meg: " + engine.food(), body.contains(engine.food()));
            engine.step();
            if (engine.isGameOver()) {
                break;
            }
        }
    }

    @Test
    public void runningIntoItselfEndsTheGame() {
        // Vízszintes, 5 hosszú kukac jobbra tartva, majd egy szűk hurok balra.
        engine.setBodyForTest(line(5, 5, 5), Direction.RIGHT);
        engine.setFoodForTest(new Cell(0, 0));

        engine.requestDirection(Direction.UP);
        engine.step();     // fej: (5,4)
        engine.requestDirection(Direction.LEFT);
        engine.step();     // fej: (4,4)
        engine.requestDirection(Direction.DOWN);
        engine.step();     // fej: (4,5) – a saját testébe fut

        assertEquals(State.GAME_OVER, engine.state());
    }

    /** A farok abban a pillanatban lép odébb, amikor a fej odaérne – ez nem ütközés. */
    @Test
    public void movingOntoTheVacatingTailIsAllowed() {
        engine.setBodyForTest(line(5, 5, 4), Direction.RIGHT);
        engine.setFoodForTest(new Cell(0, 0));

        engine.requestDirection(Direction.UP);
        engine.step();     // fej: (5,4), farok: (3,5)
        engine.requestDirection(Direction.LEFT);
        engine.step();     // fej: (4,4), farok: (4,5)
        engine.requestDirection(Direction.DOWN);
        engine.step();     // fej: (4,5) – a farok épp elhagyta

        assertEquals(State.RUNNING, engine.state());
    }

    @Test
    public void pauseFreezesTheSnake() {
        engine.pause();
        Cell head = engine.head();

        engine.step();
        assertEquals(head, engine.head());

        engine.start();
        engine.step();
        assertNotEquals(head, engine.head());
    }

    @Test
    public void speedIncreasesWithScoreButNeverBelowMinimum() {
        long start = engine.delayMillis();
        assertEquals(SnakeEngine.START_DELAY_MILLIS, start);

        eatOnce(engine);
        assertTrue(engine.delayMillis() < start);

        // Regresszió: korábban a késleltetés 30-ról csökkent egyesével, tehát 30 falat
        // után nullára, majd negatívba fordult és a játék kezelhetetlenül felgyorsult.
        SnakeEngine fast = new SnakeEngine(60, 60, new Random(3));
        fast.setBodyForTest(line(5, 30, 3), Direction.RIGHT);
        for (int i = 0; i < 40; i++) {
            eatOnce(fast);
        }
        assertEquals(40, fast.score());
        assertFalse(fast.isGameOver());
        assertEquals(SnakeEngine.MIN_DELAY_MILLIS, fast.delayMillis());
    }

    @Test
    public void resetRestoresTheInitialState() {
        eatOnce(engine);
        engine.reset();

        assertEquals(0, engine.score());
        assertEquals(3, engine.body().size());
        assertEquals(State.READY, engine.state());
        assertEquals(SnakeEngine.START_DELAY_MILLIS, engine.delayMillis());
    }

    @Test
    public void readyGameDoesNotMoveBeforeStart() {
        SnakeEngine fresh = new SnakeEngine(10, 10, new Random(11));
        Cell head = fresh.head();

        fresh.step();

        assertEquals(head, fresh.head());
    }

    /** Vízszintes kukac fejjel a megadott cellában, jobbra tartva. */
    private static List<Cell> line(int headX, int headY, int length) {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            cells.add(new Cell(headX - i, headY));
        }
        return cells;
    }

    /** A kaját a fej elé teszi, majd lép egyet – így az evés determinisztikus. */
    private void eatOnce(SnakeEngine target) {
        Cell head = target.head();
        Direction direction = target.direction();
        target.setFoodForTest(new Cell(head.x + direction.dx, head.y + direction.dy));
        target.step();
    }
}
