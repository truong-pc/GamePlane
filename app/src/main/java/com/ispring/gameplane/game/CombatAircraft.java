package com.ispring.gameplane.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.SoundPool;

import java.util.List;

public class CombatAircraft extends Sprite {
    private boolean collide = false; //Indicates whether the fighter has been hit
    private int bombAwardCount = 0; //Number of bombs available

    //Double bullet?
    private boolean single = true;
    private int doubleTime = 0;
    private int maxDoubleTime = 140;

    //Flashing after being hit
    private long beginFlushFrame = 0; //Start flashing the fighter at the beginFlushFrame frame
    private int flushTime = 0; //Number of flashes
    private int flushFrequency = 16;
    private int maxFlushTime = 10;
    private long lastGunshotFrame = 0;
    private int gunshotDelayFrames = 7;


    public CombatAircraft(Bitmap bitmap){
        super(bitmap);
    }

    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            validatePosition(canvas);

            //Fire bullets every 7 frames
            if(getFrame() % 7 == 0){
                fight(gameView, gameView.getSoundPool(), gameView.getGunshotSoundId());
            }
        }
    }

    //Check if the fighter is completely within the Canvas
    private void validatePosition(Canvas canvas){
        if(getX() < 0){
            setX(0);
        }
        if(getY() < 0){
            setY(0);
        }
        RectF rectF = getRectF();
        int canvasWidth = canvas.getWidth();
        if(rectF.right > canvasWidth){
            setX(canvasWidth - getWidth());
        }
        int canvasHeight = canvas.getHeight();
        if(rectF.bottom > canvasHeight){
            setY(canvasHeight - getHeight());
        }
    }

    //Shoot Bullets
    public void fight(GameView gameView, SoundPool soundPool, int gunshotSoundId){
        //If the fighter is hit or destroyed, no bullets will be fired
        if(collide || isDestroyed()){
            return;
        }
        long currentFrame = getFrame();
        if (currentFrame - lastGunshotFrame >= gunshotDelayFrames) {
            if (soundPool != null && gunshotSoundId != 0) {
                soundPool.play(gunshotSoundId, 0.3f, 0.3f, 1, 0, 1.0f);
            }
            lastGunshotFrame = currentFrame;
        }

        float x = getX() + getWidth() / 2;
        float y = getY() - 5;
        if(single){
            //Fires a single bullet in single shot mode
            Bitmap yellowBulletBitmap = gameView.getYellowBulletBitmap();
            Bullet yellowBullet = new Bullet(yellowBulletBitmap);
            yellowBullet.moveTo(x, y);
            gameView.addSprite(yellowBullet);
        }
        else{
            //Fires two bullets in double shot mode
            float offset = getWidth() / 4;
            float leftX = x - offset;
            float rightX = x + offset;
            Bitmap blueBulletBitmap = gameView.getBlueBulletBitmap();

            Bullet leftBlueBullet = new Bullet(blueBulletBitmap);
            leftBlueBullet.moveTo(leftX, y);
            gameView.addSprite(leftBlueBullet);

            Bullet rightBlueBullet = new Bullet(blueBulletBitmap);
            rightBlueBullet.moveTo(rightX, y);
            gameView.addSprite(rightBlueBullet);

            doubleTime++;
            if(doubleTime >= maxDoubleTime){
                single = true;
                doubleTime = 0;
            }
        }
    }

    //If the fighter is hit, execute the explosion effect
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView){
        if(isDestroyed()){
            return;
        }

        //Check whether the fighter has been hit or not
        if(!collide){
            List<EnemyPlane> enemies = gameView.getAliveEnemyPlanes();
            for(EnemyPlane enemyPlane : enemies){
                Point p = getCollidePointWithOther(enemyPlane); //p is distance between the fighter and enemies
                if(p != null){
                    explode(gameView);
                    break;
                }
            }
        }

        //beginFlushFrame == 0 -> flashing mode has not been entered
        //beginFlushFrame > 0 -> flash mode will be entered in the first frame
        if(beginFlushFrame > 0){
            long frame = getFrame();
            //If the current frame number >= beginFlushFrame -> the fighter enters flashing state before destroyed
            if(frame >= beginFlushFrame){
                if((frame - beginFlushFrame) % flushFrequency == 0){
                    boolean visible = getVisibility();
                    setVisibility(!visible);
                    flushTime++;
                    if(flushTime >= maxFlushTime){
                        //If the fighter flashes more than the maximum number of flashes, destroy the fighter
                        destroy();
                        //gameOver();
                    }
                }
            }
        }

        //Check if award taken
        if(!collide){
            //Check if bomb award taken
            List<BombAward> bombAwards = gameView.getAliveBombAwards();
            for(BombAward bombAward : bombAwards){
                Point p = getCollidePointWithOther(bombAward);
                if(p != null){
                    bombAwardCount++;
                    bombAward.destroy();
                    //Game.receiveBombAward();
                }
            }

            //Check if bullet award taken
            List<BulletAward> bulletAwards = gameView.getAliveBulletAwards();
            for(BulletAward bulletAward : bulletAwards){
                Point p = getCollidePointWithOther(bulletAward);
                if(p != null){
                    bulletAward.destroy();
                    single = false;
                    doubleTime = 0;
                }
            }
        }
    }

    //Fighter explosion
    private void explode(GameView gameView){
        if(!collide){
            collide = true;
            setVisibility(false);
            float centerX = getX() + getWidth() / 2;
            float centerY = getY() + getHeight() / 2;
            Explosion explosion = new Explosion(gameView.getExplosionBitmap());
            explosion.centerTo(centerX, centerY);
            gameView.addSprite(explosion);
            gameView.playExplosionSound();
            beginFlushFrame = getFrame() + explosion.getExplodeDurationFrame();
        }
    }

    //Number of bombs available
    public int getBombCount(){
        return bombAwardCount;
    }

    //Use bomb
    public void bomb(GameView gameView){
        if(collide || isDestroyed()){
            return;
        }

        if(bombAwardCount > 0){
            List<EnemyPlane> enemyPlanes = gameView.getAliveEnemyPlanes();
            for(EnemyPlane enemyPlane : enemyPlanes){
                enemyPlane.explode(gameView);
            }
            bombAwardCount--;
        }
    }

    public boolean isCollide(){
        return collide;
    }

    public void setNotCollide(){
        collide = false;
    }
}