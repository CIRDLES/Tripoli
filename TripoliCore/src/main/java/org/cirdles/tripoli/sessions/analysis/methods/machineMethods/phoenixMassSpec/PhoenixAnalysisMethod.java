package org.cirdles.tripoli.sessions.analysis.methods.machineMethods.phoenixMassSpec;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="HEADER"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Filename" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="DateModified" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="DateCreated" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="CreatedBy" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="ModifiedBy" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="SETTINGS"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="IntersperseBaselines" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="BaselineFrequency" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="PKCCycleInterval" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TotalBlocks" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TotalCycles" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="CalculateRatios" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="InitialBLMagnetDelay" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="DeflectBeamProtection" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="PerformBeamInterpCycles" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="PerformInterBlocks" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="AxialColl" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="ActiveSEMCollectors" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="MagnetFlybackSettleTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="CorrectForBaselines" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="CorrectForDeadtimes" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="IsoWorks_Method" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Rejection_Sigma" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Rejection_Percentage" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="ActiveSubChannel" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="ProcessingUnits" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TFE_Enabled" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TFE_MonitorPeak" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TFE_IntensityThreshold" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TFE_IntervalPeakCentring" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TFE_PeakCentringInterval" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TFE_PeakCentringMonitorThreshold" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Export_IsotopePoints" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Export_ASCFile" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Export_ASCOptions" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Export_ASCActiveColl" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Export_TXTEveryCYCLE" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Export_TripoliLiveData" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="BASELINE"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Sequence" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Enabled" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="MassID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="AxMass" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="IntegPeriod" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="IntegTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="PKCParentID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="BLReferences" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="MagnetSettleTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="AxMassOffset" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="CurveFit" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TraceColour" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="ONPEAK" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Sequence"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="1"/&gt;
 *                         &lt;enumeration value="2"/&gt;
 *                         &lt;enumeration value="3"/&gt;
 *                         &lt;enumeration value="4"/&gt;
 *                         &lt;enumeration value="5"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="Enabled" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="MassID"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="204Pb"/&gt;
 *                         &lt;enumeration value="205Pb"/&gt;
 *                         &lt;enumeration value="206Pb"/&gt;
 *                         &lt;enumeration value="207Pb"/&gt;
 *                         &lt;enumeration value="208Pb"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="MassIDCollector" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="AxMass"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="204.000"/&gt;
 *                         &lt;enumeration value="205.000"/&gt;
 *                         &lt;enumeration value="206.000"/&gt;
 *                         &lt;enumeration value="207.000"/&gt;
 *                         &lt;enumeration value="208.000"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="IntegPeriod" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="IntegTime"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="10.0"/&gt;
 *                         &lt;enumeration value="5.0"/&gt;
 *                         &lt;enumeration value="2.0"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="PeakCentre"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="False"/&gt;
 *                         &lt;enumeration value="True"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="PKCCollector" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="PKCMethod"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="None"/&gt;
 *                         &lt;enumeration value="PeakScanning"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="PKCParentID"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="205Pb:PMS2"/&gt;
 *                         &lt;enumeration value=""/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="BLReferences" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="CollectorArray"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="204Pb:PMS1"/&gt;
 *                         &lt;enumeration value="205Pb:PMS2"/&gt;
 *                         &lt;enumeration value="206Pb:PMS3"/&gt;
 *                         &lt;enumeration value="207Pb:PMS4"/&gt;
 *                         &lt;enumeration value="208Pb:PMS5"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="MagnetSettleTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="AxMassOffset"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="-0.1000"/&gt;
 *                         &lt;enumeration value="0.0000"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="CurveFit" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="TraceColour"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="Tomato"/&gt;
 *                         &lt;enumeration value="Violet"/&gt;
 *                         &lt;enumeration value="YellowGreen"/&gt;
 *                         &lt;enumeration value="Black"/&gt;
 *                         &lt;enumeration value="LightSeaGreen"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="EQUILIBRATION"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Enabled" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Collector" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="AxMass" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="MassID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="IntegPeriod" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="StoreToDB" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "header",
        "settings",
        "baseline",
        "onpeak",
        "equilibration"
})
@XmlRootElement(name = "ANALYSIS_METHOD")
public class PhoenixAnalysisMethod {

    @XmlElement(name = "HEADER", required = true)
    protected PhoenixAnalysisMethod.HEADER header;
    @XmlElement(name = "SETTINGS", required = true)
    protected PhoenixAnalysisMethod.SETTINGS settings;
    @XmlElement(name = "BASELINE", required = true)
    protected List<PhoenixAnalysisMethod.BASELINE> baseline;
    @XmlElement(name = "ONPEAK")
    protected List<PhoenixAnalysisMethod.ONPEAK> onpeak;
    @XmlElement(name = "EQUILIBRATION", required = true)
    protected PhoenixAnalysisMethod.EQUILIBRATION equilibration;

