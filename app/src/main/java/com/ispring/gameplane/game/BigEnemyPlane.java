package com.ispring.gameplane.game;

import android.graphics.Bitmap;

public class BigEnemyPlane extends EnemyPlane {

    public BigEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(10); //10 health (10 bullets to kill)
        setValue(30000); //Give 30000 points when destroyed
    }

}