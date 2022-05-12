package app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import misc.Misc;
import misc.Vector2d;

import java.util.ArrayList;

/**
 * Класс треугольника
 */
public class Triangle {
    /**
     * Список точек
     */
    public final ArrayList<Vector2d> points;

    /**
     * Конструктор треугольника
     */
    public Triangle(Vector2d pos1, Vector2d pos2, Vector2d pos3) {
        this.points = new ArrayList<>();
        points.add(pos1);
        points.add(pos2);
        points.add(pos3);
    }

    /**
     * содержится ли точка в тругольнике
     */
    public boolean contains(Vector2d pos) {
        double a = (points.get(0).x - pos.x) * (points.get(1).y - points.get(0).y) - (points.get(1).x - points.get(0).x) * (points.get(0).y - pos.y);
        double b = (points.get(1).x - pos.x) * (points.get(2).y - points.get(1).y) - (points.get(2).x - points.get(1).x) * (points.get(1).y - pos.y);
        double c = (points.get(2).x - pos.x) * (points.get(0).y - points.get(2).y) - (points.get(0).x - points.get(2).x) * (points.get(2).y - pos.y);
        return (a >= 0 && b >= 0 && c >= 0) || (a <= 0 && b <= 0 && c <= 0);
    }

    /**
     * Строковое представление объекта
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Triangle{" +
                ", pos1=" + points.get(0) +
                ", pos2=" + points.get(1) +
                ", pos3=" + points.get(2) +
                '}';
    }

    /**
     * Множество точек - пересечение 2-ч треугольников
     * @param a первый треугольник
     * @param b второй треугольник
     * @return множество точек
     */
    public static ArrayList<Vector2d> crossingTriangles(Triangle a, Triangle b) {
        ArrayList<Vector2d> answer = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (a.contains(b.points.get(i))) {
                answer.add(b.points.get(i));
            }
            if (b.contains(a.points.get(i))) {
                answer.add(a.points.get(i));
            }
            for (int j = i + 1; j < 3; j++) {
                for (int n = 0; n < 3; n++) {
                    for (int m = n + 1; m < 3; m++) {
                        crossingLinesSegments(a.points.get(i), a.points.get(j), b.points.get(n), b.points.get(m), answer);
                    }
                }
            }
        }
        for (int i = 0; i < answer.size(); i++) {
            for (int j = i + 1; j < answer.size(); j++) {
                if (answer.get(i).x == answer.get(j).x && answer.get(i).y == answer.get(j).y) {
                    answer.remove(j);
                }
            }
        }
        if (answer.size() > 3) {
            for (int i = 0; i < answer.size(); i++) {
                for (int j = i + 2; j < answer.size(); j++) {
                    if((answer.get(i + 1).x - answer.get(i).x) * (answer.get(j).y - answer.get(i).y) - (answer.get(j).x - answer.get(i).x) * (answer.get(i + 1).y - answer.get(i).y) > 0) {
                        Vector2d temp = answer.get(i + 1);
                        answer.set(i + 1, answer.get(j));
                        answer.set(j, temp);
                    }
                }
            }
        }
        return answer;
    }

    /**
     * множество точек пересечения отрезков
     * @param p1 первая точка первой прямой
     * @param p2 вторая точка первой прямой
     * @param q1 первая точка второй прямой
     * @param q2 вторая точка второй прямой
     * @param points множество точек пересечения прямых
     */
    private static void crossingLinesSegments(Vector2d p1, Vector2d p2, Vector2d q1, Vector2d q2 , ArrayList<Vector2d> points) {
        //если прямая q параллельна оси y, то меням пременные p и q местами
        if (q1.x == q2.x) {
            Vector2d t1 = q1;
            Vector2d t2 = q2;
            q1 = p1;
            q2 = p2;
            p1 = t1;
            p2 = t2;
        }
        //если прямая p параллельна оси y
        if (p1.x == p2.x) {
            //если ещё и прямая q параллельна оси y
            if (q1.x == q2.x) {
                if (p1.x == q1.x) {
                    addPointsInSameLines(p1, p2, q1, q2, points);
                }
            } else {
                double kq = (q1.y - q2.y) / (q1.x - q2.x);
                double bq = q1.y - kq * q1.x;
                Vector2d m = new Vector2d();
                m.x = p1.x;
                m.y = kq * m.x + bq;
                if (isInSquare(m, q1, q2) && isInSquare(m, p1, p2)) {
                    points.add(m);
                }
            }
        } else {
            double kp = (p1.y - p2.y) / (p1.x - p2.x);
            double kq = (q1.y - q2.y) / (q1.x - q2.x);
            double bp = p1.y - kp * p1.x;
            double bq = q1.y - kq * q1.x;
            if (kp == kq) {
                if (bp == bq) {
                    addPointsInSameLines(p1, p2, q1, q2, points);
                }
            } else {
                Vector2d m = new Vector2d();
                m.x = (bp - bq) / (kq - kp);
                m.y = kp * m.x + bp;
                if (isInSquare(m, q1, q2) && isInSquare(m, p1, p2)) {
                    points.add(m);
                }
            }
        }
    }

    /**
     * Точки если прямые отрезков совпадают
     * @param p1 первая точка первой прямой
     * @param p2 вторая точка первой прямой
     * @param q1 первая точка второй прямой
     * @param q2 вторая точка второй прямой
     * @param points множество точек пересечения прямых
     */
    private static void addPointsInSameLines(Vector2d p1, Vector2d p2, Vector2d q1, Vector2d q2, ArrayList<Vector2d> points) {
        if (isInSquare(p1, q1, q2)) {
            points.add(p1);
        }
        if (isInSquare(p2, q1, q2)) {
            points.add(p2);
        }
        if (isInSquare(q1, p1, p2)) {
            points.add(q1);
        }
        if (isInSquare(q2, p1, p2)) {
            points.add(q2);
        }
    }

    private static boolean isInSquare(Vector2d p, Vector2d q1, Vector2d q2) {
        return ((p.x >= q1.x && p.x <= q2.x) || (p.x >= q2.x && p.x <= q1.x)) && ((p.y >= q1.y && p.y <= q2.y) || (p.y >= q2.y && p.y <= q1.y));
    }
}
