package com.folioreader.model.event;

/**
 * Created by blennersilva on 04/01/18.
 */

public class JumpToBookmarkPage {
    private String chapterPosition;
    private int pageNumber;

    public JumpToBookmarkPage(String chapterPosition, int pageNumber) {
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
