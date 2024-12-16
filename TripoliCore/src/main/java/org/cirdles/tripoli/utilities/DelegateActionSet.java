package org.cirdles.tripoli.utilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    public void addDelegateActions(DelegateActionInterface... actionInterfaces) {
        this.actionSet.addAll(Arrays.asList(actionInterfaces));
    }

    public void addDelegateActions(DelegateActionSet actionSet) {
        this.actionSet.addAll(actionSet.actionSet);
    }

    public void executeDelegateActions() {
        for (DelegateActionInterface delegateActionInterface : actionSet) {
            delegateActionInterface.act();
        }
    }

    public boolean removeDelegateAction(DelegateActionInterface actionInterface) {
        return this.actionSet.remove(actionInterface);
    }

    public void clear() {
        this.actionSet.clear();
    }

    public int size() { return this.actionSet.size(); }

    public boolean isEmpty() { return this.actionSet.isEmpty(); }

}
