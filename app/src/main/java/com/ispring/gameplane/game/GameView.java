package com.ispring.gameplane.game;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.SoundPool;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ispring.gameplane.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;




public class GameView extends View {

    private Paint paint;
    private Paint textPaint;
    private CombatAircraft combatAircraft = null;
    private List<Sprite> sprites = new ArrayList<Sprite>();
    private List<Sprite> spritesNeedAdded = new ArrayList<Sprite>();
    /*
    0:combatAircraft
    1:explosion
    2:yellowBullet
    3:blueBullet
    4:smallEnemyPlane
    5:middleEnemyPlane
    6:bigEnemyPlane
    7:bombAward
    8:bulletAward
    9:pause1
    10:pause2
    11:bomb
    */
    private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private Bitmap backgroundBitmap;
    private SoundPool soundPool;
    private int gunshotSoundId;
    private int explosionSoundId;


    private float density = getResources().getDisplayMetrics().density;//Screen density
    public static final int STATUS_GAME_STARTED = 1;//Game Start
    public static final int STATUS_GAME_PAUSED = 2;//Game Pause
    public static final int STATUS_GAME_OVER = 3;//Game over
    public static final int STATUS_GAME_DESTROYED = 4;//Game Destruction
    private int status = STATUS_GAME_DESTROYED;//Before destroyed state
    private long frame = 0;//Total number of frames drawn
    private long score = 0;//Total score
    private float fontSize = 12;//Total score font size
    private float fontSize2 = 20;//Game over font size
    private float borderSize = 2;//Game Over Dialog border size
    private Rect continueRect = new Rect();//"Continue" and "Restart" buttons

    //Touch event related variables
    private static final int TOUCH_MOVE = 1;//Moving
    private static final int TOUCH_SINGLE_CLICK = 2;//Click/Tap
    private static final int TOUCH_DOUBLE_CLICK = 3;//Double click/tap
    private static final int singleClickDurationTime = 200;
    //Double-click = two single clicks. If time between two clicks is less than 300 milliseconds -> it is a double-click
    private static final int doubleClickDurationTime = 300;
    private long lastSingleClickTime = -1;//When the last click occurred
    private long touchDownTime = -1;//When click/tap
    private long touchUpTime = -1;//When stop click/tap
    private float touchX = -1;//x-coord of touch point
    private float touchY = -1;//y-coord of touch point

    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public SoundPool getSoundPool() {
        return soundPool;
    }

    public int getGunshotSoundId() {
        return gunshotSoundId;
    }

    public void playExplosionSound() {
        if (soundPool != null && explosionSoundId != 0) {
            soundPool.play(explosionSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }


    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GameView, defStyle, 0);
        a.recycle();
        //Initialize paint
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        //Font
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        textPaint.setColor(0xFF003366);
        fontSize = textPaint.getTextSize();
        fontSize *= density;
        fontSize2 *= density;
        textPaint.setTextSize(fontSize);
        borderSize *= density;
    }

    public void start(int[] bitmapIds){
        destroy();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .build();
        } else {
            soundPool = new SoundPool(10, android.media.AudioManager.STREAM_MUSIC, 0); // maxStreams = 5
        }

        // Gun sound
        gunshotSoundId = soundPool.load(getContext(), R.raw.gunshot, 1);

