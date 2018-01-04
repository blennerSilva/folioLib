package com.folioreader;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by blennersilva on 04/01/18.
 */

public class TouchDetector implements View.OnTouchListener {

    private int minDistance = 100;
    private int sideOffset = 30;
    private int centerOffset = 40;
    private float downX, downY, upX, upY;
    private View v;

    private OnTouchEventListener listener;


    public TouchDetector(View v, boolean addTouchListener) {
        this.v = v;

        if (addTouchListener) {
            v.setOnTouchListener(this);
        }
    }

    public TouchDetector(View v) {
        this(v, true);
    }

    public void setListener(OnTouchEventListener listener) {
        this.listener = listener;
    }

    private void onRightToLeftSwipe() {
        if (listener != null) {
            listener.onTouchEventDetected(v, TouchTypeEnum.RIGHT_TO_LEFT);
        }
    }

    private void onTapLeft() {
        if (listener != null) {
            listener.onTouchEventDetected(v, TouchTypeEnum.TAP_LEFT);
        }
    }

    private void onTapRight() {
        if (listener != null) {
            listener.onTouchEventDetected(v, TouchTypeEnum.TAP_RIGHT);
        }
    }

    private void onTapCenter() {
        if (listener != null) {
            listener.onTouchEventDetected(v, TouchTypeEnum.TAP_CENTER);
        }
    }

    private void onLeftToRightSwipe() {
        if (listener != null) {
            listener.onTouchEventDetected(v, TouchTypeEnum.LEFT_TO_RIGHT);
        }
    }

    private void onTopToBottomSwipe() {
        if (listener != null) {
            listener.onTouchEventDetected(v, TouchTypeEnum.TOP_TO_BOTTOM);
        }
    }

    private void onBottomToTopSwipe() {
        if (listener != null) {
            listener.onTouchEventDetected(v, TouchTypeEnum.BOTTOM_TO_TOP);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case (MotionEvent.ACTION_MOVE):
                if (downY - event.getY() > 20 || downY - event.getY() < -20)
                    return true;
                else

                    break;

            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // HORIZONTAL SCROLL

                    if (Math.abs(deltaX) > minDistance) {
                        // left or right
                        if (deltaX < 0) {
                            this.onLeftToRightSwipe();
                            return true;
                        }

                        if (deltaX > 0) {
                            this.onRightToLeftSwipe();
                            return true;
                        }
                    } else {
                        return onTap(v, event);
                    }
                } else {
                    // VERTICAL SCROLL

                    if (Math.abs(deltaY) > minDistance) {
                        // top or down
                        if (deltaY < 0) {
                            this.onTopToBottomSwipe();
                            return true;
                        }

                        if (deltaY > 0) {
                            this.onBottomToTopSwipe();
                            return true;
                        }
                    } else {
                        return onTap(v, event);
                    }
                }

                return true;
            }
        }

        return false;
    }

    private boolean onTap(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float viewWidth = v.getWidth();

            float screenPartSide = ((viewWidth * sideOffset) / 100);
            float screenPartCenter = ((viewWidth * centerOffset) / 100);

            if (x <= screenPartSide) {
                this.onTapLeft();
                return true;
            } else if (x >= (screenPartSide + screenPartCenter)) {
                this.onTapRight();
                return true;
            } else {
                this.onTapCenter();
                return true;
            }
        }

        return false;
    }

    public interface OnTouchEventListener {
        void onTouchEventDetected(View v, TouchTypeEnum touchType);
    }

    public enum TouchTypeEnum {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP,
        TAP_LEFT,
        TAP_RIGHT,
        TAP_CENTER
    }

}