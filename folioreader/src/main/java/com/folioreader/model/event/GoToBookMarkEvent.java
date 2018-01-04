package com.folioreader.model.event;

/**
 * Created by blennersilva on 29/12/17.
 */

public class GoToBookMarkEvent {
    private String chapterPosition;
    private int pageNumber;

    public GoToBookMarkEvent(String chapterPosition, int pageNumber) {
        this.chapterPosition = chapterPosition;
        this.pageNumber = pageNumber;
    }

    public String getChapterPosition() {
        return chapterPosition;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
