package com.example.nayeer.squiro.Graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

import com.example.nayeer.squiro.R;

public class Squiro {

    private Context context;
    private Bitmap squirell_front = null;
    private Bitmap squirell_runningLeft = null;
    private Bitmap squirell_runningRight = null;
    private static final float ratioWidthHeightSquirellFront = 500f/555;
    private static final float ratioWidthHeightSquirellRunning = 1180f/500;
    private static final int frameCountSquirellFront = 25;
    private static final int frameCountSquirellRunning = 3;
    private static final int STAY_MODE = 1;
    private static final int RUNNING_LEFT_MODE = 2;
    private static final int RUNNING_RIGHT_MODE = 3;
    private int actual_mode;
    private Bitmap actualModeBitmap = null;


    private float x = 0, y = 0;
    private int runningModeFrameWidth, frontModeFrameWidth;
    private int runningModeFrameHeight, frontModeFrameHeight;
    private int currentFrameWidth=0, currentFrameHeight=0;
    private int currentFrameCount;
    private int currentFrame = 0;
    private long lastFrameChangeTime = 0;
    private int frameLengthInMillisecond = 50;

    private Rect frameToDraw;
    private RectF whereToDraw;


    public Squiro(Context context, int maxSquirellHeight) {
        this.context = context;

        runningModeFrameHeight = (int) (0.8*maxSquirellHeight);
        frontModeFrameHeight = (int) (1.3*maxSquirellHeight);

        runningModeFrameWidth = (int) (runningModeFrameHeight * ratioWidthHeightSquirellRunning);
        frontModeFrameWidth = (int) (frontModeFrameHeight * ratioWidthHeightSquirellFront);

        frameToDraw = new Rect(0, 0, 0, 0);
        whereToDraw = new RectF(0, 0, 0, 0);

        initBitmaps();
        stayMode();
    }

    public void initBitmaps() {

        //FRONT
        int height = frontModeFrameHeight;
        int width = frontModeFrameWidth;
        int frameCount = frameCountSquirellFront;

        squirell_front = BitmapFactory.decodeResource(context.getResources(), R.drawable.squirell_front);
        squirell_front = Bitmap.createScaledBitmap(squirell_front, width * frameCount, height , false);

        //RUNNING
        height = runningModeFrameHeight;
        width = runningModeFrameWidth;
        frameCount = frameCountSquirellRunning;
        squirell_runningRight = BitmapFactory.decodeResource(context.getResources(), R.drawable.squirell_running_right);
        squirell_runningRight = Bitmap.createScaledBitmap(squirell_runningRight,  width * frameCount, height, false);


        squirell_runningLeft = BitmapFactory.decodeResource(context.getResources(), R.drawable.squirell_running_left);
        squirell_runningLeft = Bitmap.createScaledBitmap(squirell_runningLeft, width * frameCount, height, false);
    }

    public void stayMode() {
        reset();
        currentFrameHeight = frontModeFrameHeight;
        currentFrameWidth = frontModeFrameWidth;
        currentFrameCount = frameCountSquirellFront;

        actualModeBitmap = squirell_front;
        updateBoundRectangles();
        actual_mode = STAY_MODE;
    }

    public void runningMode(boolean isRight) { //true pour Right et false pour Left.

        reset();
        currentFrameHeight = runningModeFrameHeight;
        currentFrameWidth = runningModeFrameWidth;
        currentFrameCount = frameCountSquirellRunning;

        if (isRight) {
            actualModeBitmap = squirell_runningRight;
            actual_mode = RUNNING_RIGHT_MODE;
        } else {
            actualModeBitmap = squirell_runningLeft;
            actual_mode = RUNNING_LEFT_MODE;
        }

        updateBoundRectangles();

    }

    public int getMode() {
        return actual_mode;
    }

    public void reset() {
        currentFrame = 0;
        lastFrameChangeTime = 0;
    }

    public void updateBoundRectangles() {
        frameToDraw.set(0, 0, currentFrameWidth, currentFrameHeight);
        whereToDraw.set(x, y, x + currentFrameWidth, y+currentFrameHeight);
    }

    public void draw(Canvas canvas) {
        manageCurrentFrame();
        canvas.drawBitmap(actualModeBitmap, frameToDraw, whereToDraw, null);
    }

    public void setPos(int x, int y) {
        //En mode stay on décale légèrement pour que en mode frontal on se retrouve au niveau des pattes de l'écureuil.
        if (actual_mode == STAY_MODE) {
            int new_x = (int) (x + 0.30*runningModeFrameWidth);
            this.x = new_x;
        } else {
            this.x = x;
        }
        this.y = y;
        updatePos();
    }

    public void updatePos() {
            whereToDraw.set((int) x, (int) y, (int) x + currentFrameWidth, (int) y + currentFrameHeight);
    }

    public int getFrameHeight() {
        return this.currentFrameHeight;
    }

    public int getFrameWidth() {
        return this.currentFrameWidth;
    }

    public int getRunningModeFrameWidth() {
        return runningModeFrameWidth;
    }

    public int getFrontModeFrameWidth() {
        return frontModeFrameWidth;
    }

    public float getX() {return this.x;}

    public float getY() {return this.y;}

    public void manageCurrentFrame() {
        long time = System.currentTimeMillis();


        if (time > lastFrameChangeTime + frameLengthInMillisecond) {
            lastFrameChangeTime = time;
            currentFrame++;

            if (currentFrame >= currentFrameCount) {
                currentFrame = 0;
            }
        }

        frameToDraw.left = currentFrame * currentFrameWidth;
        frameToDraw.right = frameToDraw.left + currentFrameWidth;
    }


}
