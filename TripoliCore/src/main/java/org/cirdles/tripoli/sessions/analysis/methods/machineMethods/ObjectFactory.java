
package org.cirdles.tripoli.sessions.analysis.methods.machineMethods;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the machineMethods package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: machineMethods
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PhoenixAnalysisMethod }
     * 
     */
    public PhoenixAnalysisMethod createANALYSISMETHOD() {
        return new PhoenixAnalysisMethod();
    }

    /**
     * Create an instance of {@link PhoenixAnalysisMethod.HEADER }
     * 
     */
    public PhoenixAnalysisMethod.HEADER createANALYSISMETHODHEADER() {
        return new PhoenixAnalysisMethod.HEADER();
    }

    /**
     * Create an instance of {@link PhoenixAnalysisMethod.SETTINGS }
     * 
     */
    public PhoenixAnalysisMethod.SETTINGS createANALYSISMETHODSETTINGS() {
        return new PhoenixAnalysisMethod.SETTINGS();
    }

    /**
     * Create an instance of {@link PhoenixAnalysisMethod.BASELINE }
     * 
     */
    public PhoenixAnalysisMethod.BASELINE createANALYSISMETHODBASELINE() {
        return new PhoenixAnalysisMethod.BASELINE();
    }

    /**
     * Create an instance of {@link PhoenixAnalysisMethod.ONPEAK }
     * 
     */
    public PhoenixAnalysisMethod.ONPEAK createANALYSISMETHODONPEAK() {
        return new PhoenixAnalysisMethod.ONPEAK();
    }

    /**
     * Create an instance of {@link PhoenixAnalysisMethod.EQUILIBRATION }
     * 
     */
    public PhoenixAnalysisMethod.EQUILIBRATION createANALYSISMETHODEQUILIBRATION() {
        return new PhoenixAnalysisMethod.EQUILIBRATION();
    }

}