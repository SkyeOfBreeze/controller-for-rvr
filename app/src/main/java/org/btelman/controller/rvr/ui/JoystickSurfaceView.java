/*
 * Copyright (c) 2019 Brendon Telman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.btelman.controller.rvr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.btelman.controller.rvr.R;

public class JoystickSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    SurfaceHolder mSurfaceHolder;
    Paint paint;
    Paint joyPaint;
    Paint joyDebugPaint, joyDebugPaint2;
    int globalColor;
    float touched_x, touched_y;
    boolean touched = false;
    boolean debug = false;
    float xOutput = 0, yOutput = 0, xOffset = 0, yOffset = 0, xFinalOutput = 0, yFinalOutput = 0;
    int sleepTime = 10;
    private boolean hideView;

    public JoystickSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public JoystickSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JoystickSurfaceView(Context context) {
        super(context);
        init();
    }

    private void init() {
        //this.setZOrderOnTop(true);    // necessary
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        //mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        globalColor = Color.parseColor("#ffffff");
        paint = new Paint();
        joyPaint = new Paint();
        joyDebugPaint = new Paint();
        joyDebugPaint.setColor(Color.RED);
        joyDebugPaint2 = new Paint();
        joyDebugPaint2.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#000000"));
        joyPaint.setAntiAlias(true);
        //noinspection deprecation
        joyPaint.setColor(getResources().getColor(R.color.colorPrimary));
    }

    public void resume() {
        runnable = true;
    }

    public void pause() {
        runnable = false;
    }

    public void hide() {
        hideView = true;
        updateUI();
        pause();
    }

    public void show() {
        resume();
        hideView = false;
        updateUI();
    }

    float GLOBALoriginX = 0, GLOBALoriginY = 0;
    float radius = 0, minRadius = 50.0f;
    float origin_x, origin_y;
    float range;

    boolean runnable = true;
    private boolean touchOkay = true;

    private void updateUI() {
        if (runnable && !outside)
            try {
                if (mSurfaceHolder.getSurface().isValid()) {
                    Canvas canvas = mSurfaceHolder.lockCanvas(); //Start new rounds of drawing
                    GLOBALoriginX = canvas.getWidth() / 2;
                    GLOBALoriginY = canvas.getHeight() / 2;
                    if(GLOBALoriginX > GLOBALoriginY) {
                        minRadius = GLOBALoriginY/2.5f;
                        range = GLOBALoriginY-minRadius;
                    }
                    else if(GLOBALoriginY > GLOBALoriginX){
                        minRadius = GLOBALoriginX/2.5f;
                        range = GLOBALoriginX-minRadius;
                    }
                    radius = canvas.getWidth() / 2.5f;
                    origin_y = GLOBALoriginY;
                    origin_x = GLOBALoriginX;
                    if (hideView) {
                        canvas.drawColor(globalColor);
                    } else {
                        if(touched) {
                            xFinalOutput = (xOutput) - origin_x;
                            yFinalOutput = (yOutput) - origin_y;
                        }
                        else{
                            xFinalOutput = origin_x;
                            yFinalOutput = origin_y;
                        }
                        canvas.drawColor(globalColor);
                        canvas.drawCircle(origin_x, origin_y, radius, paint);

                        radialBounds(origin_x, origin_y); //Make sure joystick is within bounds
                        canvas.drawCircle(xOutput, yOutput, minRadius, joyPaint);

                        if (debug) {
                            canvas.drawText("X: " + (xFinalOutput), 20, 20,
                                    joyDebugPaint2);
                            canvas.drawText("Y: " + (yFinalOutput), 20, 40,
                                    joyDebugPaint2);
                            canvas.drawText("Value: " + (range), 20, 60,
                                    joyDebugPaint2);
                        }
                    }
                    mSurfaceHolder.unlockCanvasAndPost(canvas); //Done with drawing for this cycle

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
		 //updateUI();
    }

    private boolean FIRST = false;
    public void surfaceCreated(SurfaceHolder holder) {
        this.resume();
        //if(!FIRST){
        updateUI();
        //FIRST = true;
        //}
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.pause();
    }

    boolean outside = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchOkay = true;
        if (touchOkay) {
            touched_x = event.getX();
            touched_y = event.getY();
            int action = event.getAction();
            outside = false;
            switch (action) {
                case MotionEvent.ACTION_OUTSIDE:
                    touched = false;
                    outside = true;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    touched = false;
                    Log.i("I", "am not following rules!");
                    break;
                case MotionEvent.ACTION_DOWN:
                    touched = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    touched = true;
                    break;
                case MotionEvent.ACTION_UP:
                    touched = false;
                    touched_x = origin_x;
                    touched_y = origin_y;
                    updateUI();
                    break;
                default:
            }
            if (touched && !outside) {
                updateUI();
            }
        }
        return true; //processed
    }

    public void setThreadSleep(int sleepInt) {
        sleepTime = sleepInt;
    }

    public void setBackgroundColor(int color) {
        globalColor = color;
    }

    public void setDebugMode(boolean mode) {
        debug = mode;
    }

    public float[] getJoystickAxes() {
        float[] joystickAxes = new float[2];
        if(touched) {
            joystickAxes[0] = (map(range(xFinalOutput, -range, range), -range, range, -1f, 1f, 1.0f)); //range the value before remapping to make sure it stays in bounds
            joystickAxes[1] = (map(range(yFinalOutput, -range, range), -range, range, -1f, 1f, 1.0f)); //range the value before remapping to make sure it stays in bounds
        }
        return joystickAxes;
    }

    public enum Axis{
        X,
        Y
    }

    public void calibrateJoystickAxes() {
        xOffset = 291.0f;
        yOffset = 651.0f;
    }

    @Deprecated
    private void calculatePositionForJoystick(float originX, float originY) {
        float stickRange = range;//128;// /3;
        if (touched) {
            //Corners//////////////////////////////////////////////////////
            if (touched_x - originX > stickRange && touched_y - originY > stickRange) {   //Right Bottom Corner
                xOutput = originX + stickRange;
                yOutput = originY + stickRange;
            } else if (touched_x - originX > stickRange && touched_y < originY - stickRange) { //Right Top Corner
                xOutput = originX + stickRange;
                yOutput = originY - stickRange;
            } else if (touched_x < originX - stickRange && touched_y - originY > stickRange) { //Left Top Corner
                xOutput = originX - stickRange;
                yOutput = originY + stickRange;
            } else if (touched_x < originX - stickRange && touched_y < originY - stickRange) {   //Left Bottom Corner
                xOutput = originX - stickRange;
                yOutput = originY - stickRange;
            }
            //Sides////////////////////////////////////////////////////////////
            else if (touched_x - originX > stickRange) {
                xOutput = originX + stickRange;
                yOutput = touched_y;
            } else if (touched_y - originY > stickRange) {
                xOutput = touched_x;
                yOutput = originY + stickRange;
            } else if (touched_x < originX - stickRange) {
                xOutput = originX - stickRange;
                yOutput = touched_y;
            } else if (touched_y < originY - stickRange) {
                xOutput = touched_x;
                yOutput = originY - stickRange;
            } else {                            //For motions inside the bounds of joystick
                xOutput = touched_x;
                yOutput = touched_y;
            }
        } else {                                //For no touch
            xOutput = originX;
            yOutput = originY;
        }
    }

    private float scaleRadius(float x, float y){
        float joyRadius = radius;// / 1.5f;
        if(touched) {
            float xScaled = 0.0f, yScaled = 0.0f;
            float tmp = (range - Math.abs(x)) + minRadius;
            if (tmp <= joyRadius) {
                xScaled = tmp;
            }

            tmp = (range - Math.abs(y)) + minRadius;
            if (tmp <= joyRadius) {
                yScaled = tmp;
            }

            if (yScaled != 0.0f && xScaled != 0.0f) { //If both are not zero
                if (yScaled > xScaled) { //And if yScaled is more than xScaled
                    joyRadius = xScaled;
                } else { //yScaled less than xScaled
                    joyRadius = yScaled;
                }
            } else if (yScaled != 0) {
                joyRadius = yScaled;
            } else if (xScaled != 0) {
                joyRadius = xScaled;
            }
        }
        return joyRadius;
    }

    private void radialBounds(float originX, float originY){
        if (touched) {
            xOutput = range(touched_x, originX-range, originX+range);
            yOutput = range(touched_y, originY-range, originY+range);
            //TODO get distance from origin and range that, then convert it back to x y if needed. This will give circular bounds
        } else {                                //For no touch
            xOutput = originX;
            yOutput = originY;
        }
    }

    public float range(float input, float min, float max){
        float output;
        output = input;
        if(input < min){
            output = min;
        }
        else if(input > max){
            output = max;
        }
        return output;
    }

    public float map(float input, float inMin, float inMax, float outMin, float outMax, float multiplier){
        input = range(input, inMin, inMax);
        input *= multiplier;
        inMin *= multiplier;
        inMax *= multiplier;
        float output = ((input - inMin) / (inMax - inMin)) * (outMax - outMin) + outMin;
        //if()
        return range(output, outMin, outMax);
    }
}
