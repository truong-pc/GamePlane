package com.ispring.gameplane.game;

import android.graphics.Bitmap;

public class MiddleEnemyPlane extends EnemyPlane {

    public MiddleEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(4);//4 health (4 bullets to kill)
        setValue(6000);//Give 6000 points when destroyed
    }
}