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

package org.cirdles.tripoli.parameterModels;

import org.cirdles.tripoli.matrices.matrixModels.AbstractMatrixModel;
import org.cirdles.tripoli.matrices.matrixModels.CorrelationMatrixModel;
import org.cirdles.tripoli.matrices.matrixModels.CovarianceMatrixModel;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.valueModels.ValueModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static org.cirdles.tripoli.TripoliConstants.DEFAULT_OBJECT_NAME;

/**
 * @author James F. Bowring
 */
public abstract class AbstractRatiosDataModel implements
        Comparable<AbstractRatiosDataModel>,
        Serializable//,
        /*XMLSerializationI*/ {

    // private static final long serialVersionUID = -3311656789053878314L;
    /**
     *
     */
    protected transient String XMLSchemaURL;
    /**
     *
     */
    protected transient AbstractMatrixModel dataCovariancesVarUnct;

    /**
     *
     */
    protected transient AbstractMatrixModel dataCorrelationsVarUnct;

    /**
     *
     */
    protected transient AbstractMatrixModel dataCovariancesSysUnct;

    /**
     *
     */
    protected transient AbstractMatrixModel dataCorrelationsSysUnct;
    // Instance variables
    /**
     *
     */
    protected String modelName;
    /**
     *
     */
    protected int versionNumber;

    /**
     *
     */
    protected int minorVersionNumber;
    /**
     *
     */
    protected String labName;
    /**
     *
     */
    protected String dateCertified;
    /**
     *
     */
    protected String reference;
    /**
     *
     */
    protected String comment;
    /**
     *
     */
    protected ValueModel[] ratios;
    /**
     *
     */
    protected Map<String, BigDecimal> rhosUnct;

    /**
     *
     */
    protected Map<String, BigDecimal> rhosSysUnct;

    /**
     *
     */
    protected boolean immutable;

    // Constructors

    /**
     *
     */
    protected AbstractRatiosDataModel() {
        XMLSchemaURL = "";
        modelName = DEFAULT_OBJECT_NAME;
        versionNumber = 1;
        minorVersionNumber = 0;
        labName = DEFAULT_OBJECT_NAME;

        // TODO: switch to https://www.baeldung.com/java-8-date-time-intro
        //  this.dateCertified = DateHelpers.defaultEarthTimeDateString();

        reference = "None";
        comment = "None";

        ratios = new ValueModel[0];
        rhosUnct = new HashMap<>();
        rhosSysUnct = new HashMap<>();
        dataCovariancesVarUnct = new CovarianceMatrixModel();
        dataCorrelationsVarUnct = new CorrelationMatrixModel();
        dataCovariancesSysUnct = new CovarianceMatrixModel();
        dataCorrelationsSysUnct = new CorrelationMatrixModel();
        immutable = false;
    }

    /**
     * @param modelName
     * @param versionNumber
     * @param minorVersionNumber the value of minorVersionNumber
     * @param labName            the value of labName
     * @param dateCertified
     * @param reference          the value of reference
     * @param comment            the value of comment
     */
    protected AbstractRatiosDataModel(
            String modelName, int versionNumber, int minorVersionNumber, String labName, String dateCertified, String reference, String comment) {
        this();
        this.modelName = modelName.trim();
        this.versionNumber = versionNumber;
        this.minorVersionNumber = minorVersionNumber;
        this.labName = labName;
        this.dateCertified = dateCertified;
        this.reference = reference;
        this.comment = comment;
    }

    /**
     * @param name
     * @param version
     * @param minorVersionNumber the value of minorVersionNumber
     * @return
     */
    protected static String makeNameAndVersion(String name, int version, int minorVersionNumber) {
        return name.trim() + " v." + version + "." + minorVersionNumber;
    }

    /**
     * @param doAppendName the value of doAppendName
     * @return the org.earthtime.ratioDataModels.AbstractRatiosDataModel
     */
    public AbstractRatiosDataModel copyModel(boolean doAppendName) {

        AbstractRatiosDataModel myModel = cloneModel();
        myModel.modelName = myModel.modelName + (doAppendName ? "-COPY" : "");

        myModel.initializeModel();

        return myModel;
    }

    /**
     * @return
     */
    public abstract AbstractRatiosDataModel cloneModel();

    /**
     * compares this {@code AbstractRatiosDataModel} to argument
     * {@code AbstractRatiosDataModel} by their {@code name} and
     * {@code version}.
     *
     * @param model
     * @return {@code int} - 0 if this
     * {@code AbstractRatiosDataModel}'s {@code name} and
     * {@code version} is the same as argument
     * {@code AbstractRatiosDataModel}'s, -1 if they are lexicographically
     * less than argument {@code AbstractRatiosDataModel}'s, and 1 if they
     * are greater than argument {@code AbstractRatiosDataModel}'s
     * @throws java.lang.ClassCastException a ClassCastException
     * @pre argument {@code AbstractRatiosDataModel} is a valid
     * {@code AbstractRatiosDataModel}
     * @post returns an {@code int} representing the comparison between
     * this {@code AbstractRatiosDataModel} and argument
     * {@code AbstractRatiosDataModel}
     */
    @Override
    public int compareTo(AbstractRatiosDataModel model) throws ClassCastException {
        String modelID = model.getNameAndVersion().trim();
        return (getNameAndVersion().trim() //
                .compareToIgnoreCase(modelID));
    }

    /**
     * compares this {@code AbstractRatiosDataModel} to argument
     * {@code AbstractRatiosDataModel} by their {@code name} and
     * {@code version}.
     *
     * @param model
     * @return {@code boolean} - {@code true} if argument      {@code
     * AbstractRatiosDataModel} is this
     * {@code AbstractRatiosDataModel} or their {@code name} and
     * {@code version} are identical, else {@code false}
     * @pre argument {@code AbstractRatiosDataModel} is a valid
     * {@code AbstractRatiosDataModel}
     * @post returns a {@code boolean} representing the equality of this
     * {@code AbstractRatiosDataModel} and argument
     * {@code AbstractRatiosDataModel} based on their {@code name} and
     * {@code version}
     */
    @Override
    public boolean equals(Object model) {
        //check for self-comparison
        if (this == model) {
            return true;
        }
        if (!(model instanceof AbstractRatiosDataModel myModel)) {
            return false;
        }

        return (0 == getNameAndVersion().trim().compareToIgnoreCase( //
                myModel.getNameAndVersion().trim()));
    }

    /**
     * returns 0 as the hashcode for this {@code AbstractRatiosDataModel}.
     * Implemented to meet equivalency requirements as documented by
     * {@code java.lang.Object}
     *
     * @return {@code int} - 0
     * @pre this {@code AbstractRatiosDataModel} exists
     * @post hashcode of 0 is returned for this
     * {@code AbstractRatiosDataModel}
     */
    // http://www.javaworld.com/javaworld/jw-01-1999/jw-01-object.html?page=4
    @Override
    public int hashCode() {

        return 0;
    }

    /**
     *
     */
    public void initializeModel() {

        dataCovariancesVarUnct = new CovarianceMatrixModel();
        dataCorrelationsVarUnct = new CorrelationMatrixModel();

        dataCovariancesSysUnct = new CovarianceMatrixModel();
        dataCorrelationsSysUnct = new CorrelationMatrixModel();

        refreshModel();
    }

    /**
     *
     */
    public void refreshModel() {
        try {
            initializeBothDataCorrelationM();
            generateBothUnctCovarianceMFromEachUnctCorrelationM();
        } catch (Exception e) {
//            System.out.println(e.getMessage());
        }
    }

    /**
     * @param dataIncoming
     * @param myRhos
     * @param myRhosSysUnct the value of myRhosSysUnct
     */
    public void initializeModel(ValueModel[] dataIncoming, Map<String, BigDecimal> myRhos, Map<String, BigDecimal> myRhosSysUnct) {

        // precondition: the data model has been created with a prescribed set of ratios with names
        // need to check each incoming ratio for validity
        for (ValueModel valueModel : dataIncoming) {
            ValueModel ratio = ValueModel.createCopyOfValueModel(getDatumByName(valueModel.getName()));
        }

        // introduce special comparator that puts concentrations (conc) after dataIncoming)//dec 2014 not needed
        Arrays.sort(ratios, new DataValueModelNameComparator());

        if (null == myRhos) {
            buildRhosMap();
        } else {
            for (String key : myRhos.keySet()) {
                if (null != rhosUnct.get(key)) {
                    rhosUnct.put(key, myRhos.get(key));
                }
            }
        }

        if (null == myRhosSysUnct) {
            buildRhosSysUnctMap();
        } else {
            for (String key : myRhosSysUnct.keySet()) {
                if (null != rhosSysUnct.get(key)) {
                    rhosSysUnct.put(key, myRhosSysUnct.get(key));
                }
            }
        }

        initializeModel();
    }

    /**
     * @return the immutable
     */
    public boolean isImmutable() {
        return immutable;
    }

    /**
     * @param immutable the immutable to set
     */
    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    /**
     * @return the minorVersionNumber
     */
    public int getMinorVersionNumber() {
        return minorVersionNumber;
    }

    /**
     * @param minorVersionNumber the minorVersionNumber to set
     */
    public void setMinorVersionNumber(int minorVersionNumber) {
        this.minorVersionNumber = minorVersionNumber;
    }

    /**
     * @return the dataCovariancesSysUnct
     */
    public AbstractMatrixModel getDataCovariancesSysUnct() {
        return dataCovariancesSysUnct;
    }

    /**
     * @return the dataCorrelationsSysUnct
     */
    public AbstractMatrixModel getDataCorrelationsSysUnct() {
        return dataCorrelationsSysUnct;
    }

    /**
     *
     */
    protected void buildRhosMap() {

        rhosUnct = new HashMap<>();

        for (int i = 0; i < ratios.length; i++) {
            for (int j = i + 1; j < ratios.length; j++) {
                String key = "rho" + ratios[i].getName().substring(0, 1).toUpperCase() + ratios[i].getName().substring(1) + "__" + ratios[j].getName();
                rhosUnct.put(key, BigDecimal.ZERO);
            }
        }
    }

    /**
     *
     */
    protected void buildRhosSysUnctMap() {

        rhosSysUnct = new HashMap<>();

        for (int i = 0; i < ratios.length; i++) {
            for (int j = i + 1; j < ratios.length; j++) {
                String key = "rho" + ratios[i].getName().substring(0, 1).toUpperCase() + ratios[i].getName().substring(1) + "__" + ratios[j].getName();
                rhosSysUnct.put(key, BigDecimal.ZERO);
            }
        }
    }

    /**
     *
     */
    protected void initializeBothDataCorrelationM() {
        Map<Integer, String> dataNamesList = new HashMap<>();

        // only build matrices for values with positive uncertainties
        int ratioCount = 0;
        for (ValueModel ratio : ratios) {
            if (ratio.hasPositiveVarUnct()) {
                dataNamesList.put(ratioCount, ratio.getName());
                ratioCount++;
            }
        }
        dataCorrelationsVarUnct.setRows(dataNamesList);
        dataCorrelationsVarUnct.setCols(dataNamesList);

        dataCorrelationsVarUnct.initializeMatrix();

        ((CorrelationMatrixModel) dataCorrelationsVarUnct).initializeCorrelations(rhosUnct);

        // sept 2014 new sys unct
        dataNamesList = new HashMap<>();
        ratioCount = 0;
        for (ValueModel ratio : ratios) {
            if (ratio.hasPositiveSysUnct()) {
                dataNamesList.put(ratioCount, ratio.getName());
                ratioCount++;
            }
        }

        dataCorrelationsSysUnct.setRows(dataNamesList);
        dataCorrelationsSysUnct.setCols(dataNamesList);

        dataCorrelationsSysUnct.initializeMatrix();

        ((CorrelationMatrixModel) dataCorrelationsSysUnct).initializeCorrelations(rhosSysUnct);

    }

    /**
     *
     */
    protected void copyBothRhosFromEachCorrelationM() {
        // sept 2014 backwards compat
        if (null == rhosUnct) {
            buildRhosMap();
        }

        rhosUnct.replaceAll((n, v) -> BigDecimal.valueOf(((CorrelationMatrixModel) dataCorrelationsVarUnct).getCorrelationCell(n)));

        // sept 2014 backwards compat
        if (null == rhosSysUnct) {
            buildRhosSysUnctMap();
        }

        rhosSysUnct.replaceAll((n, v) -> BigDecimal.valueOf(((CorrelationMatrixModel) dataCorrelationsSysUnct).getCorrelationCell(n)));
    }

    /**
     * @param checkCovarianceValidity the value of checkCovarianceValidity
     * @throws TripoliException
     */
    public void saveEdits(boolean checkCovarianceValidity)
            throws TripoliException {
        if ((null != dataCorrelationsVarUnct) || (null != dataCorrelationsSysUnct)) {
            generateBothUnctCovarianceMFromEachUnctCorrelationM();

            copyBothRhosFromEachCorrelationM();

            if (checkCovarianceValidity && dataCovariancesVarUnct.isCovMatrixSymmetricAndPositiveDefinite()) {
                throw new TripoliException("Var Unct Correlations yield Var Unct covariance matrix NOT positive definite.");
            }
            if (checkCovarianceValidity && dataCovariancesSysUnct.isCovMatrixSymmetricAndPositiveDefinite()) {
                throw new TripoliException("Sys Unct Correlations yield Sys Unct covariance matrix NOT positive definite.");
            }
        }
    }

    /**
     * **************
     * section for translating correlation to covariance and back Correlation
     * coefficient (x,y) = covariance(x,y) / (1-sigma for x * 1-sigma for y)
     * both matrices have the same ratio names in rows and columns in the same
     * order
     */
    protected void generateBothUnctCorrelationMFromEachUnctCovarianceM() {

        Iterator<String> colNames;
        try {
            dataCorrelationsVarUnct.copyValuesFrom(dataCovariancesVarUnct);
            // divide each cell by (1-sigma for x * 1-sigma for y)
            colNames = dataCorrelationsVarUnct.getCols().keySet().iterator();
            while (colNames.hasNext()) {
                String colName = colNames.next();
                ValueModel colData = getDatumByName(colName);
                int col = dataCorrelationsVarUnct.getCols().get(colName);
                //calculate values for this column
                int rowColDimension = dataCorrelationsVarUnct.getMatrix().getColumnDimension();
                for (int row = 0; row < rowColDimension; row++) {
                    String rowName = dataCorrelationsVarUnct.getRows().get(row);
                    ValueModel rowData = getDatumByName(rowName);
                    double correlation
                            = //
                            dataCovariancesVarUnct.getMatrix().get(row, col)//
                                    / rowData.getAnalyticalOneSigmaAbs().doubleValue() //
                                    / colData.getAnalyticalOneSigmaAbs().doubleValue();
                    dataCorrelationsVarUnct.setValueAt(row, col, correlation);
                }
            }
        } catch (Exception e) {
        }

        try {
            dataCorrelationsSysUnct.copyValuesFrom(dataCovariancesSysUnct);
            // divide each cell by (1-sigma for x * 1-sigma for y)
            colNames = dataCorrelationsSysUnct.getCols().keySet().iterator();
            while (colNames.hasNext()) {
                String colName = colNames.next();
                ValueModel colData = getDatumByName(colName);
                int col = dataCorrelationsSysUnct.getCols().get(colName);
                //calculate values for this column
                int rowColDimension = dataCorrelationsSysUnct.getMatrix().getColumnDimension();
                for (int row = 0; row < rowColDimension; row++) {
                    String rowName = dataCorrelationsSysUnct.getRows().get(row);
                    ValueModel rowData = getDatumByName(rowName);
                    double correlation =
                            dataCovariancesSysUnct.getMatrix().get(row, col)
                                    / rowData.getSystematicOneSigmaAbs().doubleValue()
                                    / colData.getSystematicOneSigmaAbs().doubleValue();
                    dataCorrelationsSysUnct.setValueAt(row, col, correlation);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    protected void generateBothUnctCovarianceMFromEachUnctCorrelationM() {

        Iterator<String> colNames;
        try {
            dataCovariancesVarUnct.copyValuesFrom(dataCorrelationsVarUnct);
            // divide each cell by (1-sigma for x * 1-sigma for y)
            colNames = dataCovariancesVarUnct.getCols().keySet().iterator();
            while (colNames.hasNext()) {
                String colName = colNames.next();
                ValueModel colData = getDatumByName(colName);
                int col = dataCovariancesVarUnct.getCols().get(colName);
                //calculate values for this column
                int rowColDimension = dataCovariancesVarUnct.getMatrix().getColumnDimension();
                for (int row = 0; row < rowColDimension; row++) {
                    String rowName = dataCovariancesVarUnct.getRows().get(row);
                    ValueModel rowData = getDatumByName(rowName);
                    double covariance
                            = //
                            dataCorrelationsVarUnct.getMatrix().get(row, col)//
                                    * rowData.getAnalyticalOneSigmaAbs().doubleValue() //
                                    * colData.getAnalyticalOneSigmaAbs().doubleValue();
                    dataCovariancesVarUnct.setValueAt(row, col, covariance);
                }
            }
        } catch (Exception e) {
        }

        try {
            dataCovariancesSysUnct.copyValuesFrom(dataCorrelationsSysUnct);
            // divide each cell by (1-sigma for x * 1-sigma for y)
            colNames = dataCovariancesSysUnct.getCols().keySet().iterator();
            while (colNames.hasNext()) {
                String colName = colNames.next();
                ValueModel colData = getDatumByName(colName);
                int col = dataCovariancesSysUnct.getCols().get(colName);
                //calculate values for this column
                int rowColDimension = dataCovariancesSysUnct.getMatrix().getColumnDimension();
                for (int row = 0; row < rowColDimension; row++) {
                    String rowName = dataCovariancesSysUnct.getRows().get(row);
                    ValueModel rowData = getDatumByName(rowName);
                    double covariance
                            = //
                            dataCorrelationsSysUnct.getMatrix().get(row, col)//
                                    * rowData.getSystematicOneSigmaAbs().doubleValue() //
                                    * colData.getSystematicOneSigmaAbs().doubleValue();
                    dataCovariancesSysUnct.setValueAt(row, col, covariance);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * gets the {@code modelName} of this
     * {@code AbstractRatiosDataModel}.
     *
     * @return {@code String} - this {@code AbstractRatiosDataModel}'s
     * {@code modelName}
     * @pre this {@code AbstractRatiosDataModel} exists
     * @post returns this {@code AbstractRatiosDataModel}'s
     * {@code modelName}
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * @param modelName the modelName to set
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * gets the {@code versionNumber} of this
     * {@code AbstractRatiosDataModel}.
     *
     * @return {@code int} - this {@code AbstractRatiosDataModel}'s
     * {@code versionNumber}
     * @pre this {@code AbstractRatiosDataModel} exists
     * @post returns this {@code AbstractRatiosDataModel}'s
     * {@code versionNumber}
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * @param versionNumber the versionNumber to set
     */
    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    /**
     * gets the {@code labName} of this
     * {@code AbstractRatiosDataModel}.
     *
     * @return {@code String} - this {@code AbstractRatiosDataModel}'s
     * {@code labName}
     * @pre this {@code AbstractRatiosDataModel} exists
     * @post returns this {@code AbstractRatiosDataModel}'s
     * {@code labName}
     */
    public String getLabName() {
        return labName;
    }

    /**
     * @param labName the labName to set
     */
    public void setLabName(String labName) {
        this.labName = labName;
    }

    /**
     * gets the {@code dateCertified} of this
     * {@code AbstractRatiosDataModel}.
     *
     * @return {@code String} - this {@code AbstractRatiosDataModel}'s
     * {@code dateCertified}
     * @pre this {@code AbstractRatiosDataModel} exists
     * @post returns this {@code AbstractRatiosDataModel}'s
     * {@code dateCertified}
     */
    public String getDateCertified() {
        return dateCertified;
    }

    /**
     * @param dateCertified the dateCertified to set
     */
    public void setDateCertified(String dateCertified) {
        this.dateCertified = dateCertified;
    }

    /**
     * gets a {@code String} containing this
     * {@code AbstractRatiosDataModel}'s {@code modelName} and
     * {@code versionNumber}.
     *
     * @return {@code String} - this {@code AbstractRatiosDataModel}'s
     * {@code modelName} and {@code versionNumber}
     * @pre this {@code AbstractRatiosDataModel} exists
     * @post returns a {@code String} containing this
     * {@code AbstractRatiosDataModel}'s {@code modelName} and
     * {@code versionNumber}
     */
    public String getNameAndVersion() {
        return makeNameAndVersion(modelName, versionNumber, minorVersionNumber);
    }

    /**
     * gets the {@code ratios} of this
     * {@code AbstractRatiosDataModel}.
     *
     * @return {@code ValueModel[]} - collection of this
     * {@code AbstractRatiosDataModel}'s {@code ratios}
     * @pre this {@code AbstractRatiosDataModel} exists
     * @post returns this {@code AbstractRatiosDataModel}'s
     * {@code ratios}
     */
    public ValueModel[] getData() {
        return ratios;
    }

    /**
     * @return
     */
    public ValueModel[] cloneData() {
        ValueModel[] clonedData = new ValueModel[ratios.length];

        for (int i = 0; i < ratios.length; i++) {
            clonedData[i] = ValueModel.createCopyOfValueModel(ratios[i]);
        }
        return clonedData;
    }

    /**
     * gets a single ratio from this {@code AbstractRatiosDataModel}'s
     * {@code ratios} specified by argument {@code datumName}. Returns
     * a new, empty      {@code
     * ValueModel} if no matching ratio is found.
     *
     * @param datumName name of the ratio to search for
     * @return {@code ValueModel} - ratio found in {@code ratios}
     * whose name matches argument {@code datumName} or a new      {@code
     * ValueModel} if no match is found
     * @pre argument {@code datumName} is a valid {@code String}
     * @post returns the {@code ValueModel} found in this
     * {@code AbstractRatiosDataModel}'s {@code ratios} whose name
     * matches argument {@code datumName}
     */
    public ValueModel getDatumByName(String datumName) {

        ValueModel retVal = ValueModel.createEmptyNamedValueModel(datumName);
        for (ValueModel ratio : ratios) {
            if (ratio.getName().equals(datumName)) {
                retVal = ratio;
            }
        }

        return retVal;
    }

    /**
     * @return the dataCovariancesVarUnct
     */
    public AbstractMatrixModel getDataCovariancesVarUnct() {
        return dataCovariancesVarUnct;
    }

    /**
     * @return the dataCorrelationsVarUnct
     */
    public AbstractMatrixModel getDataCorrelationsVarUnct() {
        return dataCorrelationsVarUnct;
    }

    /**
     * @param updateOnly
     */
    public abstract void initializeNewRatiosAndRhos(boolean updateOnly);

    /**
     * @param name
     * @return
     */
    public BigDecimal getRhoByName(String name) {
        return rhosUnct.get(name);
    }

    /**
     * @param coeffName
     * @return
     */
    public ValueModel getRhoSysUnctByName(String coeffName) {
        BigDecimal myRhoValue = rhosSysUnct.get(coeffName);
        if (null == myRhoValue) {
            myRhoValue = BigDecimal.ZERO;
        }

        return ValueModel.createFullNamedValueModel(//
                coeffName,
                myRhoValue,
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * @return the rhos
     */
    public Map<String, BigDecimal> getRhosVarUnct() {
        return rhosUnct;
    }

    /**
     * @param rhosUnct the rhos to set
     */
    public void setRhosVarUnct(Map<String, BigDecimal> rhosUnct) {
        this.rhosUnct = rhosUnct;
    }

    // backward compatible

    /**
     * @return
     */
    public Map<String, BigDecimal> getRhosVarUnctForXMLSerialization() {
        Map<String, BigDecimal> tightRhos = new HashMap<>();
        for (String key : rhosUnct.keySet()) {
            if (0 != rhosUnct.get(key).compareTo(BigDecimal.ZERO)) {
                tightRhos.put(key, rhosUnct.get(key));
            }
        }

        return tightRhos;
    }

    /**
     * @return
     */
    public Map<String, BigDecimal> getRhosSysUnctForXMLSerialization() {
        Map<String, BigDecimal> tightRhos = new HashMap<>();
        Iterator<String> rhosKeyIterator = rhosSysUnct.keySet().iterator();
        while (rhosKeyIterator.hasNext()) {
            String key = rhosKeyIterator.next();
            if (0 != rhosSysUnct.get(key).compareTo(BigDecimal.ZERO)) {
                tightRhos.put(key, rhosSysUnct.get(key));
            }
        }

        return tightRhos;
    }

    /**
     * @return
     */
    public Map<String, BigDecimal> cloneRhosVarUnct() {

        Map<String, BigDecimal> clonedRhosVarUnct = new HashMap<>();
        rhosUnct.entrySet().stream().forEach((entry) -> clonedRhosVarUnct.put(entry.getKey(), entry.getValue()));

        return clonedRhosVarUnct;
    }

    // XML Serialization *******************************************************

    /**
     * @return
     */
    public Map<String, BigDecimal> cloneRhosSysUnct() {

        Map<String, BigDecimal> clonedRhosSysUnct = new HashMap<>();
        rhosSysUnct.entrySet().stream().forEach((entry) -> {
            clonedRhosSysUnct.put(entry.getKey(), entry.getValue());
        });

        return clonedRhosSysUnct;
    }

    /**
     * @param coeffName
     * @return
     */
    public ValueModel getRhoVarUnctByName(String coeffName) {

        BigDecimal myRhoValue = rhosUnct.get(coeffName);
        if (null == myRhoValue) {
            myRhoValue = BigDecimal.ZERO;
        }

        return ValueModel.createFullNamedValueModel(
                coeffName,
                myRhoValue,
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * @return the rhosSysUnct
     */
    public Map<String, BigDecimal> getRhosSysUnct() {
        return rhosSysUnct;
    }

    /**
     * @param rhosSysUnct the rhosSysUnct to set
     */
    public void setRhosSysUnct(Map<String, BigDecimal> rhosSysUnct) {
        this.rhosSysUnct = rhosSysUnct;
    }

    /**
     * @return the classNameAliasForXML
     */
    public abstract String getClassNameAliasForXML();

    /**
     * @param ratios the ratios to set
     */
    public void setRatios(ValueModel[] ratios) {
        this.ratios = ratios;
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        return getNameAndVersion();
    }

    /**
     *
     */
    protected class DataValueModelNameComparator implements Comparator<ValueModel>, Serializable {

        /**
         *
         */
        public DataValueModelNameComparator() {
        }

        @Override
        public int compare(ValueModel vm1, ValueModel vm2) {
            if (vm1.getName().substring(0, 1).equalsIgnoreCase(vm2.getName().substring(0, 1))) {
                return vm1.compareTo(vm2);
            } else {
                return vm2.compareTo(vm1);
            }
        }
    }
}