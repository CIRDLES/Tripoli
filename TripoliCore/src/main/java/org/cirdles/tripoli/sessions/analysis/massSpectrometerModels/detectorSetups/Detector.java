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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class Detector implements Comparable, Serializable {

    private final DetectorTypeEnum detectorType;
    private final String detectorName;
    // 0-based index of detector order, used for reading data column
    private final int ordinalIndex;
    private AmplifierTypeEnum amplifierType;
    private double amplifierResistanceInOhms;
    private double amplifierGain;
    private double amplifierEfficiency;
    private double detectorDeadTime;

    public Detector(DetectorTypeEnum detectorType, String detectorName, int ordinalIndex,
                    AmplifierTypeEnum amplifierType, double amplifierResistanceInOhms,
                    double amplifierGain, double amplifierEfficiency, double detectorDeadTime) {
        this.detectorType = detectorType;
        this.detectorName = detectorName;
        this.ordinalIndex = ordinalIndex;
        this.amplifierType = amplifierType;
        this.amplifierResistanceInOhms = amplifierResistanceInOhms;
        this.amplifierGain = amplifierGain;
        this.amplifierEfficiency = amplifierEfficiency;
        this.detectorDeadTime = detectorDeadTime;
    }

    public static Detector initializeDetector(DetectorTypeEnum detectorType, String detectorName, int ordinalIndex,
                                              AmplifierTypeEnum amplifierType, double amplifierResistanceInOhms,
                                              double amplifierGain, double amplifierEfficiency, double detectorDeadTime) {
        return new Detector(detectorType, detectorName, ordinalIndex, amplifierType, amplifierResistanceInOhms,
                amplifierGain, amplifierEfficiency, detectorDeadTime);
    }

    public boolean isFaraday() {
        return detectorType == DetectorTypeEnum.FARADAY;
    }

    public String getDetectorName() {
        return detectorName;
    }

    public int getOrdinalIndex() {
        return ordinalIndex;
    }

    public DetectorTypeEnum getDetectorType() {
        return detectorType;
    }

    public AmplifierTypeEnum getAmplifierType() {
        return amplifierType;
    }

    public void setAmplifierType(AmplifierTypeEnum amplifierType) {
        this.amplifierType = amplifierType;
    }

    public double getAmplifierResistanceInOhms() {
        return amplifierResistanceInOhms;
    }

    public void setAmplifierResistanceInOhms(double amplifierResistanceInOhms) {
        this.amplifierResistanceInOhms = amplifierResistanceInOhms;
    }

    public double getAmplifierGain() {
        return amplifierGain;
    }

    public void setAmplifierGain(double amplifierGain) {
        this.amplifierGain = amplifierGain;
    }

    public double getAmplifierEfficiency() {
        return amplifierEfficiency;
    }

    public void setAmplifierEfficiency(double amplifierEfficiency) {
        this.amplifierEfficiency = amplifierEfficiency;
    }

    public double getDetectorDeadTime() {
        return detectorDeadTime;
    }

    public void setDetectorDeadTime(double detectorDeadTime) {
        this.detectorDeadTime = detectorDeadTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        Detector detector = (Detector) o;
        return detectorName.equals(detector.detectorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(detectorName);
    }

    @Override
    public int compareTo(@NotNull Object object) throws ClassCastException {
        int ordinal = ((Detector) object).getOrdinalIndex();
        return Integer.compare(this.getOrdinalIndex(), ordinal);
    }

    /**
     * see https://docs.google.com/drawings/d/1CBmyaqkA3ZdnztONat0z4oEcJWapjdpFx3xUwltyS4A/edit
     */
    public enum DetectorTypeEnum {
        FARADAY("Faraday", "Faraday Collector, Faraday Detector"),
        DALYDETECTOR("DalyDetector", "Ion Counter, Daly, PhotoMultiplier, PM"),
        SEM("SEM", "Ion Counter, Secondary Electron Multiplier, Multiplier, EM"),
        CDD("CDD", "Ion Counter, Compact Discrete Dynode Detector");


        public static Map<String, Detector.DetectorTypeEnum> mapOfNamesToDetectorType = new TreeMap<>();

        static {
            mapOfNamesToDetectorType.put("F", FARADAY);
            mapOfNamesToDetectorType.put("PM", DALYDETECTOR);
            mapOfNamesToDetectorType.put("EM", SEM);
        }

        final String name;
        final String otherNames;

        DetectorTypeEnum(String name, String otherNames) {
            this.name = name;
            this.otherNames = otherNames;
        }

        public String getName() {
            return name;
        }

        public DetectorTypeEnum lookupCode(String code) {
            return mapOfNamesToDetectorType.get(code);
        }
    }

    public enum AmplifierTypeEnum {
        RESISTANCE("Resistance"),
        CAPACITANCE("Capacitance"), // aka ATONA
        VIRTUAL("VIRTUAL");

        public static Map<String, Detector.AmplifierTypeEnum> mapOfDetectorTypetoAmplifierType = new TreeMap<>();

        static {
            mapOfDetectorTypetoAmplifierType.put("F", RESISTANCE);
            mapOfDetectorTypetoAmplifierType.put("PM", VIRTUAL);
            mapOfDetectorTypetoAmplifierType.put("EM", VIRTUAL);
        }

        final String name;

        AmplifierTypeEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}