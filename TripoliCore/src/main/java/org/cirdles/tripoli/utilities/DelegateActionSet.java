/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
