package Robot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MyRobot {
    public static class Point {
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    public native void processImage(String fileName, int kernelSize, int minWallLen, int contourExpansion);
    public native void processImage(String fileName, String noEnvImage, int kernelSize, int minWallLen, int contourExpansion);
    public native void imageCalibration(String fileName, int kernelSize);

    static {
        System.loadLibrary("opencv_world454");
        System.loadLibrary("ImageProcessor");
    }

    private static Robot robot;
    private static final int delayDuration = 200;
    private static final double screenResolutionFactor = 500/625.0;
    private static double imageFactor;
    private static Point offset;

    public MyRobot() throws AWTException {
        robot = new Robot();
    }
    private static void openPrevWindow() {
        robot.keyPress(KeyEvent.VK_ALT);
        robot.delay(50);
        robot.keyPress(KeyEvent.VK_TAB);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_ALT);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_TAB);
    }
    private static void moveMouseRelatively(Point point) {
        int relativeX = (int)(point.x * screenResolutionFactor);
        int relativeY = (int)(point.y * screenResolutionFactor);
        robot.mouseMove(relativeX, relativeY);
    }
    private static void moveMouseRelatively(int x, int y) {
        int relativeX = (int)(x * screenResolutionFactor);
        int relativeY = (int)(y * screenResolutionFactor);
        robot.mouseMove(relativeX, relativeY);
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
        if (isInnerContour) wallTypePos = new Point(730, 175);
        else                wallTypePos = new Point(673, 175);
        moveMouseRelatively(wallTypePos);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);

        robot.delay(delayDuration);

        if (isInnerContour) wallTypePos = new Point(700, 335);
        else                wallTypePos = new Point(700, 450);
        moveMouseRelatively(wallTypePos);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
    }
    private static void setupScreen(String fileName, Point cur_offset, int relativeResolutionX) throws IOException {
        offset = cur_offset;
        BufferedImage picture = ImageIO.read(new File(fileName));
        imageFactor = (double) relativeResolutionX / (double) picture.getWidth();
    }
    private static void drawWallsTomo(Point p1, Point p2) throws IOException {
        Point mousePos = mouseOffSetPosition(p1.x, p1.y);
        moveMouseRelatively(mousePos);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(delayDuration);

        mousePos = mouseOffSetPosition(p2.x, p2.y);
        moveMouseRelatively(mousePos);
        clickMouse(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(delayDuration);

        clickKey(KeyEvent.VK_ESCAPE);
        robot.delay(delayDuration);
    }
    private static void drawWallsTomo(Point[] points) {
        for (Point point : points) {
            Point mousePos = mouseOffSetPosition(point);
            moveMouseRelatively(mousePos);
            clickMouse(InputEvent.BUTTON1_DOWN_MASK);
            robot.delay(delayDuration);
        }
        clickKey(KeyEvent.VK_ESCAPE);
    }
    public static void drawInnerContour(String fileName) throws IOException {
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);

        robot.delay(2000);
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

        robot.delay(2000);
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
    public static void putContourOnMap(String filePath, Point _offset, int resulution) throws IOException {
        setupScreen(filePath + "cropped.jpeg", _offset, resulution);
        openPrevWindow();
        drawInnerContour(filePath + "inner.txt");
        drawOutsideContour(filePath + "outside.txt");
    }
    public static BufferedImage toBuffered(Image img) {
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    public static void main(String[] args) throws IOException, AWTException {

    }
}
