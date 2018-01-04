package com.folioreader.model.event;

/**
 * Created by blennersilva on 11/12/17.
 */

public class GoToChapterEvent {

    private  String selectedChapterPosition;
    private  String bookTitle;
    private  String chapterSelected;

    public GoToChapterEvent(String selectedChapterPosition) {
        this.selectedChapterPosition = selectedChapterPosition;
    }

    public GoToChapterEvent(String selectedChapterPosition, String bookTitle, String chapterSelected) {
        this.selectedChapterPosition = selectedChapterPosition;
        this.bookTitle = bookTitle;
        this.chapterSelected = chapterSelected;
    }

    public String getSelectedChapterPosition() {
        return selectedChapterPosition;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getChapterSelected() {
        return chapterSelected;
    }
}
