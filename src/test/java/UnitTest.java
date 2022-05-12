import app.Point;
import app.Task;
import misc.CoordinateSystem2d;
import misc.Vector2d;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс тестирования
 */
public class UnitTest {

    /**
     * Тест
     *
     * @param points        список точек
     * @param crossedCoords мн-во пересечений
     * @param singleCoords  мн-во разности
     */
    private static void test(ArrayList<Point> points, Set<Vector2d> crossedCoords, Set<Vector2d> singleCoords) {

    }


    /**
     * Первый тест
     */
    @Test
    public void test1() {
        ArrayList<Point> points = new ArrayList<>();

        points.add(new Point(new Vector2d(1, 1)));
        points.add(new Point(new Vector2d(-1, 1)));
        points.add(new Point(new Vector2d(-1, 1)));
        points.add(new Point(new Vector2d(2, 1)));
        points.add(new Point(new Vector2d(1, 2)));
        points.add(new Point(new Vector2d(1, 2)));

        Set<Vector2d> crossedCoords = new HashSet<>();
        crossedCoords.add(new Vector2d(1, 2));
        crossedCoords.add(new Vector2d(-1, 1));

        Set<Vector2d> singleCoords = new HashSet<>();
        singleCoords.add(new Vector2d(1, 1));
        singleCoords.add(new Vector2d(2, 1));

        test(points, crossedCoords, singleCoords);
    }

    /**
     * Второй тест
     */
    @Test
    public void test2() {
        ArrayList<Point> points = new ArrayList<>();

        points.add(new Point(new Vector2d(1, 1)));
        points.add(new Point(new Vector2d(2, 1)));
        points.add(new Point(new Vector2d(2, 2)));
        points.add(new Point(new Vector2d(1, 2)));

        Set<Vector2d> crossedCoords = new HashSet<>();

        Set<Vector2d> singleCoords = new HashSet<>();
        singleCoords.add(new Vector2d(1, 1));
        singleCoords.add(new Vector2d(2, 1));
        singleCoords.add(new Vector2d(2, 2));
        singleCoords.add(new Vector2d(1, 2));

        test(points, crossedCoords, singleCoords);
    }

    /**
     * Третий тест
     */
    @Test
    public void test3() {
        ArrayList<Point> points = new ArrayList<>();

        points.add(new Point(new Vector2d(1, 1)));
        points.add(new Point(new Vector2d(2, 1)));
        points.add(new Point(new Vector2d(2, 2)));
        points.add(new Point(new Vector2d(1, 2)));

        Set<Vector2d> crossedCoords = new HashSet<>();

        Set<Vector2d> singleCoords = new HashSet<>();
        singleCoords.add(new Vector2d(1, 1));
        singleCoords.add(new Vector2d(2, 1));
        singleCoords.add(new Vector2d(2, 2));
        singleCoords.add(new Vector2d(1, 2));

        test(points, crossedCoords, singleCoords);
    }
}
