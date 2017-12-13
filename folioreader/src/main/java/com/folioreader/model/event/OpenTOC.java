package com.folioreader.model.event;

/**
 * Created by blennersilva on 11/12/17.
 */

public class OpenTOC {
    Class aClass;

    public OpenTOC(Class aClass) {
        this.aClass = aClass;
    }

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }
}
