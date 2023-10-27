package org.cirdles.tripoli.gui;

public interface AnalysisManagerCallbackI {
    public void callbackRefreshBlocksStatus();

    public void reviewAndSculptDataAction();

    public void callBackSetBlockIncludedStatus(int blockID, boolean status);
}