    /**
     * Gets the value of the header property.
     *
     * @return possible object is
     * {@link PhoenixAnalysisMethod.HEADER }
     */
    public PhoenixAnalysisMethod.HEADER getHEADER() {
        return header;
    }

    /**
     * Sets the value of the header property.
     *
     * @param value allowed object is
     *              {@link PhoenixAnalysisMethod.HEADER }
     */
    public void setHEADER(PhoenixAnalysisMethod.HEADER value) {
        header = value;
    }

    /**
     * Gets the value of the settings property.
     *
     * @return possible object is
     * {@link PhoenixAnalysisMethod.SETTINGS }
     */
    public PhoenixAnalysisMethod.SETTINGS getSETTINGS() {
        return settings;
    }

    /**
     * Sets the value of the settings property.
     *
     * @param value allowed object is
     *              {@link PhoenixAnalysisMethod.SETTINGS }
     */
    public void setSETTINGS(PhoenixAnalysisMethod.SETTINGS value) {
        settings = value;
    }

    /**
     * Gets the value of the baseline property.
     *
     * @return possible object is
     * {@link PhoenixAnalysisMethod.BASELINE }
     */
    public List<PhoenixAnalysisMethod.BASELINE> getBASELINE() {
        if (null == baseline) {
            baseline = new ArrayList<PhoenixAnalysisMethod.BASELINE>();
        }
        return baseline;
    }

    /**
     * Gets the value of the onpeak property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the onpeak property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getONPEAK().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PhoenixAnalysisMethod.ONPEAK }
     */
    public List<PhoenixAnalysisMethod.ONPEAK> getONPEAK() {
        if (null == onpeak) {
            onpeak = new ArrayList<PhoenixAnalysisMethod.ONPEAK>();
        }
        return onpeak;
    }

    /**
     * Gets the value of the equilibration property.
     *
     * @return possible object is
     * {@link PhoenixAnalysisMethod.EQUILIBRATION }
     */
    public PhoenixAnalysisMethod.EQUILIBRATION getEQUILIBRATION() {
        return equilibration;
    }

