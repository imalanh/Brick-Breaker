package excelion;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
//minimum of 120 blocks
//100 points per powerup collected
//at least 5 powerups implemented out of: laser, englarge, catch, slow, speed, multi[x], life
//green, 1; purple, 2; blue, 3; yellow 4; red, 5; grey, 6
//tink for invincible brick collision
//ricochet for breakable brick collision

public class Arkanoid extends JPanel implements Runnable {
    private ArrayList<GameObject> objects = new ArrayList<>();
    private ArrayList<PowerUp> puppies = new ArrayList<>();
    private String[] powerups = new String[] {"multiballs", "enlarge", "lasers", "extralife", "speed"}; 
    private ArrayList<Blocks> blocks = new ArrayList<>();
    private Blocks[] blcArray = new Blocks[blocks.size()];
    private BufferedImage[] images = new BufferedImage[10];
    private Player ship;
    private Ball ball;
    private boolean run = true;
    private boolean hasEnlarge = false;
    private int score = 0;
    private File fontFile = new File("/Users/alanhu/Desktop/excelion/src/images/American Captain.TTF");
    private JPanel information = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            try { //importing font
		Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(30f);
                g.setFont(font);
                g.setColor(Color.WHITE);
            } catch (IOException | FontFormatException io) {}
            
            FontMetrics fm = g.getFontMetrics();
            g.drawString("u can do it fam :) ", 5, 5 + fm.getAscent());
            g.drawString("SCORE", this.getWidth() - 5 - fm.stringWidth("SCORE"), 5 + fm.getAscent());
            g.drawString(score + "", this.getWidth() - 5 - fm.stringWidth(score + ""), this.getHeight() - 5);
            
