package com.folioreader.model.event;

/**
 * Created by blennersilva on 28/12/17.
 */

public class GetTOCLinkWrapper {
    String href;

    public GetTOCLinkWrapper(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }
}
