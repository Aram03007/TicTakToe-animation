package com.example.narek.tictaktoe;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class CrossView extends View {
    private Path path;
    private Paint paint;
    private int animationRowIndex = -1;
    private int animationColIndex = -1;

    private static final float animSpeedInMs = 0.5f;
    private static final long animMsBetweenStrokes = 10;
    private long animLastUpdate;
    private boolean animRunning;
    private int animCurrentCountour;
    private float animCurrentPos;
    private Path animPath;
    private PathMeasure animPathMeasure;
    private int rowCount = 3;
    private OnSellTapListener onSellTapListener;


    public void setOnSellTapListener(OnSellTapListener onSellTapListener) {
        this.onSellTapListener = onSellTapListener;
    }

    private List<ShapeDrawable> shapeDrawables = new ArrayList<>();
    //    sahmanenq massiv vortex 1} nshanakum e X, -1 _ O, isk 0 _ der voroshvac che;
    private int[][] board = {
            {0,0,0},
            {0,0,0},
            {0,0,0}
    };
    private boolean needUpdate = true;




    public static final String TAG = "CROSSVIEW: ";

    private GestureDetectorCompat gestureDetector;
    private int padding = 10;

    public void updateBoard(int[][] board, int animationRowIndex, int animationColIndex) {

        this.board = board;
        for (int i = 0; i < board[0].length; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, board.length);
        }
        this.animationRowIndex = animationRowIndex;
        this.animationColIndex = animationColIndex;

        startAnimation();


        invalidate();
    }



    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }


    private  void initDisplayKanjiView() {

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xff996699);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dipToPixels(getContext(), 10));
        path = new Path();
        animRunning = false;

    }
    public void setPath(Path p) {
        path = p;
    }
    public void startAnimation() {
        animRunning = true;
        animPathMeasure = null;
        invalidate();
    }

    public interface OnSellTapListener {
        void onSellTapped(int row, int col);
    }





    public CrossView(Context context) {
        super(context);
        init(null, 0);
    }

    public CrossView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        initDisplayKanjiView();
    }

    public CrossView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
        initDisplayKanjiView();
    }

    private void init(AttributeSet attrs, int defStyle) {
        gestureDetector = new GestureDetectorCompat(getContext(), new TapRecognizer());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        System.out.println("OnDraw");

        if (needUpdate) {
            recreateBoard();
            needUpdate = false;
        }

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {

                final ShapeDrawable cur = shapeDrawables.get(i * rowCount + j);

                cur.draw(canvas);

                if (i == animationRowIndex && j == animationColIndex) continue;

                if (board[i][j] == 1) {//draw x
                    drawCross(canvas, cur.getBounds());
                }


                if (board[i][j] == -1) {//draw zero
                    drawZero(canvas, cur.getBounds());
                }
            }

            if (animationRowIndex >= 0 && animationColIndex >= 0) {
                final ShapeDrawable cur = shapeDrawables.get(animationRowIndex * rowCount + animationColIndex);
                Rect bounds = cur.getBounds();


                float padding = (float) (bounds.width() * 0.15);
                Path path = new Path();
                if (board[animationRowIndex][animationColIndex] == 1) {
                    //first line
                    path.moveTo(bounds.left + padding, bounds.top + padding);
                    path.lineTo(bounds.right - padding, bounds.bottom - padding);

                    //second line
                    path.moveTo(bounds.right - padding, bounds.top + padding);
                    path.lineTo(bounds.left + padding, bounds.bottom - padding);

                } else  {
                    path.addCircle(bounds.centerX(), bounds.centerY(), bounds.width() * 0.35f, Path.Direction.CW);
                }

                setPath(path);


                if (animRunning) {
                    drawAnimation(canvas);
                } else {
                    if (board[animationRowIndex][animationColIndex] == 1) {
                        drawCross(canvas, cur.getBounds());
                    }else {
                        drawZero(canvas, cur.getBounds());
                    }
                }
            }


        }


    }

    private void drawZero(Canvas canvas, Rect bounds) {
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() * 0.35f, paint);
    }

    private void drawCross(Canvas canvas, Rect bounds) {
        float padding = (float) (bounds.width() * 0.15);
        Path path = new Path();
        //first line
        path.moveTo(bounds.left + padding, bounds.top + padding);
        path.lineTo(bounds.right - padding, bounds.bottom - padding);

        //second line
        path.moveTo(bounds.right - padding, bounds.top + padding);
        path.lineTo(bounds.left + padding, bounds.bottom - padding);

        canvas.drawPath(path, paint);
    }




    private void drawAnimation(Canvas canvas) {
        if (animPathMeasure == null) {
            // Start of animation. Set it up.
            animPathMeasure = new PathMeasure(path, false);
            animPathMeasure.nextContour();
            animPath = new Path();


            animLastUpdate = System.currentTimeMillis();
            animCurrentCountour = 0;
            animCurrentPos = 0.0f;
        } else {
            // Get time since last frame
            long now = System.currentTimeMillis();
            long timeSinceLast = now - animLastUpdate;

            if (animCurrentPos == 0.0f) {
                timeSinceLast -= animMsBetweenStrokes;
            }

            if (timeSinceLast > 0) {
                // Get next segment of path
                float newPos = (float)(timeSinceLast) / animSpeedInMs + animCurrentPos;
                boolean moveTo = (animCurrentPos == 0.0f);
                animPathMeasure.getSegment(animCurrentPos, newPos, animPath, moveTo);
                animCurrentPos = newPos;
                animLastUpdate = now;

                // If this stroke is done, move on to next
                if (newPos > animPathMeasure.getLength()) {
                    animCurrentPos = 0.0f;
                    animCurrentCountour++;
                    boolean more = animPathMeasure.nextContour();
                    // Check if finished
                    if (!more) { animRunning = false; }
                }
            }

            // Draw path
            canvas.drawPath(animPath, paint);
        }

        invalidate();
    }

    private int contentWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int contentHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int startX() {
        int min = Math.min(contentWidth(), contentHeight());
        return  (int) ((contentWidth() - min) * 0.5f) + getPaddingLeft();
    }

    private int startY() {
        int min = Math.min(contentWidth(), contentHeight());
        return  (int) ((contentHeight() - min) * 0.5f) + getPaddingTop();
    }

