package com.folioreader.model.event;

/**
 * Created by blennersilva on 12/12/17.
 */

public class JumpToAnchorPoint {
    private String href;

    public JumpToAnchorPoint(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }
}
