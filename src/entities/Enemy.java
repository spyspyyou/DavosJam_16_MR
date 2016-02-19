package entities;

import main.Main;
import util.PictureImport;

public class Enemy extends Entity{
    private long lastShotTime = 0;
    private int shootCooldown = 2000;
    private static final int PARTICLE_SPEED = 20;
    private static final int RANGE = 500;


    public Enemy(int xCoordinate, int yCoordinate) {
        super(xCoordinate, yCoordinate, Player.PLAYER_SIZE,Player.PLAYER_SIZE, PictureImport.importImage("Enemy_1.png"));
    }

    public void update(){
        if(lastShotTime + shootCooldown <= System.currentTimeMillis() &&
                Math.pow(Main.player.x - x, 2) + Math.pow(Main.player.y - y, 2) <= RANGE * RANGE){
            if(Main.rng(0.05f)){
                shoot();
            }
        }
    }
    private void shoot(){
        Main.hostileParticles.add(new HostileParticle(x, y, (byte)(-1)));
        lastShotTime = System.currentTimeMillis();
        System.out.println("Enemy shot a particle");
    }

}
