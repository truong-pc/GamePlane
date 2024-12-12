package com.ispring.gameplane.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class AutoSprite extends Sprite {
    //Number of pixels moved per frame, with downward as positive
    private float speed = 2;

    public AutoSprite(Bitmap bitmap){
        super(bitmap);
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public float getSpeed(){
        return speed;
    }

    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            //Speed y-axis
            move(0, speed * gameView.getDensity());
        }
    }

    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView){
        if(!isDestroyed()){
            //Destroy the Sprite if it is outside the bounds of the Canvas
            RectF canvasRecF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            RectF spriteRecF = getRectF();
            if(!RectF.intersects(canvasRecF, spriteRecF)){
                destroy();
            }
        }
    }
}