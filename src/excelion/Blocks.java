package excelion;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Blocks extends GameObject {
    private BufferedImage image;
    private Rectangle2D rectangle;
    private int x, y, width, height;
    private int imageNum;
    private int maxhp, hp;
    private Area area;
    
    public Blocks(int x, int y, int width, int height, Color color, int imageNum, int hp) {
        super(x, y, width, height, color);
        rectangle = new Rectangle2D.Double(x, y, width, height);  
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxhp = hp;
        this.hp = hp;
        try {
            if (hp == 0) {
                image = ImageIO.read(new File("/Users/alanhu/Desktop/excelion/src/invincible_bricks/0.png"));
            } else {
                image = ImageIO.read(new File("/Users/alanhu/Desktop/excelion/src/undamaged_bricks/FULL_ (" + imageNum + ").png"));
            }
        } catch (IOException io) {
        }
        area = new Area(rectangle);
        this.imageNum = imageNum - (16 * 3) + (16 * maxhp);
    }
    
    public Rectangle2D getRectangle() {
        return rectangle;
    }
    
    public int maxhp() {
        return maxhp;
    }
    
    public Area getArea() {
        return area;
    }
    
    public boolean checkCollision(Ball ball) {
        if (ball.getY() <= y + height && ball.getY() > y + height - ball.getRadius() && ball.getX() + ball.getRadius() >= x && ball.getX() + ball.getRadius() <= x + width) { //bottom
            ball.setSpeed(ball.getXSpeed(), -ball.getYSpeed());
            return true;
        } else if (ball.getY() + ball.getHeight() >= y && ball.getY() <= y + ball.getRadius() && ball.getX() + ball.getRadius() >= x && ball.getX() + ball.getRadius() <= x + width) { //top
            ball.setSpeed(ball.getXSpeed(), -ball.getYSpeed());
            return true;
        } else if (ball.getX() <= x + width && ball.getX() >= x + width - ball.getRadius() && ball.getY() + ball.getRadius() >= y && ball.getY() + ball.getRadius() <= y + height) { //right
            ball.setSpeed(-ball.getXSpeed(), ball.getYSpeed());
            return true;
        } else if (ball.getX() + ball.getRadius() * 2 >= x && ball.getX() <= x + ball.getRadius() && ball.getY() + ball.getRadius() >= y && ball.getY() + ball.getRadius() <= y + height) { //left
            ball.setSpeed(-ball.getXSpeed(), ball.getYSpeed());
            return true;
        } else {
            return false;
        }
    }
    
    public void minushp() {
        hp--;
        int colournum = maxhp - hp;
        try {
            if (hp > 0) {
                image = ImageIO.read(new File(".\\src\\damaged_bricks\\" + colournum + "_ (" + imageNum + ").png"));
            }
        } catch (IOException io) {
            System.out.println("Image not found!");
        }
    }
    
    public int gethp() {
        return hp;
    }
    
    public void update(Arkanoid panel) {
        
    }
    
    public void paintComponent(Graphics2D g2) {
        rectangle.setFrame(x, y, width, height);
        g2.drawImage(image, x, y, width, height, null);
    }
}