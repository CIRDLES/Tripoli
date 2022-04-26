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

    private DetectorTypeEnum detectorType;
    private String detectorName;

    private Detector() {
    }

    private Detector(DetectorTypeEnum detectorType, String detectorName) {
        this.detectorType = detectorType;
        this.detectorName =detectorName;
    }

    public static Detector initializeDetector(DetectorTypeEnum detectorType, String detectorName) {
        return new Detector(detectorType, detectorName);
    }

    public boolean isFaraday(){
        return detectorType.equals(DetectorTypeEnum.FARADAY);
    }

    public DetectorTypeEnum getDetectorType() {
        return detectorType;
    }

    public void setDetectorType(DetectorTypeEnum detectorType) {
        this.detectorType = detectorType;
    }

    public String getDetectorName() {
        return detectorName;
    }

    public void setDetectorName(String detectorName) {
        this.detectorName = detectorName;
    }

    public enum DetectorTypeEnum {
        FARADAY("FARADAY"),
        DALY("DALY"),
        SEM("SEM"),
        CDD("CDD");

        final String name;

        DetectorTypeEnum(String name) {
            this.name = name;
        }
    }
}