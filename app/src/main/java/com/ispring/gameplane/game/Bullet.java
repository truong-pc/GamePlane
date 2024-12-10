package com.ispring.gameplane.game;

import android.graphics.Bitmap;

public class Bullet extends AutoSprite {

    public Bullet(Bitmap bitmap){
        super(bitmap);
        setSpeed(-10); //Bullets fly upward
    }

}