    /**
     * Sets the value of the equilibration property.
     *
     * @param value allowed object is
     *              {@link PhoenixAnalysisMethod.EQUILIBRATION }
     */
    public void setEQUILIBRATION(PhoenixAnalysisMethod.EQUILIBRATION value) {
        equilibration = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Sequence" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Enabled" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="MassID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="AxMass" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="IntegPeriod" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="IntegTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="PKCParentID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="BLReferences" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="MagnetSettleTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="AxMassOffset" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="CurveFit" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TraceColour" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "sequence",
            "enabled",
            "massID",
            "axMass",
            "integPeriod",
            "integTime",
            "pkcParentID",
            "blReferences",
            "magnetSettleTime",
            "axMassOffset",
            "curveFit",
            "traceColour"
    })
    public static class BASELINE {

        @XmlElement(name = "Sequence", required = true)
        protected String sequence;
        @XmlElement(name = "Enabled", required = true)
        protected String enabled;
        @XmlElement(name = "MassID", required = true)
        protected String massID;
        @XmlElement(name = "AxMass", required = true)
        protected String axMass;
        @XmlElement(name = "IntegPeriod", required = true)
        protected String integPeriod;
        @XmlElement(name = "IntegTime", required = true)
        protected String integTime;
        @XmlElement(name = "PKCParentID", required = true)
        protected String pkcParentID;
        @XmlElement(name = "BLReferences", required = true)
        protected String blReferences;
        @XmlElement(name = "MagnetSettleTime", required = true)
        protected String magnetSettleTime;
        @XmlElement(name = "AxMassOffset", required = true)
        protected String axMassOffset;
        @XmlElement(name = "CurveFit", required = true)
        protected String curveFit;
        @XmlElement(name = "TraceColour", required = true)
        protected String traceColour;

        /**
         * Gets the value of the sequence property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getSequence() {
            return sequence;
        }

        /**
         * Sets the value of the sequence property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setSequence(String value) {
            sequence = value;
        }

        /**
         * Gets the value of the enabled property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getEnabled() {
            return enabled;
        }

        /**
         * Sets the value of the enabled property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setEnabled(String value) {
            enabled = value;
        }

        /**
         * Gets the value of the massID property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getMassID() {
            return massID;
        }

        /**
         * Sets the value of the massID property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setMassID(String value) {
            massID = value;
        }

        /**
         * Gets the value of the axMass property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getAxMass() {
            return axMass;
        }

        /**
         * Sets the value of the axMass property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setAxMass(String value) {
            axMass = value;
        }

        /**
         * Gets the value of the integPeriod property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getIntegPeriod() {
            return integPeriod;
        }

        /**
         * Sets the value of the integPeriod property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIntegPeriod(String value) {
            integPeriod = value;
        }

        /**
         * Gets the value of the integTime property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getIntegTime() {
            return integTime;
        }

        /**
         * Sets the value of the integTime property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIntegTime(String value) {
            integTime = value;
        }

        /**
         * Gets the value of the pkcParentID property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getPKCParentID() {
            return pkcParentID;
        }

        /**
         * Sets the value of the pkcParentID property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setPKCParentID(String value) {
            pkcParentID = value;
        }

        /**
         * Gets the value of the blReferences property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getBLReferences() {
            return blReferences;
        }

        /**
         * Sets the value of the blReferences property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setBLReferences(String value) {
            blReferences = value;
        }

        /**
         * Gets the value of the magnetSettleTime property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getMagnetSettleTime() {
            return magnetSettleTime;
        }

        /**
         * Sets the value of the magnetSettleTime property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setMagnetSettleTime(String value) {
            magnetSettleTime = value;
        }

        /**
         * Gets the value of the axMassOffset property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getAxMassOffset() {
            return axMassOffset;
        }

        /**
         * Sets the value of the axMassOffset property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setAxMassOffset(String value) {
            axMassOffset = value;
        }

        /**
         * Gets the value of the curveFit property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCurveFit() {
            return curveFit;
        }

        /**
         * Sets the value of the curveFit property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCurveFit(String value) {
            curveFit = value;
        }

        /**
         * Gets the value of the traceColour property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTraceColour() {
            return traceColour;
        }

        /**
         * Sets the value of the traceColour property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTraceColour(String value) {
            traceColour = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Enabled" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Collector" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="AxMass" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="MassID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="IntegPeriod" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="StoreToDB" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "enabled",
            "collector",
            "axMass",
            "massID",
            "duration",
            "integPeriod",
            "storeToDB"
    })
    public static class EQUILIBRATION {

        @XmlElement(name = "Enabled", required = true)
        protected String enabled;
        @XmlElement(name = "Collector", required = true)
        protected String collector;
        @XmlElement(name = "AxMass", required = true)
        protected String axMass;
        @XmlElement(name = "MassID", required = true)
        protected String massID;
        @XmlElement(name = "Duration", required = true)
        protected String duration;
        @XmlElement(name = "IntegPeriod", required = true)
        protected String integPeriod;
        @XmlElement(name = "StoreToDB", required = true)
        protected String storeToDB;

        /**
         * Gets the value of the enabled property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getEnabled() {
            return enabled;
        }

        /**
         * Sets the value of the enabled property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setEnabled(String value) {
            enabled = value;
        }

        /**
         * Gets the value of the collector property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCollector() {
            return collector;
        }

        /**
         * Sets the value of the collector property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCollector(String value) {
            collector = value;
        }

        /**
         * Gets the value of the axMass property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getAxMass() {
            return axMass;
        }

        /**
         * Sets the value of the axMass property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setAxMass(String value) {
            axMass = value;
        }

        /**
         * Gets the value of the massID property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getMassID() {
            return massID;
        }

        /**
         * Sets the value of the massID property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setMassID(String value) {
            massID = value;
        }

        /**
         * Gets the value of the duration property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getDuration() {
            return duration;
        }

        /**
         * Sets the value of the duration property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setDuration(String value) {
            duration = value;
        }

        /**
         * Gets the value of the integPeriod property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getIntegPeriod() {
            return integPeriod;
        }

        /**
         * Sets the value of the integPeriod property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIntegPeriod(String value) {
            integPeriod = value;
        }

        /**
         * Gets the value of the storeToDB property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getStoreToDB() {
            return storeToDB;
        }

        /**
         * Sets the value of the storeToDB property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setStoreToDB(String value) {
            storeToDB = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Filename" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="DateModified" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="DateCreated" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="CreatedBy" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="ModifiedBy" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "filename",
            "dateModified",
            "dateCreated",
            "createdBy",
            "modifiedBy"
    })
    public static class HEADER {

        @XmlElement(name = "Filename", required = true)
        protected String filename;
        @XmlElement(name = "DateModified", required = true)
        protected String dateModified;
        @XmlElement(name = "DateCreated", required = true)
        protected String dateCreated;
        @XmlElement(name = "CreatedBy", required = true)
        protected String createdBy;
        @XmlElement(name = "ModifiedBy", required = true)
        protected String modifiedBy;

        /**
         * Gets the value of the filename property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Sets the value of the filename property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setFilename(String value) {
            filename = value;
        }

        /**
         * Gets the value of the dateModified property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getDateModified() {
            return dateModified;
        }

        /**
         * Sets the value of the dateModified property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setDateModified(String value) {
            dateModified = value;
        }

        /**
         * Gets the value of the dateCreated property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getDateCreated() {
            return dateCreated;
        }

        /**
         * Sets the value of the dateCreated property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setDateCreated(String value) {
            dateCreated = value;
        }

        /**
         * Gets the value of the createdBy property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCreatedBy() {
            return createdBy;
        }

        /**
         * Sets the value of the createdBy property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCreatedBy(String value) {
            createdBy = value;
        }

        /**
         * Gets the value of the modifiedBy property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getModifiedBy() {
            return modifiedBy;
        }

        /**
         * Sets the value of the modifiedBy property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setModifiedBy(String value) {
            modifiedBy = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Sequence"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="1"/&gt;
     *               &lt;enumeration value="2"/&gt;
     *               &lt;enumeration value="3"/&gt;
     *               &lt;enumeration value="4"/&gt;
     *               &lt;enumeration value="5"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="Enabled" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="MassID"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="204Pb"/&gt;
     *               &lt;enumeration value="205Pb"/&gt;
     *               &lt;enumeration value="206Pb"/&gt;
     *               &lt;enumeration value="207Pb"/&gt;
     *               &lt;enumeration value="208Pb"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="MassIDCollector" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="AxMass"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="204.000"/&gt;
     *               &lt;enumeration value="205.000"/&gt;
     *               &lt;enumeration value="206.000"/&gt;
     *               &lt;enumeration value="207.000"/&gt;
     *               &lt;enumeration value="208.000"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="IntegPeriod" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="IntegTime"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="10.0"/&gt;
     *               &lt;enumeration value="5.0"/&gt;
     *               &lt;enumeration value="2.0"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="PeakCentre"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="False"/&gt;
     *               &lt;enumeration value="True"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="PKCCollector" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="PKCMethod"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="None"/&gt;
     *               &lt;enumeration value="PeakScanning"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="PKCParentID"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="205Pb:PMS2"/&gt;
     *               &lt;enumeration value=""/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="BLReferences" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="CollectorArray"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="204Pb:PMS1"/&gt;
     *               &lt;enumeration value="205Pb:PMS2"/&gt;
     *               &lt;enumeration value="206Pb:PMS3"/&gt;
     *               &lt;enumeration value="207Pb:PMS4"/&gt;
     *               &lt;enumeration value="208Pb:PMS5"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="MagnetSettleTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="AxMassOffset"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="-0.1000"/&gt;
     *               &lt;enumeration value="0.0000"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="CurveFit" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TraceColour"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="Tomato"/&gt;
     *               &lt;enumeration value="Violet"/&gt;
     *               &lt;enumeration value="YellowGreen"/&gt;
     *               &lt;enumeration value="Black"/&gt;
     *               &lt;enumeration value="LightSeaGreen"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "sequence",
            "enabled",
            "massID",
            "massIDCollector",
            "axMass",
            "integPeriod",
            "integTime",
            "peakCentre",
            "pkcCollector",
            "pkcMethod",
            "pkcParentID",
            "blReferences",
            "collectorArray",
            "magnetSettleTime",
            "axMassOffset",
            "curveFit",
            "traceColour"
    })
    public static class ONPEAK {

        @XmlElement(name = "Sequence", required = true)
        protected String sequence;
        @XmlElement(name = "Enabled", required = true)
        protected String enabled;
        @XmlElement(name = "MassID", required = true)
        protected String massID;
        @XmlElement(name = "MassIDCollector", required = true)
        protected String massIDCollector;
        @XmlElement(name = "AxMass", required = true)
        protected String axMass;
        @XmlElement(name = "IntegPeriod", required = true)
        protected String integPeriod;
        @XmlElement(name = "IntegTime", required = true)
        protected String integTime;
        @XmlElement(name = "PeakCentre", required = true)
        protected String peakCentre;
        @XmlElement(name = "PKCCollector", required = true)
        protected String pkcCollector;
        @XmlElement(name = "PKCMethod", required = true)
        protected String pkcMethod;
        @XmlElement(name = "PKCParentID", required = true)
        protected String pkcParentID;
        @XmlElement(name = "BLReferences", required = true)
        protected String blReferences;
        @XmlElement(name = "CollectorArray", required = true)
        protected String collectorArray;
        @XmlElement(name = "MagnetSettleTime", required = true)
        protected String magnetSettleTime;
        @XmlElement(name = "AxMassOffset", required = true)
        protected String axMassOffset;
        @XmlElement(name = "CurveFit", required = true)
        protected String curveFit;
        @XmlElement(name = "TraceColour", required = true)
        protected String traceColour;

        /**
         * Gets the value of the sequence property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getSequence() {
            return sequence;
        }

        /**
         * Sets the value of the sequence property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setSequence(String value) {
            sequence = value;
        }

        /**
         * Gets the value of the enabled property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getEnabled() {
            return enabled;
        }

        /**
         * Sets the value of the enabled property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setEnabled(String value) {
            enabled = value;
        }

        /**
         * Gets the value of the massID property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getMassID() {
            return massID;
        }

        /**
         * Sets the value of the massID property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setMassID(String value) {
            massID = value;
        }

        /**
         * Gets the value of the massIDCollector property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getMassIDCollector() {
            return massIDCollector;
        }

        /**
         * Sets the value of the massIDCollector property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setMassIDCollector(String value) {
            massIDCollector = value;
        }

        /**
         * Gets the value of the axMass property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getAxMass() {
            return axMass;
        }

        /**
         * Sets the value of the axMass property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setAxMass(String value) {
            axMass = value;
        }

        /**
         * Gets the value of the integPeriod property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getIntegPeriod() {
            return integPeriod;
        }

        /**
         * Sets the value of the integPeriod property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIntegPeriod(String value) {
            integPeriod = value;
        }

        /**
         * Gets the value of the integTime property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getIntegTime() {
            return integTime;
        }

        /**
         * Sets the value of the integTime property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIntegTime(String value) {
            integTime = value;
        }

        /**
         * Gets the value of the peakCentre property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getPeakCentre() {
            return peakCentre;
        }

        /**
         * Sets the value of the peakCentre property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setPeakCentre(String value) {
            peakCentre = value;
        }

        /**
         * Gets the value of the pkcCollector property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getPKCCollector() {
            return pkcCollector;
        }

        /**
         * Sets the value of the pkcCollector property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setPKCCollector(String value) {
            pkcCollector = value;
        }

        /**
         * Gets the value of the pkcMethod property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getPKCMethod() {
            return pkcMethod;
        }

        /**
         * Sets the value of the pkcMethod property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setPKCMethod(String value) {
            pkcMethod = value;
        }

        /**
         * Gets the value of the pkcParentID property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getPKCParentID() {
            return pkcParentID;
        }

        /**
         * Sets the value of the pkcParentID property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setPKCParentID(String value) {
            pkcParentID = value;
        }

        /**
         * Gets the value of the blReferences property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getBLReferences() {
            return blReferences;
        }

        /**
         * Sets the value of the blReferences property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setBLReferences(String value) {
            blReferences = value;
        }

        /**
         * Gets the value of the collectorArray property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCollectorArray() {
            return collectorArray;
        }

        /**
         * Sets the value of the collectorArray property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCollectorArray(String value) {
            collectorArray = value;
        }

        /**
         * Gets the value of the magnetSettleTime property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getMagnetSettleTime() {
            return magnetSettleTime;
        }

        /**
         * Sets the value of the magnetSettleTime property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setMagnetSettleTime(String value) {
            magnetSettleTime = value;
        }

        /**
         * Gets the value of the axMassOffset property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getAxMassOffset() {
            return axMassOffset;
        }

        /**
         * Sets the value of the axMassOffset property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setAxMassOffset(String value) {
            axMassOffset = value;
        }

        /**
         * Gets the value of the curveFit property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCurveFit() {
            return curveFit;
        }

        /**
         * Sets the value of the curveFit property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCurveFit(String value) {
            curveFit = value;
        }

        /**
         * Gets the value of the traceColour property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTraceColour() {
            return traceColour;
        }

        /**
         * Sets the value of the traceColour property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTraceColour(String value) {
            traceColour = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="IntersperseBaselines" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="BaselineFrequency" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="PKCCycleInterval" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TotalBlocks" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TotalCycles" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="CalculateRatios" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="InitialBLMagnetDelay" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="DeflectBeamProtection" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="PerformBeamInterpCycles" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="PerformInterBlocks" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="AxialColl" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="ActiveSEMCollectors" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="MagnetFlybackSettleTime" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="CorrectForBaselines" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="CorrectForDeadtimes" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="IsoWorks_Method" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Rejection_Sigma" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Rejection_Percentage" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="ActiveSubChannel" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="ProcessingUnits" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TFE_Enabled" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TFE_MonitorPeak" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TFE_IntensityThreshold" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TFE_IntervalPeakCentring" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TFE_PeakCentringInterval" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="TFE_PeakCentringMonitorThreshold" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Export_IsotopePoints" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Export_ASCFile" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Export_ASCOptions" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Export_ASCActiveColl" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Export_TXTEveryCYCLE" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="Export_TripoliLiveData" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "intersperseBaselines",
            "baselineFrequency",
            "pkcCycleInterval",
            "totalBlocks",
            "totalCycles",
            "calculateRatios",
            "initialBLMagnetDelay",
            "deflectBeamProtection",
            "performBeamInterpCycles",
            "performInterBlocks",
            "axialColl",
            "activeSEMCollectors",
            "magnetFlybackSettleTime",
            "correctForBaselines",
            "correctForDeadtimes",
            "isoWorksMethod",
            "rejectionSigma",
            "rejectionPercentage",
            "activeSubChannel",
            "processingUnits",
            "tfeEnabled",
            "tfeMonitorPeak",
            "tfeIntensityThreshold",
            "tfeIntervalPeakCentring",
            "tfePeakCentringInterval",
            "tfePeakCentringMonitorThreshold",
            "exportIsotopePoints",
            "exportASCFile",
            "exportASCOptions",
            "exportASCActiveColl",
            "exportTXTEveryCYCLE",
            "exportTripoliLiveData"
    })
    public static class SETTINGS {

        @XmlElement(name = "IntersperseBaselines", required = true)
        protected String intersperseBaselines;
        @XmlElement(name = "BaselineFrequency", required = true)
        protected String baselineFrequency;
        @XmlElement(name = "PKCCycleInterval", required = true)
        protected String pkcCycleInterval;
        @XmlElement(name = "TotalBlocks", required = true)
        protected String totalBlocks;
        @XmlElement(name = "TotalCycles", required = true)
        protected String totalCycles;
        @XmlElement(name = "CalculateRatios", required = true)
        protected String calculateRatios;
        @XmlElement(name = "InitialBLMagnetDelay", required = true)
        protected String initialBLMagnetDelay;
        @XmlElement(name = "DeflectBeamProtection", required = true)
        protected String deflectBeamProtection;
        @XmlElement(name = "PerformBeamInterpCycles", required = true)
        protected String performBeamInterpCycles;
        @XmlElement(name = "PerformInterBlocks", required = true)
        protected String performInterBlocks;
        @XmlElement(name = "AxialColl", required = true)
        protected String axialColl;
        @XmlElement(name = "ActiveSEMCollectors", required = true)
        protected String activeSEMCollectors;
        @XmlElement(name = "MagnetFlybackSettleTime", required = true)
        protected String magnetFlybackSettleTime;
        @XmlElement(name = "CorrectForBaselines", required = true)
        protected String correctForBaselines;
        @XmlElement(name = "CorrectForDeadtimes", required = true)
        protected String correctForDeadtimes;
        @XmlElement(name = "IsoWorks_Method", required = true)
        protected String isoWorksMethod;
        @XmlElement(name = "Rejection_Sigma", required = true)
        protected String rejectionSigma;
        @XmlElement(name = "Rejection_Percentage", required = true)
        protected String rejectionPercentage;
        @XmlElement(name = "ActiveSubChannel", required = true)
        protected String activeSubChannel;
        @XmlElement(name = "ProcessingUnits", required = true)
        protected String processingUnits;
        @XmlElement(name = "TFE_Enabled", required = true)
        protected String tfeEnabled;
        @XmlElement(name = "TFE_MonitorPeak", required = true)
        protected String tfeMonitorPeak;
        @XmlElement(name = "TFE_IntensityThreshold", required = true)
        protected String tfeIntensityThreshold;
        @XmlElement(name = "TFE_IntervalPeakCentring", required = true)
        protected String tfeIntervalPeakCentring;
        @XmlElement(name = "TFE_PeakCentringInterval", required = true)
        protected String tfePeakCentringInterval;
        @XmlElement(name = "TFE_PeakCentringMonitorThreshold", required = true)
        protected String tfePeakCentringMonitorThreshold;
        @XmlElement(name = "Export_IsotopePoints", required = true)
        protected String exportIsotopePoints;
        @XmlElement(name = "Export_ASCFile", required = true)
        protected String exportASCFile;
        @XmlElement(name = "Export_ASCOptions", required = true)
        protected String exportASCOptions;
        @XmlElement(name = "Export_ASCActiveColl", required = true)
        protected String exportASCActiveColl;
        @XmlElement(name = "Export_TXTEveryCYCLE", required = true)
        protected String exportTXTEveryCYCLE;
        @XmlElement(name = "Export_TripoliLiveData", required = true)
        protected String exportTripoliLiveData;

        /**
         * Gets the value of the intersperseBaselines property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getIntersperseBaselines() {
            return intersperseBaselines;
        }

        /**
         * Sets the value of the intersperseBaselines property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIntersperseBaselines(String value) {
            intersperseBaselines = value;
        }

        /**
         * Gets the value of the baselineFrequency property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getBaselineFrequency() {
            return baselineFrequency;
        }

        /**
         * Sets the value of the baselineFrequency property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setBaselineFrequency(String value) {
            baselineFrequency = value;
        }

        /**
         * Gets the value of the pkcCycleInterval property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getPKCCycleInterval() {
            return pkcCycleInterval;
        }

        /**
         * Sets the value of the pkcCycleInterval property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setPKCCycleInterval(String value) {
            pkcCycleInterval = value;
        }

        /**
         * Gets the value of the totalBlocks property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTotalBlocks() {
            return totalBlocks;
        }

        /**
         * Sets the value of the totalBlocks property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTotalBlocks(String value) {
            totalBlocks = value;
        }

        /**
         * Gets the value of the totalCycles property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTotalCycles() {
            return totalCycles;
        }

        /**
         * Sets the value of the totalCycles property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTotalCycles(String value) {
            totalCycles = value;
        }

        /**
         * Gets the value of the calculateRatios property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCalculateRatios() {
            return calculateRatios;
        }

        /**
         * Sets the value of the calculateRatios property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCalculateRatios(String value) {
            calculateRatios = value;
        }

        /**
         * Gets the value of the initialBLMagnetDelay property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getInitialBLMagnetDelay() {
            return initialBLMagnetDelay;
        }

        /**
         * Sets the value of the initialBLMagnetDelay property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setInitialBLMagnetDelay(String value) {
            initialBLMagnetDelay = value;
        }

        /**
         * Gets the value of the deflectBeamProtection property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getDeflectBeamProtection() {
            return deflectBeamProtection;
        }

        /**
         * Sets the value of the deflectBeamProtection property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setDeflectBeamProtection(String value) {
            deflectBeamProtection = value;
        }

        /**
         * Gets the value of the performBeamInterpCycles property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getPerformBeamInterpCycles() {
            return performBeamInterpCycles;
        }

        /**
         * Sets the value of the performBeamInterpCycles property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setPerformBeamInterpCycles(String value) {
            performBeamInterpCycles = value;
        }

        /**
         * Gets the value of the performInterBlocks property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getPerformInterBlocks() {
            return performInterBlocks;
        }

        /**
         * Sets the value of the performInterBlocks property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setPerformInterBlocks(String value) {
            performInterBlocks = value;
        }

        /**
         * Gets the value of the axialColl property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getAxialColl() {
            return axialColl;
        }

        /**
         * Sets the value of the axialColl property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setAxialColl(String value) {
            axialColl = value;
        }

        /**
         * Gets the value of the activeSEMCollectors property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getActiveSEMCollectors() {
            return activeSEMCollectors;
        }

        /**
         * Sets the value of the activeSEMCollectors property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setActiveSEMCollectors(String value) {
            activeSEMCollectors = value;
        }

        /**
         * Gets the value of the magnetFlybackSettleTime property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getMagnetFlybackSettleTime() {
            return magnetFlybackSettleTime;
        }

        /**
         * Sets the value of the magnetFlybackSettleTime property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setMagnetFlybackSettleTime(String value) {
            magnetFlybackSettleTime = value;
        }

        /**
         * Gets the value of the correctForBaselines property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCorrectForBaselines() {
            return correctForBaselines;
        }

        /**
         * Sets the value of the correctForBaselines property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCorrectForBaselines(String value) {
            correctForBaselines = value;
        }

        /**
         * Gets the value of the correctForDeadtimes property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCorrectForDeadtimes() {
            return correctForDeadtimes;
        }

        /**
         * Sets the value of the correctForDeadtimes property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCorrectForDeadtimes(String value) {
            correctForDeadtimes = value;
        }

        /**
         * Gets the value of the isoWorksMethod property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getIsoWorksMethod() {
            return isoWorksMethod;
        }

        /**
         * Sets the value of the isoWorksMethod property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setIsoWorksMethod(String value) {
            isoWorksMethod = value;
        }

        /**
         * Gets the value of the rejectionSigma property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getRejectionSigma() {
            return rejectionSigma;
        }

        /**
         * Sets the value of the rejectionSigma property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setRejectionSigma(String value) {
            rejectionSigma = value;
        }

        /**
         * Gets the value of the rejectionPercentage property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getRejectionPercentage() {
            return rejectionPercentage;
        }

        /**
         * Sets the value of the rejectionPercentage property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setRejectionPercentage(String value) {
            rejectionPercentage = value;
        }

        /**
         * Gets the value of the activeSubChannel property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getActiveSubChannel() {
            return activeSubChannel;
        }

        /**
         * Sets the value of the activeSubChannel property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setActiveSubChannel(String value) {
            activeSubChannel = value;
        }

        /**
         * Gets the value of the processingUnits property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getProcessingUnits() {
            return processingUnits;
        }

        /**
         * Sets the value of the processingUnits property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setProcessingUnits(String value) {
            processingUnits = value;
        }

        /**
         * Gets the value of the tfeEnabled property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTFEEnabled() {
            return tfeEnabled;
        }

        /**
         * Sets the value of the tfeEnabled property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTFEEnabled(String value) {
            tfeEnabled = value;
        }

        /**
         * Gets the value of the tfeMonitorPeak property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTFEMonitorPeak() {
            return tfeMonitorPeak;
        }

        /**
         * Sets the value of the tfeMonitorPeak property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTFEMonitorPeak(String value) {
            tfeMonitorPeak = value;
        }

        /**
         * Gets the value of the tfeIntensityThreshold property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTFEIntensityThreshold() {
            return tfeIntensityThreshold;
        }

        /**
         * Sets the value of the tfeIntensityThreshold property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTFEIntensityThreshold(String value) {
            tfeIntensityThreshold = value;
        }

        /**
         * Gets the value of the tfeIntervalPeakCentring property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTFEIntervalPeakCentring() {
            return tfeIntervalPeakCentring;
        }

        /**
         * Sets the value of the tfeIntervalPeakCentring property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTFEIntervalPeakCentring(String value) {
            tfeIntervalPeakCentring = value;
        }

        /**
         * Gets the value of the tfePeakCentringInterval property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTFEPeakCentringInterval() {
            return tfePeakCentringInterval;
        }

        /**
         * Sets the value of the tfePeakCentringInterval property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTFEPeakCentringInterval(String value) {
            tfePeakCentringInterval = value;
        }

        /**
         * Gets the value of the tfePeakCentringMonitorThreshold property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getTFEPeakCentringMonitorThreshold() {
            return tfePeakCentringMonitorThreshold;
        }

        /**
         * Sets the value of the tfePeakCentringMonitorThreshold property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setTFEPeakCentringMonitorThreshold(String value) {
            tfePeakCentringMonitorThreshold = value;
        }

        /**
         * Gets the value of the exportIsotopePoints property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getExportIsotopePoints() {
            return exportIsotopePoints;
        }

        /**
         * Sets the value of the exportIsotopePoints property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setExportIsotopePoints(String value) {
            exportIsotopePoints = value;
        }

        /**
         * Gets the value of the exportASCFile property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getExportASCFile() {
            return exportASCFile;
        }

        /**
         * Sets the value of the exportASCFile property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setExportASCFile(String value) {
            exportASCFile = value;
        }

        /**
         * Gets the value of the exportASCOptions property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getExportASCOptions() {
            return exportASCOptions;
        }

        /**
         * Sets the value of the exportASCOptions property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setExportASCOptions(String value) {
            exportASCOptions = value;
        }

        /**
         * Gets the value of the exportASCActiveColl property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getExportASCActiveColl() {
            return exportASCActiveColl;
        }

        /**
         * Sets the value of the exportASCActiveColl property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setExportASCActiveColl(String value) {
            exportASCActiveColl = value;
        }

        /**
         * Gets the value of the exportTXTEveryCYCLE property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getExportTXTEveryCYCLE() {
            return exportTXTEveryCYCLE;
        }

        /**
         * Sets the value of the exportTXTEveryCYCLE property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setExportTXTEveryCYCLE(String value) {
            exportTXTEveryCYCLE = value;
        }

        /**
         * Gets the value of the exportTripoliLiveData property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getExportTripoliLiveData() {
            return exportTripoliLiveData;
        }

        /**
         * Sets the value of the exportTripoliLiveData property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setExportTripoliLiveData(String value) {
            exportTripoliLiveData = value;
        }

    }

}