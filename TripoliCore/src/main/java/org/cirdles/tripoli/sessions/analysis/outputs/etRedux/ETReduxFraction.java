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

package org.cirdles.tripoli.sessions.analysis.outputs.etRedux;

import com.thoughtworks.xstream.XStream;
import org.cirdles.tripoli.DataDictionary;
import org.cirdles.tripoli.utilities.xml.ETReduxFractionXMLConverter;
import org.cirdles.tripoli.utilities.xml.XMLSerializerInterface;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Defines and produces fractions for use with www.earth-time.org ET_Redux program.
 * Provides for recording the actual Tracer used to process the fraction, if any.
 * ET_Redux previously known as UPb_Redux.  This class ported from OG Tripoli.
 */

public class ETReduxFraction implements Comparable, Serializable, XMLSerializerInterface {
    private static String schemaURI = "https://raw.githubusercontent.com/EARTHTIME/Schema/UPbReduxInputXMLSchema.xsd";
    // Fields
    private String sampleName;
    private String fractionID;
    private String ratioType;
    private String pedigree;
    private MeasuredRatioModel[] measuredRatios;
    private double meanAlphaU;
    private double meanAlphaPb;
    private double r18O_16O;
    private double labUBlankMass;
    // april 2008
    private double r238_235b;
    private double r238_235s;
    private double tracerMass;

    public ETReduxFraction() {
        this("", "", "", 0.0);
    }

    private ETReduxFraction(String sampleName, String fractionID, String ratioType, double r18O_16O) {
        this.sampleName = sampleName;
        this.fractionID = fractionID;
        this.ratioType = ratioType;
        this.pedigree = "None";
        this.measuredRatios = new MeasuredRatioModel[0];
        this.meanAlphaU = 0.0;
        this.meanAlphaPb = 0.0;
        this.labUBlankMass = 0.0;
        this.r18O_16O = r18O_16O;
        this.r238_235b = 0.0;
        this.r238_235s = 0.0;
        this.tracerMass = 0.0;
    }

    public static ETReduxFraction buildExportFraction(String sampleName, String fractionID, String ratioType, double r18O_16O){
        ETReduxFraction etReduxFraction = new ETReduxFraction(sampleName, fractionID, ratioType, r18O_16O);

        // filter measured ratios
        // modified april 2010 to split "U" fractions from "Pb" fractions parts for LiveUpdate
        if (ratioType.compareToIgnoreCase("U") == 0) {
            etReduxFraction.measuredRatios = new MeasuredRatioModel[DataDictionary.etReduxUraniumMeasuredRatioNames.length];
        } else {
            etReduxFraction.measuredRatios = new MeasuredRatioModel[DataDictionary.etReduxLeadMeasuredRatioNames.length];
        }
        for (int i = 0; i < etReduxFraction.measuredRatios.length; i++) {
            etReduxFraction.measuredRatios[i] =
                    new MeasuredRatioModel(
                            (ratioType.compareTo("U")==0)?
                                    DataDictionary.etReduxUraniumMeasuredRatioNames[i]:
                                    DataDictionary.etReduxLeadMeasuredRatioNames[i], 0.0, 0.0, false, false);
        }

        return etReduxFraction;
    }

    public MeasuredRatioModel getMeasuredRatioByName(String myRatioName) {
        // NOV 2009 NOTE: Tripoli still uses no r and no m ... TODO: fix this!!
        // April 2024 - still true and also for new Tripoli
        String ratioName = myRatioName.trim();
        MeasuredRatioModel retval = null;

        // look for ratio
        for (int i = 0; i < measuredRatios.length; i++) {
            if (measuredRatios[i].getName().equalsIgnoreCase(ratioName)) {
                retval = measuredRatios[i];
            }
        }
        return retval;
    }

    public static String getSchemaURI() {
        return schemaURI;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getFractionID() {
        return fractionID;
    }

    public void setFractionID(String fractionID) {
        this.fractionID = fractionID;
    }

    public String getRatioType() {
        return ratioType;
    }

    public void setRatioType(String ratioType) {
        this.ratioType = ratioType;
    }

    public String getPedigree() {
        return pedigree;
    }

    public void setPedigree(String pedigree) {
        this.pedigree = pedigree;
    }

    public MeasuredRatioModel[] getMeasuredRatios() {
        return measuredRatios;
    }

    public void setMeasuredRatios(MeasuredRatioModel[] measuredRatios) {
        this.measuredRatios = measuredRatios;
    }

    public double getMeanAlphaU() {
        return meanAlphaU;
    }

    public void setMeanAlphaU(double meanAlphaU) {
        this.meanAlphaU = meanAlphaU;
    }

    public double getMeanAlphaPb() {
        return meanAlphaPb;
    }

    public void setMeanAlphaPb(double meanAlphaPb) {
        this.meanAlphaPb = meanAlphaPb;
    }

    public double getR18O_16O() {
        return r18O_16O;
    }

    public void setR18O_16O(double r18O_16O) {
        this.r18O_16O = r18O_16O;
    }

    public double getLabUBlankMass() {
        return labUBlankMass;
    }

    public void setLabUBlankMass(double labUBlankMass) {
        this.labUBlankMass = labUBlankMass;
    }

    public double getR238_235b() {
        return r238_235b;
    }

    public void setR238_235b(double r238_235b) {
        this.r238_235b = r238_235b;
    }

    public double getR238_235s() {
        return r238_235s;
    }

    public void setR238_235s(double r238_235s) {
        this.r238_235s = r238_235s;
    }

    public double getTracerMass() {
        return tracerMass;
    }

    public void setTracerMass(double tracerMass) {
        this.tracerMass = tracerMass;
    }

    /**
     * @param xstream
     */
    @Override
    public void customizeXstream(XStream xstream) {
        xstream.registerConverter(new ETReduxFractionXMLConverter());
        xstream.alias("UPbReduxFraction", ETReduxFraction.class);
        xstream.alias("MeasuredRatioModel", MeasuredRatioModel.class);
    }

    /**
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof ETReduxFraction) {
            return (sampleName + fractionID + ratioType)
                    .compareTo(((ETReduxFraction) o).sampleName + ((ETReduxFraction) o).fractionID + ((ETReduxFraction) o).ratioType);
        } else return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        ETReduxFraction that = (ETReduxFraction) o;
        return 0 == Double.compare(r18O_16O, that.r18O_16O)
                && Objects.equals(sampleName, that.sampleName)
                && Objects.equals(fractionID, that.fractionID)
                && Objects.equals(ratioType, that.ratioType)
                && Objects.equals(pedigree, that.pedigree)
                && Arrays.equals(measuredRatios, that.measuredRatios);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sampleName, fractionID, ratioType, pedigree, r18O_16O);
        result = 31 * result + Arrays.hashCode(measuredRatios);
        return result;
    }
}
