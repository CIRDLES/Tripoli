package org.cirdles.tripoli.gui;

public interface AnalysisManagerCallbackI {
    void callbackRefreshBlocksStatus();

    void reviewAndSculptDataAction();

    void callBackSetBlockIncludedStatus(int blockID, boolean status);
}