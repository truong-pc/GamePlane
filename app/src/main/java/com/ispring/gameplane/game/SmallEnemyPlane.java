package com.ispring.gameplane.game;

import android.graphics.Bitmap;

public class SmallEnemyPlane extends EnemyPlane {

    public SmallEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(1);//1 health (1 bullets to kill)
        setValue(1000);//Give 1000 points when destroyed
    }
    //
}