package com.example.nayeer.squiro.Graphics;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import com.example.nayeer.squiro.GameEngine.GameView;
import java.util.Random;

public class Obstacle {
    float left, top, right, bottom;
    int y;
    Rect formattingDestRect;
    Bitmap finalBitmap; //avec le gap
    GameView context;
    int ballSize;
    int height, width;

    public Obstacle(GameView context, int height, int y) {
        this.y = y;
        this.context = context;
        this.ballSize = context.getGapSize();

        this.height = height;
        this.width = context.getPlatformBmp().getWidth();

        this.formattingDestRect = new Rect(0,y, width - 1, height+y );

        this.finalBitmap = createBitmapWithGap();
    }

    private Bitmap createBitmapWithGap() {
        Bitmap originalBitmap = context.getPlatformBmp();
        Bitmap bitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        canvas.drawBitmap(originalBitmap, 0, 0, paint);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        int max = context.getViewWidth() - (int) 1.1*ballSize;
        int min = 0;
        Random random = new Random();


        top = 0; // basically (X1, Y1)
        left = random.nextInt(max - min + 1) + min;
        right = left + (int) (1.1 * ballSize); // width (distance from X1 to X2)
        bottom = top + originalBitmap.getHeight(); // height (distance from Y1 to Y2)
        canvas.drawRect(left, top, right, bottom, paint);

        return bitmap;
    }

    public int getGapPosition() {
        return (int) ((left+right)/2);
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(finalBitmap, null, formattingDestRect, null);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        formattingDestRect = new Rect(0,y, width - 1, height+y );
    }
}
