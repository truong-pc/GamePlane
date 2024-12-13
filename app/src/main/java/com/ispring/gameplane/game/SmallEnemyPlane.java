package com.ispring.gameplane.game;

import android.graphics.Bitmap;

public class SmallEnemyPlane extends EnemyPlane {

    public SmallEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(2);//2 health (2 bullets to kill)
        setValue(1000);//Give 1000 points when destroyed
    }
    //
}