        // Explosion sound
        explosionSoundId = soundPool.load(getContext(), R.raw.explosion, 1);

        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_image);
        for(int bitmapId : bitmapIds){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmapId);
            bitmaps.add(bitmap);
        }
        startWhenBitmapsReady();
    }

    private void startWhenBitmapsReady(){
        combatAircraft = new CombatAircraft(bitmaps.get(0));
        //Game start
        status = STATUS_GAME_STARTED;
        postInvalidate();
    }

    private void restart(){
        destroyNotRecyleBitmaps();
        startWhenBitmapsReady();
    }

    public void pause(){
        //Game pause
        status = STATUS_GAME_PAUSED;
    }

    private void resume(){
        //Game resume
        status = STATUS_GAME_STARTED;
        postInvalidate();
    }

    private long getScore(){
        //Game score
        return score;
    }

    /*-------------------------------draw-------------------------------------*/

    @Override
    protected void onDraw(Canvas canvas) {
        //Check in each frame whether the conditions for delaying the click event are met
        if(isSingleClick()){
            onSingleClick(touchX, touchY);
        }

        super.onDraw(canvas);

        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        }

        if(status == STATUS_GAME_STARTED){
            drawGameStarted(canvas);
        }else if(status == STATUS_GAME_PAUSED){
            drawGamePaused(canvas);
        }else if(status == STATUS_GAME_OVER){
            drawGameOver(canvas);
        }
    }

    //Draw the game
    private void drawGameStarted(Canvas canvas){

        drawScoreAndBombs(canvas);

        //When start, center the fighter
        if(frame == 0){
            float centerX = canvas.getWidth() / 2;
            float centerY = canvas.getHeight() - combatAircraft.getHeight() / 2;
            combatAircraft.centerTo(centerX, centerY);
        }

        //Add spritesNeedAdded to sprites
        if(spritesNeedAdded.size() > 0){
            sprites.addAll(spritesNeedAdded);
            spritesNeedAdded.clear();
        }

        //Check if bullets are in front of fighter
        destroyBulletsFrontOfCombatAircraft();

        //Remove the destroyed Sprite before drawing
        removeDestroyedSprites();

        //Add a random Sprite every 30 frames
        if(frame % 30 == 0){
            createRandomSprites(canvas.getWidth());
        }
        frame++;

        //Draw stuffs
        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()){
            Sprite s = iterator.next();

            //Check to destroy or not
            if(!s.isDestroyed()){
                s.draw(canvas, paint, this);
            }
            if(s.isDestroyed()){
                //If the Sprite is destroyed, remove
                iterator.remove();
            }
        }

        if(combatAircraft != null){
            //Draw the fighter
            combatAircraft.draw(canvas, paint, this);
            if(combatAircraft.isDestroyed()){
                //If the fighter is hit and destroyed, the game ends
                status = STATUS_GAME_OVER;
            }
            //View continues to render to see explosion effect
            postInvalidate();
        }
    }

    // Draw the paused state of the game
    private void drawGamePaused(Canvas canvas){
        drawScoreAndBombs(canvas);

        // Call the onDraw method of Sprite instead of the draw method to render static Sprites without changing their positions
        for(Sprite s : sprites){
            s.onDraw(canvas, paint, this);
        }
        if(combatAircraft != null){
            combatAircraft.onDraw(canvas, paint, this);
        }

        // Draw the dialog to display the score
        drawScoreDialog(canvas, "Continue");

        if(lastSingleClickTime > 0){
            postInvalidate();
        }
    }

    // Draw the game-over state
    private void drawGameOver(Canvas canvas){
        // After Game Over, only the dialog showing the final score is drawn
        drawScoreDialog(canvas, "Restart");

        if(lastSingleClickTime > 0){
            postInvalidate();
        }
    }

    private void drawScoreDialog(Canvas canvas, String operation){
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        // Store original values
        float originalFontSize = textPaint.getTextSize();
        Paint.Align originalFontAlign = textPaint.getTextAlign();
        int originalColor = paint.getColor();
        Paint.Style originalStyle = paint.getStyle();
    /*
    W = 360
    w1 = 20
    w2 = 320
    buttonWidth = 140
    buttonHeight = 42
    H = 558
    h1 = 150
    h2 = 60
    h3 = 124
    h4 = 76
    */
        int w1 = (int)(20.0 / 360.0 * canvasWidth);
        int w2 = canvasWidth - 2 * w1;
        int buttonWidth = (int)(140.0 / 360.0 * canvasWidth);

        int h1 = (int)(150.0 / 558.0 * canvasHeight);
        int h2 = (int)(60.0 / 558.0 * canvasHeight);
        int h3 = (int)(124.0 / 558.0 * canvasHeight);
        int h4 = (int)(76.0 / 558.0 * canvasHeight);
        int buttonHeight = (int)(42.0 / 558.0 * canvasHeight);

        canvas.translate(w1, h1);
        // Draw background color
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFD8E9F0);
        Rect rect1 = new Rect(0, 0, w2, canvasHeight - 2 * h1);
        canvas.drawRect(rect1, paint);
        // Draw the border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF5D3A9B);
        paint.setStrokeWidth(borderSize);
        //paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawRect(rect1, paint);
        // Draw the text "Score"
        textPaint.setTextSize(fontSize2);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Score", w2 / 2, (h2 - fontSize2) / 2 + fontSize2, textPaint);
        // Draw the line below "Score"
        canvas.translate(0, h2);
        canvas.drawLine(0, 0, w2, 0, paint);
        // Draw the actual score
        String allScore = String.valueOf(getScore());
        canvas.drawText(allScore, w2 / 2, (h3 - fontSize2) / 2 + fontSize2, textPaint);
        // Draw the line below the score
        canvas.translate(0, h3);
        canvas.drawLine(0, 0, w2, 0, paint);
        // Draw the button border
        Rect rect2 = new Rect();
        rect2.left = (w2 - buttonWidth) / 2;
        rect2.right = w2 - rect2.left;
        rect2.top = (h4 - buttonHeight) / 2;
        rect2.bottom = h4 - rect2.top;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7);
        canvas.drawRect(rect2, paint);
        // Draw the text "Continue" or "Restart"
        canvas.translate(0, rect2.top);
        canvas.drawText(operation, w2 / 2, (buttonHeight - fontSize2) / 2 + fontSize2, textPaint);
        continueRect = new Rect(rect2);
        continueRect.left = w1 + rect2.left;
        continueRect.right = continueRect.left + buttonWidth;
        continueRect.top = h1 + h2 + h3 + rect2.top;
        continueRect.bottom = continueRect.top + buttonHeight;

        // Reset
        textPaint.setTextSize(originalFontSize);
        textPaint.setTextAlign(originalFontAlign);
        paint.setColor(originalColor);
        paint.setStyle(originalStyle);
    }

    // Draw the score at the top-left and the number of bombs at the bottom-left
    private void drawScoreAndBombs(Canvas canvas){
        // Draw the pause button at the top-left
        Bitmap pauseBitmap = status == STATUS_GAME_STARTED ? bitmaps.get(9) : bitmaps.get(10);
        RectF pauseBitmapDstRecF = getPauseBitmapDstRecF();
        float pauseLeft = pauseBitmapDstRecF.left;
        float pauseTop = pauseBitmapDstRecF.top;
        canvas.drawBitmap(pauseBitmap, pauseLeft, pauseTop, paint);
        // Draw the total score at the top-left
        float scoreLeft = pauseLeft + pauseBitmap.getWidth() + 20 * density;
        float scoreTop = fontSize + pauseTop + pauseBitmap.getHeight() / 2 - fontSize / 2;
        canvas.drawText(score + "", scoreLeft, scoreTop, textPaint);

        // Draw the bottom-left
        if(combatAircraft != null && !combatAircraft.isDestroyed()){
            int bombCount = combatAircraft.getBombCount();
            if(bombCount > 0){
                // Draw the bombs at the bottom-left
                Bitmap bombBitmap = bitmaps.get(11);
                float bombTop = canvas.getHeight() - bombBitmap.getHeight();
                canvas.drawBitmap(bombBitmap, 0, bombTop, paint);
                // Draw the bomb count at the bottom-left
                float bombCountLeft = bombBitmap.getWidth() + 10 * density;
                float bombCountTop = fontSize + bombTop + bombBitmap.getHeight() / 2 - fontSize / 2;
                canvas.drawText("X " + bombCount, bombCountLeft, bombCountTop, textPaint);
            }
        }
    }

    // Check for cases where the aircraft is ahead of bullets
    private void destroyBulletsFrontOfCombatAircraft(){
        if(combatAircraft != null){
            float aircraftY = combatAircraft.getY();
            List<Bullet> aliveBullets = getAliveBullets();
            for(Bullet bullet : aliveBullets){
                // Destroy bullets if the aircraft is ahead
                if(aircraftY <= bullet.getY()){
                    bullet.destroy();
                }
            }
        }
    }

    // Remove Sprites that are destroyed
    private void removeDestroyedSprites(){
        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()){
            Sprite s = iterator.next();
            if(s.isDestroyed()){
                iterator.remove();
            }
        }
    }

    // Generate random Sprites
    private void createRandomSprites(int canvasWidth){
        Sprite sprite = null;
        int speed = 2;
        // callTime indicates how many times createRandomSprites has been called
        int callTime = Math.round(frame / 30);
        if((callTime + 1) % 25 == 0){
            // Send power-up items
            if((callTime + 1) % 50 == 0){
                // Send bomb
                sprite = new BombAward(bitmaps.get(7));
            }
            else{
                // Send double bullets
                sprite = new BulletAward(bitmaps.get(8));
            }
        }
        else{
            // Send enemy aircraft
            int[] nums = {0,0,0,0,0,1,0,0,1,0,0,0,0,1,1,1,1,1,1,2};
            int index = (int)Math.floor(nums.length*Math.random());
            int type = nums[index];
            if(type == 0){
                // Small enemy aircraft
                sprite = new SmallEnemyPlane(bitmaps.get(4));
            }
            else if(type == 1){
                // Medium enemy aircraft
                sprite = new MiddleEnemyPlane(bitmaps.get(5));
            }
            else if(type == 2){
                // Large enemy aircraft
                sprite = new BigEnemyPlane(bitmaps.get(6));
            }
            if(type != 2){
                if(Math.random() < 0.33){
                    speed = 4;
                }
            }
        }

        if(sprite != null){
            float spriteWidth = sprite.getWidth();
            float spriteHeight = sprite.getHeight();
            float x = (float)((canvasWidth - spriteWidth)*Math.random());
            float y = -spriteHeight;
            sprite.setX(x);
            sprite.setY(y);
            if(sprite instanceof AutoSprite){
                AutoSprite autoSprite = (AutoSprite)sprite;
                autoSprite.setSpeed(speed);
            }
            addSprite(sprite);
        }
    }


    /*-------------------------------touch------------------------------------*/

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //By calling the resolveTouchType method, get the desired event type
        //Note that the resolveTouchType method will not return TOUCH_SINGLE_CLICK type
        //We will call the isSingleClick method during each execution of the onDraw method to check for single-click events
        int touchType = resolveTouchType(event);
        if(status == STATUS_GAME_STARTED){
            if(touchType == TOUCH_MOVE){
                if(combatAircraft != null){
                    combatAircraft.centerTo(touchX, touchY);
                }
            }else if(touchType == TOUCH_DOUBLE_CLICK){
                if(status == STATUS_GAME_STARTED){
                    if(combatAircraft != null){
                        //Double-click will make the combat aircraft use a bomb
                        combatAircraft.bomb(this);
                    }
                }
            }
        }else if(status == STATUS_GAME_PAUSED){
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }else if(status == STATUS_GAME_OVER){
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }
        return true;
    }

    //Synthesize the desired event type
    private int resolveTouchType(MotionEvent event){
        int touchType = -1;
        int action = event.getAction();
        touchX = event.getX();
        touchY = event.getY();
        if(action == MotionEvent.ACTION_MOVE){
            long deltaTime = System.currentTimeMillis() - touchDownTime;
            if(deltaTime > singleClickDurationTime){
                //Touch point moved
                touchType = TOUCH_MOVE;
            }
        }else if(action == MotionEvent.ACTION_DOWN){
            //Touch point pressed
            touchDownTime = System.currentTimeMillis();
        }else if(action == MotionEvent.ACTION_UP){
            //Touch point released
            touchUpTime = System.currentTimeMillis();
            //Calculate the time difference between touch down and touch up
            long downUpDurationTime = touchUpTime - touchDownTime;
            //If the time difference is less than the time for a single-click event,
            //then consider it as a single click
            if(downUpDurationTime <= singleClickDurationTime){
                //Calculate the time difference from the previous single click
                long twoClickDurationTime = touchUpTime - lastSingleClickTime;

                if(twoClickDurationTime <= doubleClickDurationTime){
                    //If the time difference is less than the double-click event time,
                    //consider it as a double-click event
                    touchType = TOUCH_DOUBLE_CLICK;
                    //Reset variables
                    lastSingleClickTime = -1;
                    touchDownTime = -1;
                    touchUpTime = -1;
                }else{
                    //If this forms a single-click event but not a double-click event, do not trigger the single-click yet
                    //Wait for doubleClickDurationTime milliseconds to see if another single-click occurs
                    //If another occurs, combine the two into a double-click event
                    //Otherwise, trigger the single-click after doubleClickDurationTime milliseconds
                    lastSingleClickTime = touchUpTime;
                }
            }
        }
        return touchType;
    }

    //Call this method in the onDraw method to check for single-click events on each frame
    private boolean isSingleClick(){
        boolean singleClick = false;
        //Check if the last single-click event meets the conditions for triggering a single-click after doubleClickDurationTime
        if(lastSingleClickTime > 0){
            //Calculate the time difference from the last single-click
            long deltaTime = System.currentTimeMillis() - lastSingleClickTime;

            if(deltaTime >= doubleClickDurationTime){
                //If time difference exceeds double-click event time,trigger the delayed single-click event

                singleClick = true;
                //Reset variables
                lastSingleClickTime = -1;
                touchDownTime = -1;
                touchUpTime = -1;
            }
        }
        return singleClick;
    }

    private void onSingleClick(float x, float y){
        if(status == STATUS_GAME_STARTED){
            if(isClickPause(x, y)){
                //Single click on the pause button
                pause();
            }
        }else if(status == STATUS_GAME_PAUSED){
            if(isClickContinueButton(x, y)){
                //Single click on the "Continue" button
                resume();
            }
        }else if(status == STATUS_GAME_OVER){
            if(isClickRestartButton(x, y)){
                //Single click on the "Restart" button
                restart();
            }
        }
    }

    //Check if the pause button was clicked
    private boolean isClickPause(float x, float y){
        RectF pauseRecF = getPauseBitmapDstRecF();
        return pauseRecF.contains(x, y);
    }

    //Check if the "Continue" button was clicked while pause
    private boolean isClickContinueButton(float x, float y){
        return continueRect.contains((int)x, (int)y);
    }

    //Check if the "Restart" button was clicked while in GAME OVER
    private boolean isClickRestartButton(float x, float y){
        return continueRect.contains((int)x, (int)y);
    }

    private RectF getPauseBitmapDstRecF(){
        Bitmap pauseBitmap = status == STATUS_GAME_STARTED ? bitmaps.get(9) : bitmaps.get(10);
        RectF recF = new RectF();
        recF.left = 15 * density;
        recF.top = 15 * density;
        recF.right = recF.left + pauseBitmap.getWidth();
        recF.bottom = recF.top + pauseBitmap.getHeight();
        return recF;
    }


    /*-------------------------------destroy------------------------------------*/

    private void destroyNotRecyleBitmaps(){
        //Set the game status to destroyed
        status = STATUS_GAME_DESTROYED;

        //Reset frame
        frame = 0;

        //Reset score
        score = 0;

        //Destroy the combat aircraft
        if(combatAircraft != null){
            combatAircraft.destroy();
        }
        combatAircraft = null;

        //Destroy enemy planes, bullets, rewards, explosions
        for(Sprite s : sprites){
            s.destroy();
        }
        sprites.clear();
    }

    public void destroy(){
        destroyNotRecyleBitmaps();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        //Release Bitmap resources
        for(Bitmap bitmap : bitmaps){
            bitmap.recycle();
        }
        bitmaps.clear();
    }


    /*-------------------------------public methods-----------------------------------*/

    //Add Sprite to Sprites
    public void addSprite(Sprite sprite){
        spritesNeedAdded.add(sprite);
    }

    //Add score
    public void addScore(int value){
        score += value;
    }

    public int getStatus(){
        return status;
    }

    public float getDensity(){
        return density;
    }

    public Bitmap getYellowBulletBitmap(){
        return bitmaps.get(2);
    }

    public Bitmap getBlueBulletBitmap(){
        return bitmaps.get(3);
    }

    public Bitmap getExplosionBitmap(){
        return bitmaps.get(1);
    }

    //Get enemies that are active
    public List<EnemyPlane> getAliveEnemyPlanes(){
        List<EnemyPlane> enemyPlanes = new ArrayList<EnemyPlane>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof EnemyPlane){
                EnemyPlane sprite = (EnemyPlane)s;
                enemyPlanes.add(sprite);
            }
        }
        return enemyPlanes;
    }

    //Get active bomb rewards
    public List<BombAward> getAliveBombAwards(){
        List<BombAward> bombAwards = new ArrayList<BombAward>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof BombAward){
                BombAward bombAward = (BombAward)s;
                bombAwards.add(bombAward);
            }
        }
        return bombAwards;
    }

    //Get active bullet rewards
    public List<BulletAward> getAliveBulletAwards(){
        List<BulletAward> bulletAwards = new ArrayList<BulletAward>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof BulletAward){
                BulletAward bulletAward = (BulletAward)s;
                bulletAwards.add(bulletAward);
            }
        }
        return bulletAwards;
    }

    //Get bullets that are active
    public List<Bullet> getAliveBullets(){
        List<Bullet> bullets = new ArrayList<Bullet>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof Bullet){
                Bullet bullet = (Bullet)s;
                bullets.add(bullet);
            }
        }
        return bullets;
    }
}