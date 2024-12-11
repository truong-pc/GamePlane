package com.ispring.gameplane.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Award extends AutoSprite {
    public static int STATUS_DOWN1 = 1;
    public static int STATUS_UP2 = 2;
    public static int STATUS_DOWN3 = 3;

    private int status = STATUS_DOWN1;

    public Award(Bitmap bitmap){
        super(bitmap);
        setSpeed(7);
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {

        if(!isDestroyed()){
            //Change direction or speed after drawing a certain number of times
            int canvasHeight = canvas.getHeight();
            if(status != STATUS_DOWN3){
                float maxY = getY() + getHeight();
                if(status == STATUS_DOWN1){
                    if(maxY >= canvasHeight * 0.25){
                        //Changes direction and goes up when it drops to the certain height for the first time
                        setSpeed(-5);
                        status = STATUS_UP2;
                    }
                }
                else if(status == STATUS_UP2){
                    if(maxY+this.getSpeed() <= 0){
                        //Changes direction and goes down when it rises to the certain height for the second time
                        setSpeed(13);
                        status = STATUS_DOWN3;
                    }
                }
            }
            if(status == STATUS_DOWN3){
                if(getY() >= canvasHeight){
                    destroy();
                }
            }
        }
    }
}