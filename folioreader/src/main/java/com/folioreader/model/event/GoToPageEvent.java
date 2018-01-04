package com.folioreader.model.event;

/**
 * Created by blennersilva on 29/12/17.
 */

public class GoToPageEvent {
    int pageNumber;

    public GoToPageEvent(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
