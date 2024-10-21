package org.cirdles.tripoli.expressions.userFunctions;

import java.io.Serializable;

public class UserFunctionDisplay implements Serializable {
    //    @Serial
//    private static final long serialVersionUID = -5408855769497340457L;
    private String name;
    private boolean displayed;
    private boolean inverted;

    public UserFunctionDisplay(String name, boolean displayed, boolean inverted) {
        this.name = name;
        this.displayed = displayed;
        this.inverted = inverted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
}
