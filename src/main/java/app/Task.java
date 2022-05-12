package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.humbleui.jwm.MouseButton;
import io.github.humbleui.skija.*;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;
import misc.Vector2i;
import panels.PanelLog;

import java.util.ArrayList;

import static app.Colors.*;

/**
 * Класс задачи
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Task {
    /**
     * Текст задачи
     */
    public static final String TASK_TEXT = """
            ПОСТАНОВКА ЗАДАЧИ:
            На плоскости задано множество треугольников. Найти
            два треугольника, чтобы площадь фигуры, находящейся
            внутри обоих треугольников, была максимальна.""";


    /**
     *  коэффициент колёсика мыши
     */
    private static final float WHEEL_SENSITIVE = 0.001f;

    /**
     * Вещественная система координат задачи
     */
    private final CoordinateSystem2d ownCS;
    /**
     * Список точек
     */
    private final ArrayList<Point> points;
    /**
     * Список треугольников
     */
    private final ArrayList<Triangle> triangles;
    /**
     * Размер точки
     */
    private static final int POINT_SIZE = 3;
    /**
     * Последняя СК окна
     */
    private CoordinateSystem2i lastWindowCS;
    /**
     * Флаг, решена ли задача
     */
    private boolean solved;
    /**
     * Список треугольников в ответе
     */
    private final ArrayList<Triangle> answerTriangles;
    /**
     * Список вершин многоугольника в ответе
     */
    private ArrayList<Vector2d> answerPoints;
    /**
     * Порядок разделителя сетки, т.е. раз в сколько отсечек
     * будет нарисована увеличенная
     */
    private static final int DELIMITER_ORDER = 10;

    /**
     * Задача
     *
     * @param ownCS  СК задачи
     * @param points массив точек
     */
    @JsonCreator
    public Task(@JsonProperty("ownCS") CoordinateSystem2d ownCS, @JsonProperty("points") ArrayList<Point> points) {
        this.ownCS = ownCS;
        this.points = points;
        this.triangles = new ArrayList<>();
        this.answerTriangles = new ArrayList<>();
        this.answerPoints = new ArrayList<>();
    }

    /**
     * Рисование
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // Сохраняем последнюю СК
        lastWindowCS = windowCS;
        // рисуем координатную сетку
        renderGrid(canvas, lastWindowCS);
        // рисуем задачу
        renderTask(canvas, windowCS);
    }

    /**
     * Рисование задачи
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    private void renderTask(Canvas canvas, CoordinateSystem2i windowCS) {
        canvas.save();
        // создаём перо
        try (var paint = new Paint()) {
            paint.setColor(POINT_COLOR);
            for (Triangle t : triangles) {
                // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                // а в классическом представлении - вверх
                Vector2i windowPos1 = windowCS.getCoords(t.points.get(0).x, t.points.get(0).y, ownCS);
                Vector2i windowPos2 = windowCS.getCoords(t.points.get(1).x, t.points.get(1).y, ownCS);
                Vector2i windowPos3 = windowCS.getCoords(t.points.get(2).x, t.points.get(2).y, ownCS);
                // рисуем точку
                canvas.drawLine(windowPos1.x, windowPos1.y, windowPos2.x, windowPos2.y, paint);
                canvas.drawLine(windowPos2.x, windowPos2.y, windowPos3.x, windowPos3.y, paint);
                canvas.drawLine(windowPos1.x, windowPos1.y, windowPos3.x, windowPos3.y, paint);
            }
            if (!solved) {
                paint.setColor(POINT_COLOR);
                for (Point p : points) {
                    // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                    // а в классическом представлении - вверх
                    Vector2i windowPos = windowCS.getCoords(p.pos.x, p.pos.y, ownCS);
                    // рисуем точку
                    canvas.drawRect(Rect.makeXYWH(windowPos.x - POINT_SIZE, windowPos.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2), paint);
                }
            } else {
                paint.setColor(CROSSED_COLOR);
                for (Triangle t : answerTriangles) {
                    // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                    // а в классическом представлении - вверх
                    Vector2i windowPos1 = windowCS.getCoords(t.points.get(0).x, t.points.get(0).y, ownCS);
                    Vector2i windowPos2 = windowCS.getCoords(t.points.get(1).x, t.points.get(1).y, ownCS);
                    Vector2i windowPos3 = windowCS.getCoords(t.points.get(2).x, t.points.get(2).y, ownCS);
                    // рисуем точку
                    canvas.drawLine(windowPos1.x, windowPos1.y, windowPos2.x, windowPos2.y, paint);
                    canvas.drawLine(windowPos2.x, windowPos2.y, windowPos3.x, windowPos3.y, paint);
                    canvas.drawLine(windowPos1.x, windowPos1.y, windowPos3.x, windowPos3.y, paint);
                }

                paint.setColor(SUBTRACTED_COLOR);
                for (int i = 0; i < answerPoints.size(); i++) {
                    Vector2i windowPos1 = windowCS.getCoords(answerPoints.get(i).x, answerPoints.get(i).y, ownCS);
                    Vector2i windowPos2 = windowCS.getCoords(answerPoints.get((i + 1) % answerPoints.size()).x, answerPoints.get((i + 1) % answerPoints.size()).y, ownCS);
                    canvas.drawLine(windowPos1.x, windowPos1.y, windowPos2.x, windowPos2.y, paint);
                }
            }
        }
        canvas.restore();
    }

    /**
     * Добавить точку
     *
     * @param pos      положение
     */
    public void addPoint(Vector2d pos) {
        solved = false;
        Point newPoint = new Point(pos);
        points.add(newPoint);
        PanelLog.info("точка " + newPoint + " добавлена");
        if (points.size() == 3) {
            triangles.add(new Triangle(points.get(0).getPos(), points.get(1).getPos(), points.get(2).getPos()));
            points.clear();
            PanelLog.info("треугольник " + triangles.get(triangles.size() - 1) + " добавлен");
        }
    }


    /**
     * Клик мыши по пространству задачи
     *
     * @param pos         положение мыши
     * @param mouseButton кнопка мыши
     */
    public void click(Vector2i pos, MouseButton mouseButton) {
        if (lastWindowCS == null) return;
        // получаем положение на экране
        Vector2d taskPos = ownCS.getCoords(pos, lastWindowCS);
        // добавляем во множество при нажатии кнопки
        if (mouseButton.equals(MouseButton.PRIMARY) || mouseButton.equals(MouseButton.SECONDARY)) {
            addPoint(taskPos);
        }
    }


    /**
     * Добавить случайные точки
     *
     * @param cnt кол-во случайных точек
     */
    public void addRandomTriangles(int cnt) {
        CoordinateSystem2i addGrid = new CoordinateSystem2i(30, 30);

        for (int i = 0; i < cnt; i++) {
            Vector2i gridPos1 = addGrid.getRandomCoords();
            Vector2d pos1 = ownCS.getCoords(gridPos1, addGrid);

            Vector2i gridPos2 = addGrid.getRandomCoords();
            Vector2d pos2 = ownCS.getCoords(gridPos2, addGrid);

            Vector2i gridPos3 = addGrid.getRandomCoords();
            Vector2d pos3 = ownCS.getCoords(gridPos3, addGrid);

            addPoint(pos1);
            addPoint(pos2);
            addPoint(pos3);
        }
    }


    /**
     * Рисование сетки
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void renderGrid(Canvas canvas, CoordinateSystem2i windowCS) {
        // сохраняем область рисования
        canvas.save();
        // получаем ширину штриха(т.е. по факту толщину линии)
        float strokeWidth = 0.03f / (float) ownCS.getSimilarity(windowCS).y + 0.5f;
        // создаём перо соответствующей толщины
        try (var paint = new Paint().setMode(PaintMode.STROKE).setStrokeWidth(strokeWidth).setColor(TASK_GRID_COLOR)) {
            // перебираем все целочисленные отсчёты нашей СК по оси X
            for (int i = (int) (ownCS.getMin().x); i <= (int) (ownCS.getMax().x); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(i, 0, ownCS);
                // каждый 10 штрих увеличенного размера
                float strokeHeight = i % DELIMITER_ORDER == 0 ? 5 : 2;
                // рисуем вертикальный штрих
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x, windowPos.y + strokeHeight, paint);
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x, windowPos.y - strokeHeight, paint);
            }
            // перебираем все целочисленные отсчёты нашей СК по оси Y
            for (int i = (int) (ownCS.getMin().y); i <= (int) (ownCS.getMax().y); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(0, i, ownCS);
                // каждый 10 штрих увеличенного размера
                float strokeHeight = i % 10 == 0 ? 5 : 2;
                // рисуем горизонтальный штрих
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x + strokeHeight, windowPos.y, paint);
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x - strokeHeight, windowPos.y, paint);
            }
        }
        // восстанавливаем область рисования
        canvas.restore();
    }


    /**
     * Очистить задачу
     */
    public void clear() {
        points.clear();
        triangles.clear();
        solved = false;
    }

    /**
     * Решить задачу
     */
    public void solve() {
        if (triangles.size() >= 2) {
            // очищаем списки
            answerTriangles.clear();
            answerPoints.clear();

            //добавляем случайные точки для метода Монте-Карло
            ArrayList<Vector2d> monteKarlo = new ArrayList<>();
            int countRandom = 10000;
            double fullSquare = ownCS.getMax().x * ownCS.getMax().y;
            for (int i = 0; i < countRandom; i++) {
                monteKarlo.add(ownCS.getRandomCoords());
            }

            double maxSquare = 0;
            // перебираем пары треугольников
            for (int i = 0; i < triangles.size(); i++) {
                for (int j = i + 1; j < triangles.size(); j++) {
                    int countContains = 0;
                    for (int k = 0; k < countRandom; k++) {
                        if (triangles.get(i).contains(monteKarlo.get(k)) && triangles.get(j).contains(monteKarlo.get(k))) {
                            countContains++;
                        }
                    }
                    double square = fullSquare * (double) countContains / (double) countRandom;

                    if (square > maxSquare) {
                        maxSquare = square;
                        answerTriangles.clear();
                        answerTriangles.add(triangles.get(i));
                        answerTriangles.add(triangles.get(j));
                    }
                }
            }

//            triangles.remove(answerTriangles.get(0));
//            triangles.remove(answerTriangles.get(1));

            if (answerTriangles.size() == 2) {
                answerPoints = Triangle.crossingTriangles(answerTriangles.get(0), answerTriangles.get(1));
            }

            // задача решена
            solved = true;
        }
    }
    /**
     * Отмена решения задачи
     */
    public void cancel() {
//        triangles.add(answerTriangles.get(0));
//        triangles.add(answerTriangles.get(1));
        solved = false;
    }

    /**
     * проверка, решена ли задача
     *
     * @return флаг
     */
    public boolean isSolved() {
        return solved;
    }

    /**
     * Масштабирование области просмотра задачи
     *
     * @param delta  прокрутка колеса
     * @param center центр масштабирования
     */
    public void scale(float delta, Vector2i center) {
        if (lastWindowCS == null) return;
        // получаем координаты центра масштабирования в СК задачи
        Vector2d realCenter = ownCS.getCoords(center, lastWindowCS);
        // выполняем масштабирование
        ownCS.scale(1 + delta * WHEEL_SENSITIVE, realCenter);
    }

    /**
     * Получить положение курсора мыши в СК задачи
     *
     * @param x        координата X курсора
     * @param y        координата Y курсора
     * @param windowCS СК окна
     * @return вещественный вектор положения в СК задачи
     */
    @JsonIgnore
    public Vector2d getRealPos(int x, int y, CoordinateSystem2i windowCS) {
        return ownCS.getCoords(x, y, windowCS);
    }


    /**
     * Рисование курсора мыши
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     * @param font     шрифт
     * @param pos      положение курсора мыши
     */
    public void paintMouse(Canvas canvas, CoordinateSystem2i windowCS, Font font, Vector2i pos) {
        // создаём перо
        try (var paint = new Paint().setColor(TASK_GRID_COLOR)) {
            // сохраняем область рисования
            canvas.save();
            // рисуем перекрестие
            canvas.drawRect(Rect.makeXYWH(0, pos.y - 1, windowCS.getSize().x, 2), paint);
            canvas.drawRect(Rect.makeXYWH(pos.x - 1, 0, 2, windowCS.getSize().y), paint);
            // смещаемся немного для красивого вывода текста
            canvas.translate(pos.x + 3, pos.y - 5);
            // положение курсора в пространстве задачи
            Vector2d realPos = getRealPos(pos.x, pos.y, lastWindowCS);
            // выводим координаты
            canvas.drawString(realPos.toString(), 0, 0, font, paint);
            // восстанавливаем область рисования
            canvas.restore();
        }
    }

}
