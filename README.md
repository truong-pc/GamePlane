# GamePlane

Game Description
----

 1. The plane continuously fires bullets, and you can change the plane's position by sliding your finger.
 2. Different enemy planes have different resistance levels. When an enemy plane is hit by a certain number of bullets, it will explode with an animation effect.
 3. Every once in a while, there will be rewards such as double bullets or bombs.
 4. After obtaining double bullets, the bullets become double.
 5. After obtaining the bomb reward, you can destroy all enemy planes on the screen by double-clicking.



Implementation
--

 - We defined the `Sprite` class, which is the sprite class. The planes, bullets, reward items, ..., in the game all inherit from this class. We control the sprite's position through methods like `moveTo()` and `move()`, and implement the corresponding drawing logic through `beforeDraw()`, `onDraw()`, and `afterDraw()`. The inheritance of the sprite class and its subclasses is shown below:


 - `GameView` is our custom View class, mainly overriding the `onDraw()` and `onTouchEvent()` methods. The `onDraw()` source code is as follows:

	```
	@Override
	protected void onDraw(Canvas canvas) {
	    // We check whether the conditions for triggering a single click event are met after the doubleClickDurationTime milliseconds have passed since the last single click event.
	    if(isSingleClick()){
	        onSingleClick(touchX, touchY);
	    }
	
	    super.onDraw(canvas);
	
	    if(status == STATUS_GAME_STARTED){
	        drawGameStarted(canvas);
	    }else if(status == STATUS_GAME_PAUSED){
	        drawGamePaused(canvas);
	    }else if(status == STATUS_GAME_OVER){
	        drawGameOver(canvas);
	    }
	}
	```

    At a certain moment, `GameView` has three states: game started `STATUS_GAME_STARTED`, game paused `STATUS_GAME_PAUSED`, and game over `STATUS_GAME_OVER`. In different states, we call different drawing methods. These methods all call the `postInvalidate()` method, which drives the View to continuously redraw, thereby continuously calling the `onDraw()` method to achieve the dynamic effect of the game. For drawing techniques, you can refer to another blog post [Detailed Explanation of Canvas Drawing Basics in Android (with source code download)](http://blog.csdn.net/iispring/article/details/49770651).
 
 - We also override the `onTouchEvent()` method of `GameView`. Since View only supports single-click events and not double-click events, we define our own `resolveTouchType()` method to synthesize the event types we want, such as double-click events. We record the time of `MotionEvent.ACTION_DOWN` and `MotionEvent.ACTION_UP`. A single-click event is composed of two events, ACTION_DOWN and ACTION_UP. If the interval between ACTION_DOWN and ACTION_UP is less than 200 milliseconds, we consider it a single-click event. A double-click event is composed of two single-click events. If the interval between two single-click events is less than 300 milliseconds, we consider it a double-click event. When a double-click event is triggered, we trigger a bomb to destroy all enemy planes on the screen. When in the ACTION_MOVE state, we change the position of the fighter plane through `event.getX()` and `event.getY()`. For detailed information on `MotionEvent`, you can refer to another blog post [Touch Event Mechanism in Android](http://blog.csdn.net/iispring/article/details/50364126).
 
 - We also provide methods such as `start()`, `pause()`, `resume()`, and `destroy()` for `GameView`, making it have a lifecycle similar to that of an Activity, which is convenient for managing the state of `GameView` in an Activity.
 
 - Small enemy planes are small in size and have low resistance; medium enemy planes are medium in size and have medium resistance; large enemy planes are large in size and have high resistance. When an enemy plane is destroyed, we use an explosion effect with the following image:
  
  This image demonstrates the effect of the explosion from start to finish in 14 stages. We use two frames to draw one stage of the explosion, so it takes 28 frames to complete the drawing of an explosion effect. After drawing the last stage, the `Explosion` class will destroy itself.
   

**If you think it's good, feel free to Star and Fork!**
