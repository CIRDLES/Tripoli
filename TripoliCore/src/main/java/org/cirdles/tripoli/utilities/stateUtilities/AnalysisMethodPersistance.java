package org.cirdles.tripoli.utilities.stateUtilities;

import org.cirdles.tripoli.expressions.userFunctions.UserFunctionDisplay;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class AnalysisMethodPersistance implements Serializable {
    private static final long serialVersionUID = -3839100871560617989L;
//        private void readObject(ObjectInputStream stream) throws IOException,
//            ClassNotFoundException {
//        stream.defaultReadObject();
//
//        ObjectStreamClass myObject = ObjectStreamClass.lookup(
//                Class.forName(AnalysisMethodPersistance.class.getCanonicalName()));
//        long theSUID = myObject.getSerialVersionUID();
//
//        System.out.println("Customized De-serialization of AnalysisMethodPersistance "
//                + theSUID);
//    }
    private int cyclesPerBlock;
    private Map<String, UserFunctionDisplay> userFunctionDisplay;

    public AnalysisMethodPersistance(int cyclesPerBlock) {
        this.cyclesPerBlock = cyclesPerBlock;
    }

    public int getCyclesPerBlock() {
        return cyclesPerBlock;
    }

    public void setCyclesPerBlock(int cyclesPerBlock) {
        this.cyclesPerBlock = cyclesPerBlock;
    }

    public Map<String, UserFunctionDisplay> getUserFunctionDisplayMap() {
        if( userFunctionDisplay == null){
            userFunctionDisplay = new TreeMap<>();
        }
        return userFunctionDisplay;
    }

    public void setUserFunctionDisplayMap(Map<String, UserFunctionDisplay> userFunctionDisplay) {
        this.userFunctionDisplay = userFunctionDisplay;
    }
}