//    canvas.drawText("Some Text", 10, 25, textPaint);

    private void recreateBoard() {
//        Log.d(TAG, "start x: " + startX() + " start y: " + startY());


        int cellWidth = cellWidth();
        shapeDrawables.clear();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
                shapeDrawable.getPaint().setColor(Color.parseColor("#aae78b"));

                int x = startX() + (i + 1) * padding + cellWidth * i;
                int y = startY() + (j + 1) * padding + cellWidth * j;
                shapeDrawable.setBounds(x, y, x + cellWidth, y + cellWidth);
                shapeDrawables.add(shapeDrawable);
            }
        }
    }

    private int cellWidth() {
        int min = Math.min(contentWidth(), contentHeight());
        return (min - (rowCount + 1) * padding) / rowCount;
    }

    Pair<Integer, Integer> cellIndexAt(int x, int y) {
        int startX = (int) (startX() - padding * 0.5f);
        int startY = (int) (startY() - padding * 0.5f);
        int rowIndex = (x - startX) / (cellWidth() + padding);
        int colIndex = (y - startY) / (cellWidth() + padding);
//        Log.d(TAG, "getCellIndexAt: (rowIndex, colIndex), (" + rowIndex + "," + colIndex + ")");


        return rowIndex >= 3 || rowIndex < 0 || colIndex >= 3 || colIndex < 0 ? null : new Pair<>(rowIndex, colIndex);
    }

    private class TapRecognizer extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
//            Log.d(TAG, "onSingleTapUp: onSingleTapUp");
            int touchX = (int) e.getX();
            int touchY = (int) e.getY();
//            Log.d(TAG, "onSingleTapUp: (x, y) " + touchX + " " + touchY);

            int rowX = (int) e.getRawX();
            int rowY = (int) e.getRawY();

//            Log.d(TAG, "onSingleTapUp: raw (x, y) " + rowX + " " + rowY);

            Pair<Integer,Integer> cur = cellIndexAt(touchX, touchY);

            if (cur != null && onSellTapListener != null) {
                onSellTapListener.onSellTapped(cur.first, cur.second);
            }

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return true;
        }


    }

}
