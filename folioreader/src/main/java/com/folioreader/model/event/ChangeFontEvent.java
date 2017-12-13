package com.folioreader.model.event;

/**
 * Created by blennersilva on 11/12/17.
 */

public class ChangeFontEvent {

    private int fontSize;

    public ChangeFontEvent(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getFontSize() {
        return fontSize;
    }
}
