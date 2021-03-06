package entities;

import input.Keyboard;
import main.Main;
import util.PictureImport;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends Entity{

    public static int maxLife;
    public static int life;
    public static int speed=5;
    private static boolean moving = false;
    private BufferedImage img, img1, _img, _img1, _attackingImg, attackingImg;
    long lastImgChange = 0;
    private final int ATTACK_DURATION = Main.TICKS / 2;
    private final int ATTACK_COOLDOWN = Main.TICKS*2;
    public static int PLAYER_SIZE=256;
    public static int WALK_BORDER_LEFT = PLAYER_SIZE/2;
    public static int WALK_BORDER_RIGHT = PLAYER_SIZE*3;
    private static final int LIFE_BAR_WIDTH = 200;
    private static final int LIFE_BAR_HEIGHT = 30;
    private static final int ATTACK_RANGE = 50;
    private static int lastRainbowTime = 0;
    private static final int RAINBOW_COOLDOWN = 100;
    public static float ySpeed = 0;
    public static float gravity = 1.7f;
    private final int START_X=WALK_BORDER_LEFT;
    private final int START_Y=Main.panel.getHeight()-Player.PLAYER_SIZE-Level.TILE_SCREEN_SIZE;

    private int lastAttackChange=-ATTACK_COOLDOWN;
    public static boolean attacking = false;

    public Player(int xCoordinate, int yCoordinate){
        super(xCoordinate,yCoordinate, PLAYER_SIZE, PLAYER_SIZE, PictureImport.importImage("Einhorn_1.png"));
        maxLife = 10000;
        life = maxLife;
        img = PictureImport.importImage("Einhorn_1.png");
        img1 = PictureImport.importImage("Einhorn_2.png");
        _img = PictureImport.importImage("Einhorn_1.png");
        _img1 = PictureImport.importImage("Einhorn_2.png");

        for (int i=0;i<_img.getWidth()/2;i++)
            for (int j=0;j<_img.getHeight();j++)
            {
                int tmp = _img.getRGB(i, j);
                _img.setRGB(i, j, _img.getRGB(_img.getWidth()-i-1, j));
                _img.setRGB(_img.getWidth()-i-1, j, tmp);
            }

        for (int i=0;i<_img1.getWidth()/2;i++)
            for (int j=0;j<_img1.getHeight();j++)
            {
                int tmp = _img1.getRGB(i, j);
                _img1.setRGB(i, j, _img1.getRGB(_img1.getWidth()-i-1, j));
                _img1.setRGB(_img1.getWidth()-i-1, j, tmp);
            }

        attackingImg = PictureImport.importImage("Einhorn_Attack.png");
        _attackingImg = PictureImport.importImage("Einhorn_Attack.png");
        for (int i=0;i<_attackingImg.getWidth()/2;i++)
            for (int j=0;j<_attackingImg.getHeight();j++)
            {
                int tmp = _attackingImg.getRGB(i, j);
                _attackingImg.setRGB(i, j, _attackingImg.getRGB(_attackingImg.getWidth()-i-1, j));
                _attackingImg.setRGB(_attackingImg.getWidth()-i-1, j, tmp);
            }

    }

    public void render(Graphics g){
        g.setColor(Color.GRAY);
        g.fillRect(x-rCL, y + 280, LIFE_BAR_WIDTH, LIFE_BAR_HEIGHT);
        g.setColor(Color.RED);
        g.fillRect(x-rCL, y + 280, LIFE_BAR_WIDTH * life / maxLife, LIFE_BAR_HEIGHT);

        if(!attacking) {
            if(moving) {
                if (speed > 0) {
                    if (System.currentTimeMillis() - 100 > lastImgChange) {
                        if (bi != img1) {
                            bi = img1;
                        } else bi = img;
                        lastImgChange = System.currentTimeMillis();
                    }
                } else if (speed < 0) {
                    if (System.currentTimeMillis() - 100 > lastImgChange) {
                        if (bi != _img) {
                            bi = _img;
                        } else bi = _img1;
                        lastImgChange = System.currentTimeMillis();
                    }
                }
            }

        } else {
            if(lastAttackChange + ATTACK_DURATION < Main.tick){
                attacking = false;
                if(speed > 0) bi = img;
                else bi = _img;
            } else if(speed > 0){
                bi = attackingImg;
            } else bi = _attackingImg;
        }
        super.render(g);
    }

    @Override
    public void update() {
        if(Keyboard.getPressedKeys(65))moveX(-1);
        else if(Keyboard.getPressedKeys(68))moveX(1);
        else moving = false;
        moveY();
        if(Keyboard.getPressedKeys(32)){
            jump();
        }
        if(System.currentTimeMillis() > lastRainbowTime + RAINBOW_COOLDOWN){
            if(speed > 0)Main.rainbows.add(new Rainbow(x,(int)( y + 140 + 20 * (Math.random() - 0.5))));
            else if(speed < 0)Main.rainbows.add(new Rainbow(x + 220, (int)( y + 140 + 20 * (Math.random() - 0.5))));
        }
        life+=30*Math.pow(1.2f, Main.upgrades[1].level);
        if(life > maxLife){
            life = maxLife;
        }else if(life<=0){
            life = 0;
        }

        if(attacking){
            for(Enemy e : Main.activeEnemies){
                if(speed > 0) {
                    if (e.x > x + sizeX - 100 && e.x - x - sizeX < ATTACK_RANGE) {
                        if (Math.abs((y + sizeY) / 2 - (e.y + e.sizeY) / 2) < 100) e.die();
                    }
                } else if (e.x < x + 100 && x - e.x - e.sizeX < ATTACK_RANGE) {
                    if (Math.abs((y + sizeY) / 2 - (e.y + e.sizeY) / 2) < 100) e.die();
                }
            }
        }
        System.out.println(x);
    }

    public void startAttack(){
        if(!attacking&&(lastAttackChange+ATTACK_COOLDOWN)<=Main.tick){
            System.out.println("ATAAAAACK!!");
            attacking = true;
            lastAttackChange = Main.tick;
            if(speed < 0) bi = _attackingImg;
            else bi = attackingImg;
        }
    }

    private void moveX(int i){
        moving=true;
        if(i==1){
            speed=Math.abs(speed);
        }else{
            speed=-Math.abs(speed);
        }
        x+=speed;
        if(Level.getSolid(x+20, y+20)||Level.getSolid(x+20, y + PLAYER_SIZE/2+20)){
            x=((x+20)/Level.TILE_SCREEN_SIZE+1)*Level.TILE_SCREEN_SIZE-20;
        }else if(Level.getSolid(x+PLAYER_SIZE-20, y+20)||Level.getSolid(x+PLAYER_SIZE-20, y + PLAYER_SIZE/2+20)){
            x=((x+PLAYER_SIZE-20)/Level.TILE_SCREEN_SIZE)*Level.TILE_SCREEN_SIZE+20-PLAYER_SIZE;
        }
        if(x<WALK_BORDER_LEFT){
            moving = false;
            x = WALK_BORDER_LEFT;
        }
        if(x-rCL<WALK_BORDER_LEFT){
            rCL-=WALK_BORDER_LEFT-(x-rCL);
            if(rCL<0)rCL=0;
        }
        else if(x-rCL>(Main.panel.getWidth()-WALK_BORDER_RIGHT)){
            rCL+=(x-rCL)-(Main.panel.getWidth()-WALK_BORDER_RIGHT);
            if(rCL>Level.length-Main.panel.getWidth())rCL=Level.length-Main.panel.getWidth();
        }

    }

    private void moveY(){
        if(!Level.getSolid(x+20, y + sizeY + 1)&&!Level.getSolid(x+PLAYER_SIZE/2, y + sizeY + 1)&&!Level.getSolid(x+PLAYER_SIZE-50, y + sizeY + 1)){
            ySpeed += gravity;
        } else {
            ySpeed = ySpeed < 0 ? ySpeed : 0;
        }
        y += ySpeed;
        if(y>Main.panel.getHeight()){
            Main.lost();
        }
        if(Level.getSolid(x+20, y + sizeY + 1)||Level.getSolid(x+PLAYER_SIZE/2, y + sizeY + 1)||Level.getSolid(x+PLAYER_SIZE-50, y + sizeY + 1)) {
            y =((y + sizeY + 1)/Level.TILE_SCREEN_SIZE)*Level.TILE_SCREEN_SIZE-PLAYER_SIZE;
        }
    }
    private void jump(){
        if(Level.getSolid(x+50, y + sizeY + 1)||Level.getSolid(x+PLAYER_SIZE/2, y + sizeY + 1)||Level.getSolid(x+PLAYER_SIZE-50, y + sizeY + 1)){
            System.out.println("jump!");
            ySpeed = -30;
        }
    }

    public void setBack() {
        x=START_X;
        y=START_Y;
    }
}
