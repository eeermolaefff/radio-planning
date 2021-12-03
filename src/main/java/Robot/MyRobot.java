package Robot;

import OIP.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;

public class MyRobot {
    public static class Point {
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    public static Robot robot;

    static {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            robot = new Robot(ge.getDefaultScreenDevice());
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    private static Integer k = 0;
    private static final int delayDuration = 200;
    private static double imageFactor;
    private static Point offset;
    public static void openPrevWindow() {
        robot.keyPress(KeyEvent.VK_ALT);
        robot.delay(50);
        robot.keyPress(KeyEvent.VK_TAB);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_TAB);
    }
    private static void moveMouse(Point point) {
        robot.mouseMove(point.x, point.y);
    }
    private static void clickMouse(int typeEvent) {
        robot.mousePress(typeEvent);
        robot.delay(50);
        robot.mouseRelease(typeEvent);
    }
    private static void clickKey(int typeEvent) {
        robot.keyPress(typeEvent);
        robot.delay(50);
        robot.keyRelease(typeEvent);
    }
    private static Point mouseOffSetPosition(int x, int y) {
        int relativeX = (int)(x * imageFactor);
        int relativeY = (int)(y * imageFactor);
        return new Point(relativeX + offset.x, relativeY + offset.y);
    }
    private static Point mouseOffSetPosition(Point p) {
        int relativeX = (int)(p.x * imageFactor);
        int relativeY = (int)(p.y * imageFactor);
        return new Point(relativeX + offset.x, relativeY + offset.y);
    }
    private static void takeProperWallType(boolean isInnerContour) {
        Point wallTypePos;
        if (isInnerContour) wallTypePos = new Point(585, 135);
        else                wallTypePos = new Point(545, 135);
        moveMouse(wallTypePos);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);

        robot.delay(delayDuration);

        if (isInnerContour) wallTypePos = new Point(500, 270);
        else                wallTypePos = new Point(500, 360);

        moveMouse(wallTypePos);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
    }
    private static void setupScreen(String fileName) throws IOException {
        MyRobot.robot.delay(300);
        String screenName = "screen.jpeg";
        String offsetName = "offset.txt";

        MyRobot.screenCapture(screenName);
        ImageProcessor.screenCalibration(screenName);

        File file = new File(offsetName);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String[] tokens = br.readLine().split(" ");
        br.close();

        offset = new Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));

        BufferedImage picture = ImageIO.read(new File(fileName));
        imageFactor = (double) Integer.parseInt(tokens[2]) / (double) picture.getWidth();

        file.delete();
        new File(screenName).delete();
    }
    private static void drawWallsTomo(Point p1, Point p2) throws IOException {
        Point mousePos = mouseOffSetPosition(p1.x, p1.y);
        moveMouse(mousePos);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(delayDuration);

        mousePos = mouseOffSetPosition(p2.x, p2.y);
        moveMouse(mousePos);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(delayDuration);

        clickKey(KeyEvent.VK_ESCAPE);
        robot.delay(delayDuration);
    }
    private static void drawWallsTomo(Point[] points) throws IOException {
        for (Point point : points) {
            Point mousePos = mouseOffSetPosition(point);
            moveMouse(mousePos);
            clickMouse(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(delayDuration);
        }
        clickKey(KeyEvent.VK_ESCAPE);
    }
    public static void drawInnerContour(String fileName) throws IOException {
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);

        robot.delay(1000);
        takeProperWallType(true);

        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(" ");
            Point p1 = new Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
            Point p2 = new Point(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
            drawWallsTomo(p1, p2);
            line = reader.readLine();
        }
        reader.close();
        fr.close();
    }
    public static void drawOutsideContour(String fileName) throws IOException {
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);

        robot.delay(1000);
        takeProperWallType(false);

        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(" ");
            Point[] points = new Point[tokens.length/2];
            for (int i = 0; i < tokens.length; i+=2) {
                int x = Integer.parseInt(tokens[i]);
                int y = Integer.parseInt(tokens[i+1]);
                points[i/2] = new Point(x, y);
            }
            drawWallsTomo(points);
            line = reader.readLine();
        }
        reader.close();
        fr.close();
    }
    public static void putContourOnMap(String filePath) throws IOException {
        setupScreen(filePath + "cropped.jpeg");
        drawInnerContour(filePath + "inner.txt");
        drawOutsideContour(filePath + "outside.txt");
    }
    public static void pasteText(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }
    public static void switchLevel(int floor) {
        robot.mouseMove(40, 135);
        robot.delay(200);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(200);

        int delta_y = 20;
        Point firstFloor = new Point(1635, 255);
        robot.mouseMove(firstFloor.x, firstFloor.y + delta_y * floor);
        robot.delay(200);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(1000);
    }
    public static void screenCapture(String fileName) {
        BufferedImage image = null;
        try {
            image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        } catch (AWTException e) {
            e.printStackTrace();
        }
        try {
            ImageIO.write(image, "jpeg", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, AWTException {
        MyRobot.openPrevWindow();
        MyRobot.robot.delay(500);
        String screenName = "screen.jpeg";
        MyRobot.screenCapture(screenName);
        ImageProcessor.screenCalibration(screenName);
    }
}
