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

package org.cirdles.tripoli.parameters;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.Serializable;

import static org.cirdles.tripoli.constants.TripoliConstants.*;

public class Parameters implements Serializable {

    private static final long serialVersionUID = 4300973848877908846L;

    // Chauvenet's parameters
    private double chauvenetRejectionProbability;
    private int requiredMinDatumCount;
    private MassSpectrometerContextEnum massSpectrometerContext;
    
    // Scaling dot size parameters
    private double scalingDotMinSize;
    private double scalingDotMaxSize;

    public Parameters() {
        this.chauvenetRejectionProbability = CHAUVENETS_DEFAULT_REJECT_PROBABILITY;
        this.requiredMinDatumCount = CHAUVENETS_DEFAULT_MIN_DATUM_COUNT;
        this.massSpectrometerContext = MassSpectrometerContextEnum.UNKNOWN;
        this.scalingDotMinSize = SCALING_DOT_DEFAULT_MIN_SIZE;
        this.scalingDotMaxSize = SCALING_DOT_DEFAULT_MAX_SIZE;
    }

    // Copy Constructor
    public Parameters(Parameters other) {
        this.chauvenetRejectionProbability = other.getChauvenetRejectionProbability();
        this.requiredMinDatumCount = other.getRequiredMinDatumCount();
        this.massSpectrometerContext = other.massSpectrometerContext;
        this.scalingDotMinSize = other.getScalingDotMinSize();
        this.scalingDotMaxSize = other.getScalingDotMaxSize();
    }

    // Provides a deep copy of this instance
    public Parameters copy() {
        return new Parameters(this);
    }

    public double getChauvenetRejectionProbability() {
        return chauvenetRejectionProbability;
    }

    public void setChauvenetRejectionProbability(double chauvenetRejectionProbability) {
        this.chauvenetRejectionProbability = chauvenetRejectionProbability;
    }

    public int getRequiredMinDatumCount() {
        return requiredMinDatumCount;
    }

    public void setRequiredMinDatumCount(int requiredMinDatumCount) {
        this.requiredMinDatumCount = requiredMinDatumCount;
    }
    public MassSpectrometerContextEnum getMassSpectrometerContext() {
        return massSpectrometerContext;
    }
    public void setMassSpectrometerContext(MassSpectrometerContextEnum massSpectrometerContext) {
        this.massSpectrometerContext = massSpectrometerContext;
    }

    public double getScalingDotMinSize() {
        return scalingDotMinSize;
    }

    public void setScalingDotMinSize(double scalingDotMinSize) {
        this.scalingDotMinSize = scalingDotMinSize;
    }

    public double getScalingDotMaxSize() {
        return scalingDotMaxSize;
    }

    public void setScalingDotMaxSize(double scalingDotMaxSize) {
        this.scalingDotMaxSize = scalingDotMaxSize;
    }
}
