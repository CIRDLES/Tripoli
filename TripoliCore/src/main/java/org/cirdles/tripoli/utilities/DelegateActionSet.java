package org.cirdles.tripoli.utilities;

import java.util.*;

public class DelegateActionSet {
    private final Set<DelegateActionInterface> actionSet;

    public DelegateActionSet() {
        actionSet = new HashSet<>();
    }

    public DelegateActionSet(DelegateActionInterface... actionInterfaces) {
        this.actionSet = new HashSet<>();
        actionSet.addAll(Arrays.asList(actionInterfaces));
    }

    public boolean addDelegateAction(DelegateActionInterface actionInterface) {
        return this.actionSet.add(actionInterface);
    }

    public void executeDelegateActions() {
        for (DelegateActionInterface delegateActionInterface: actionSet) {
            delegateActionInterface.act();
        }
    }

    public boolean removeDelegateAction(DelegateActionInterface actionInterface) {
        return this.actionSet.remove(actionInterface);
    }

    public void clear() {
        this.actionSet.clear();
    }

}