            try {
                for (int i = ship.numLives() - 1; i > -1; i--) {
                    g.drawImage(images[1], 4 + 27 * i, this.getHeight() - 7 - fm.getAscent(), fm.getAscent(), 25, null);
                }
            } catch (NullPointerException npe) {             
            }
        }
    };
    private JPanel panelContainer = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(images[0], 0, 0, getWidth(), getHeight(), null);
        }
    };
    
    public Arkanoid() {       
        try { //importing images and sounds
            Clip clip = AudioSystem.getClip(); //hot-fix for clip initialization buffer lag
            images[0] = ImageIO.read(new File("/Users/alanhu/Desktop/excelion/src/images/Space [1].jpg"));
            images[1] = ImageIO.read(new File("/Users/alanhu/Desktop/excelion/src/images/Heart.png"));
        } catch (IOException | LineUnavailableException e) {
            System.out.println("Failure in creating images.");
        }
        
        JFrame frame = new JFrame();
        Box boxLayout = new Box(BoxLayout.Y_AXIS);
        Dimension dimension = new Dimension(650, 600);
        
        information.setOpaque(false);
        information.setPreferredSize(new Dimension(650, 75));
        information.setMaximumSize(new Dimension(650, 75));
        information.setMinimumSize(new Dimension(650, 75));
        information.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        
        this.setPreferredSize(dimension);
        this.setOpaque(false);
        this.setMaximumSize(dimension);
        this.setMinimumSize(dimension);
        this.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        boxLayout.add(Box.createVerticalStrut(20));
        boxLayout.add(this);     
        boxLayout.add(Box.createVerticalStrut(5));
        boxLayout.add(information);     
        boxLayout.add(Box.createVerticalStrut(25));

        panelContainer.add(boxLayout);
        frame.add(panelContainer);
        frame.setTitle("A Game to Stifle Imperial Progression (A Game in Progress)");
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setSize(new Dimension(700, 955));
        frame.setMinimumSize(frame.getMinimumSize());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        this.setFocusable(true);
        
        addMouseListener(new MouseClicked());       
        addMouseMotionListener(new MousePosition());
        addKeyListener(new KeySelected());

        ship = new Player(getWidth() / 2 - (64 + 14) / 2, getHeight() - 40, 64, 14, Color.CYAN);
        //ball starting position set to middle of ship
        double xBall = ship.getX() + (int)ship.getActualWidth() / 2 - 5; //centre x and y of ship
        double yBall = ship.getY() - 13;
        ball = new Ball((int)xBall, (int)yBall, 10, new Color(255, 255, 255), 0, 0);
        
        //block creation
        int brickWidth = 50;
        int brickHeight = 22;
        int brickhp = 1;
        Random rand = new Random(); //0 to 16
        for (int x = 0; x < this.getWidth() / brickWidth; x++) {
            for (int y = 0; y < 9; y++) {
                int imageNum = rand.nextInt(16) + 48 - brickhp * 16;
                Blocks brick = new Blocks(0 + brickWidth * x, 150 + brickHeight * y, 50, 22, Color.WHITE, imageNum + 1, brickhp);
                blocks.add(brick);
            }
        }
        blcArray = blocks.toArray(blcArray);
                
        objects.add(ship);
        objects.add(ball);
        start();
    }
    
    public void playSound(String type) {
        String file = "/Users/alanhu/Desktop/excelion/src/sounds/";
        Random rand = new Random();
        
        switch (type) {
            case "Brick Collision":
                //file = file + "block_collision" + (rand.nextInt(2) + 1);
                file = file + "block_collision4";
                break;
            case "Ship Collision":
                file = file + "ship_collision";
                break;
            case "Death":
                file = file + "death_music1";
                break;
            case "Powerup Collected":
                file = file + "powerup_collected1";
                break;
        }
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(file + ".wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            System.out.println(e);
        }
    }
    
    public void start() {
        Thread thread = new Thread(this);
        run = true;        
        thread.start();
    }

    public void stop() {
        run = false;
    }

    public void run() {
        int ballsAlive = 1;

        while (run) {
            boolean hasMultiballs = ballsAlive > 1;
            //boolean hasPower = hasMultiballs == false && hasEnlarge == false;
            
            ListIterator objectsList = objects.listIterator();
            while (objectsList.hasNext()) {
                Object i = objectsList.next();
                if (i instanceof Ball) {
                    if (((Ball)i).checkCollision(ship) == true) { //make an adjustment for checkCollision
                        playSound("Ship Collision");
                        ship.showColour();
                    }
                    for (int n = 0; n < blcArray.length; n++) { //checking brick characteristics
                        if (blcArray[n] == null) {
                            continue;
                        }
                        if (((Ball)i).blockCollision(blcArray[n]) == true) {
                            score = score + 50;
                            playSound("Brick Collision");
                            blcArray[n].minushp();
                            if (blcArray[n].gethp() == 0) {
                                //creating powerups
                                int cX = (int)blcArray[n].getX() + blcArray[n].getWidth() / 2 - 10; //x of where the powerup thing spawns
                                int cY = (int)blcArray[n].getY() + blcArray[n].getHeight() / 2; //y of where the powerup thing spawns
                                blcArray[n] = null; //deleting the dead brick
                                
                                Random rand = new Random();
                                //String powType = powerups[rand.nextInt(5)]; //5 is exclusive
                                int dropRate = rand.nextInt(10);
                                if (dropRate > 3) {
                                    PowerUp power = new PowerUp(cX, cY, 18, 10, Color.WHITE, powerups[rand.nextInt(2)]);
                                    puppies.add(power);
                                }   
                            }
                        }
                    }
                    ((Ball)i).update(this);
                    if (((Ball)i).getY() == this.getHeight() - (((Ball)i).getRadius() * 2) +- 1 && ballsAlive == 1) { //death
                        ((Ball)i).ballStop();
                        try {
                            if (ship.numLives() > 0) {
                                Thread.sleep(1250);
                            } else {
                                Thread.sleep(50);
                            }
                            puppies = new ArrayList<>();
                            ship.reset(this);
                            ((Ball)i).reset(ship);
                            ((Ball)i).setFired(false);
                            ship.death();
                            if (ship.numLives() < 0) {
                                playSound("Death");
                                System.out.println("GAME OVER");
                                return; //temporary end of game
                            }
                        } catch (InterruptedException e) {
                        }
                    } else if (((Ball)i).getY() == this.getHeight() - (((Ball)i).getRadius() * 2) +- 1 && ballsAlive > 1) {
                        ballsAlive--;
                        objectsList.remove();
                    }
                } else if (i instanceof Player) {
                    ((Player)i).update(this);
                }
            }
            ListIterator powerList = puppies.listIterator();
            while (powerList.hasNext()) {
                Object i = powerList.next();
                ((PowerUp)i).update(this);
                if (ship.powerupCollision((PowerUp)i) == true) {
                    PowerUp pup = (PowerUp)i;
                    score = score + 100;
                    playSound("Powerup Collected");

                    if (pup.getType().equals("multiballs") && hasMultiballs == false) { //activation of powerup
                        Ball b1 = new Ball((int)(ship.getX() + ship.getActualWidth() / 2 - 5), (int)ship.getY() - 13, 10, Color.WHITE, 1, -2);
                        Ball b2 = new Ball((int)(ship.getX() + ship.getActualWidth() / 2 - 5), (int)ship.getY() - 13, 10, Color.WHITE, -1, -1.5);
                        objects.add(b1);
                        objects.add(b2);
                        b1.setFired(true);
                        b2.setFired(true);
                        ballsAlive = ballsAlive + 2;
                    } else if(((PowerUp) i).getType().equals("enlarge") && hasEnlarge == false) {
                        ship.grow(); //method to grow
                        hasEnlarge = true;
                        java.util.Timer timer = new java.util.Timer();
                        TimerTask task = new TimerTask() { //reminder: when an active powerup is collected, reset the timer
                            @Override
                            public void run() {
                                ship.subside();
                                hasEnlarge = false;
                            }
                        };
                        timer.schedule(task, 4000);    
                    }
                    
                    powerList.remove();
                } else if (((PowerUp)i).outOfPanel(this) == true) {
                    powerList.remove();
                }
            }
            panelContainer.repaint();
            information.repaint();
            repaint();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }
    }    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        try {
            ListIterator objectList = objects.listIterator();
            ListIterator powerList = puppies.listIterator();
            for (int n = 0; n < blcArray.length; n++) {
                if (blcArray[n] == null) {
                    continue;
                }
                blcArray[n].paintComponent(g2);
            }
            while (objectList.hasNext()) {
                Object i = objectList.next();
                if (i instanceof Ball) {
                    ((Ball)i).paintComponent(g2);
                } else if (i instanceof Player) {
                    ((Player)i).paintComponent(g2);
                }
            }
            while (powerList.hasNext()) {
                Object i = powerList.next();
                ((PowerUp)i).paintComponent(g2);
            }
        } catch (ConcurrentModificationException cme) {
            System.out.println("Concurrent Modification Exception");
        }
    }
    
    public class MouseClicked implements MouseListener {
        public void mouseClicked(MouseEvent e) {
            for (GameObject go : objects) {
                if (go instanceof Ball) {
                    if (((Ball)go).fired() == false) {
                        ((Ball)go).setSpeed(1, -2);
                        ((Ball)go).setFired(true); 
                    }
                }
            }
        }
        public void mousePressed(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {}

        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}
    }
    
    public class KeySelected implements KeyListener {
        public void keyTyped(KeyEvent e) {}
        
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                for (GameObject go : objects) {
                    if (go instanceof Ball) {
                        if (((Ball)go).fired() == false) {
                            ((Ball)go).setSpeed(1, -2);
                            ((Ball)go).setFired(true); 
                        }
                    }
                }
            }
        }
        public void keyReleased(KeyEvent e) {}
    }
    
    public class MousePosition implements MouseMotionListener {
        public void mouseDragged(MouseEvent e) {}

        public void mouseMoved(MouseEvent e) {
            for (GameObject go : objects) {
                if (go instanceof Ball) {
                    try {
                        if (((Ball)go).fired() == false && (e.getX() - ship.getActualWidth() / 2) > - 3 && e.getX() + ship.getActualWidth() / 2 < getWidth() + 3) {
                            ship.setPosition((int)(e.getX() - ship.getActualWidth() / 2), getHeight() - 40);
                            ((Ball)go).setPosition((int)(ship.getX() + ship.getActualWidth() / 2 - ((Ball)go).getRadius()), (int)((Ball)go).getY());
                        } else if (((Ball)go).fired() == true && (e.getX() - ship.getActualWidth() / 2) > -3 && (e.getX() + ship.getActualWidth() / 2) < getWidth() + 3) {
                            ship.setPosition((int)(e.getX() - ship.getActualWidth() / 2), getHeight() - 40);
                        }
                    } catch (NullPointerException npe) {
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        Arkanoid game = new Arkanoid();
    }
}