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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups;

import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class Detector implements Serializable {

    public enum DetectorTypeEnum{
        FARADAY("FARADAY"),
        ION_COUNTER("ION_COUNTER");

        final String name;

        DetectorTypeEnum(String name) {
            this.name = name;
        }
    }

    private DetectorTypeEnum detectorType;

    private Detector() {
        this(DetectorTypeEnum.FARADAY);
    }

    private Detector(DetectorTypeEnum detectorType) {
        this.detectorType = detectorType;
    }

    static Detector createDetector(){
        Detector detector = new Detector();
        return detector;
    }

    public DetectorTypeEnum getDetectorType() {
        return detectorType;
    }

    public void setDetectorType(DetectorTypeEnum detectorType) {
        this.detectorType = detectorType;
    }
}