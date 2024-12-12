package com.ispring.gameplane.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Explosion extends Sprite {

    private int segment = 14; //Explosion effect is composed of 14 segments
    private int level = 0; //Starts at segment 0
    private int explodeFrequency = 2; //Each segment draws 2 frames

    public Explosion(Bitmap bitmap){
        super(bitmap);
    }

    @Override
    public float getWidth() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null){
            return bitmap.getWidth() / segment;
        }
        return 0;
    }

    @Override
    public Rect getBitmapSrcRec() {
        Rect rect = super.getBitmapSrcRec();
        int left = (int)(level * getWidth());
        rect.offsetTo(left, 0);
        return rect;
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            if(getFrame() % explodeFrequency == 0){
                //Level is incremented by 1 to draw the next segment
                level++;
                if(level >= segment){
                    //When all segments are drawn, destroy the explosion effect
                    destroy();
                }
            }
        }
    }

    //Number of frames required to draw the complete explosion effect (28 frames)
    public int getExplodeDurationFrame(){
        return segment * explodeFrequency;
    }
}