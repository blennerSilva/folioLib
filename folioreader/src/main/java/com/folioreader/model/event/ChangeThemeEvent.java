package com.folioreader.model.event;

/**
 * Created by blennersilva on 11/12/17.
 */

public class ChangeThemeEvent {
    public enum Theme{
        DAY_THEME,
        NIGHT_THEME
    }

    private Theme theme;

    public ChangeThemeEvent(Theme theme) {
        this.theme = theme;
    }

    public Theme getTheme() {
        return theme;
    }
}
