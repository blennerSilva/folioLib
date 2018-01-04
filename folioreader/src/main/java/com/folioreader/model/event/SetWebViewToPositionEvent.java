package com.folioreader.model.event;

public class SetWebViewToPositionEvent {
    int position;

    public SetWebViewToPositionEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
