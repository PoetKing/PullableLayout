/**
 * 
 */
package com.poet.pullablelayout;

/**
 * @Description 
 * @author POET_WYD@FOXMAIL.COM
 * @date 2015-11-6
 */
public enum State {

    TEMP(-1), RESET(0), PULL_TO_RUN(1), RELEASE_TO_RUN(2), RUNNING(3);

    int intValue;
    Object tag;

    private State(int value) {
        this.intValue = value;
    }

    public void setTag(Object obj) {
        this.tag = obj;
    }

    public Object fetchTag() {
        Object obj = this.tag;
        this.tag = null;
        return obj;
    }
}
