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
        this.XMLSchemaURL = "";
        this.modelName = DEFAULT_OBJECT_NAME;
        this.versionNumber = 1;
        this.minorVersionNumber = 0;
        this.labName = DEFAULT_OBJECT_NAME;

        // TODO: switch to https://www.baeldung.com/java-8-date-time-intro
        //  this.dateCertified = DateHelpers.defaultEarthTimeDateString();

        this.reference = "None";
        this.comment = "None";

        this.ratios = new ValueModel[0];
        this.rhosUnct = new HashMap<>();
        this.rhosSysUnct = new HashMap<>();
        this.dataCovariancesVarUnct = new CovarianceMatrixModel();
        this.dataCorrelationsVarUnct = new CorrelationMatrixModel();
        this.dataCovariancesSysUnct = new CovarianceMatrixModel();
        this.dataCorrelationsSysUnct = new CorrelationMatrixModel();
        this.immutable = false;
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
        myModel.setModelName(myModel.getModelName() + (doAppendName ? "-COPY" : ""));

        myModel.initializeModel();

        return myModel;
    }

    /**
     * @return
     */
    public abstract AbstractRatiosDataModel cloneModel();

    /**
     * compares this <code>AbstractRatiosDataModel</code> to argument
     * <code>AbstractRatiosDataModel</code> by their <code>name</code> and
     * <code>version</code>.
     *
     * @param model
     * @return <code>int</code> - 0 if this
     * <code>AbstractRatiosDataModel</code>'s <code>name</code> and
     * <code>version</code> is the same as argument
     * <code>AbstractRatiosDataModel</code>'s, -1 if they are lexicographically
     * less than argument <code>AbstractRatiosDataModel</code>'s, and 1 if they
     * are greater than argument <code>AbstractRatiosDataModel</code>'s
     * @throws java.lang.ClassCastException a ClassCastException
     * @pre argument <code>AbstractRatiosDataModel</code> is a valid
     * <code>AbstractRatiosDataModel</code>
     * @post returns an <code>int</code> representing the comparison between
     * this <code>AbstractRatiosDataModel</code> and argument
     * <code>AbstractRatiosDataModel</code>
     */
    @Override
    public int compareTo(AbstractRatiosDataModel model) throws ClassCastException {
        String modelID = model.getNameAndVersion().trim();
        return (this.getNameAndVersion().trim() //
                .compareToIgnoreCase(modelID));
    }

    /**
     * compares this <code>AbstractRatiosDataModel</code> to argument
     * <code>AbstractRatiosDataModel<?code> by their <code>name</code> and
     * <code>version</code>.
     *
     * @param model
     * @return <code>boolean</code> - <code>true</code> if argument      <code>
     * AbstractRatiosDataModel</code> is this
     * <code>AbstractRatiosDataModel</code> or their <code>name</code> and
     * <code>version</code> are identical, else <code>false</code>
     * @pre argument <code>AbstractRatiosDataModel</code> is a valid
     * <code>AbstractRatiosDataModel</code>
     * @post returns a <code>boolean</code> representing the equality of this
     * <code>AbstractRatiosDataModel</code> and argument
     * <code>AbstractRatiosDataModel</code> based on their <code>name</code> and
     * <code>version</code>
     */
    @Override
    public boolean equals(Object model) {
        //check for self-comparison
        if (this == model) {
            return true;
        }
        if (!(model instanceof AbstractRatiosDataModel)) {
            return false;
        }

        AbstractRatiosDataModel myModel = (AbstractRatiosDataModel) model;
        return (this.getNameAndVersion().trim().compareToIgnoreCase( //
                myModel.getNameAndVersion().trim()) == 0);
    }

    /**
     * returns 0 as the hashcode for this <code>AbstractRatiosDataModel</code>.
     * Implemented to meet equivalency requirements as documented by
     * <code>java.lang.Object</code>
     *
     * @return <code>int</code> - 0
     * @pre this <code>AbstractRatiosDataModel</code> exists
     * @post hashcode of 0 is returned for this
     * <code>AbstractRatiosDataModel</code>
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
        for (int i = 0; i < dataIncoming.length; i++) {
            ValueModel ratio = ValueModel.createCopyOfValueModel(getDatumByName(dataIncoming[i].getName()));
        }

        // introduce special comparator that puts concentrations (conc) after dataIncoming)//dec 2014 not needed
        Arrays.sort(this.ratios, new DataValueModelNameComparator());

        if (myRhos == null) {
            buildRhosMap();
        } else {
            for (String key : myRhos.keySet()) {
                String rhoName = key;
                if (rhosUnct.get(rhoName) != null) {
                    rhosUnct.put(rhoName, myRhos.get(rhoName));
                }
            }
        }

        if (myRhosSysUnct == null) {
            buildRhosSysUnctMap();
        } else {
            for (String key : myRhosSysUnct.keySet()) {
                String rhoName = key;
                if (rhosSysUnct.get(rhoName) != null) {
                    rhosSysUnct.put(rhoName, myRhosSysUnct.get(rhoName));
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
        if (rhosUnct == null) {
            buildRhosMap();
        }

        for (String rhoName : rhosUnct.keySet()) {
            rhosUnct.put(rhoName, new BigDecimal(((CorrelationMatrixModel) dataCorrelationsVarUnct).getCorrelationCell(rhoName)));
        }

        // sept 2014 backwards compat
        if (rhosSysUnct == null) {
            buildRhosSysUnctMap();
        }

        for (String rhoName : rhosSysUnct.keySet()) {
            rhosSysUnct.put(rhoName, new BigDecimal(((CorrelationMatrixModel) dataCorrelationsSysUnct).getCorrelationCell(rhoName)));
        }
    }

    /**
     * @param checkCovarianceValidity the value of checkCovarianceValidity
     * @throws TripoliException
     */
    public void saveEdits(boolean checkCovarianceValidity)
            throws TripoliException {
        if ((dataCorrelationsVarUnct != null) || (dataCorrelationsSysUnct != null)) {
            generateBothUnctCovarianceMFromEachUnctCorrelationM();

            copyBothRhosFromEachCorrelationM();

            if (checkCovarianceValidity && !dataCovariancesVarUnct.isCovMatrixSymmetricAndPositiveDefinite()) {
                throw new TripoliException("Var Unct Correlations yield Var Unct covariance matrix NOT positive definite.");
            }
            if (checkCovarianceValidity && !dataCovariancesSysUnct.isCovMatrixSymmetricAndPositiveDefinite()) {
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
                                    / rowData.getOneSigmaAbs().doubleValue() //
                                    / colData.getOneSigmaAbs().doubleValue();
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
                                    / rowData.getOneSigmaSysAbs().doubleValue()
                                    / colData.getOneSigmaSysAbs().doubleValue();
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
                                    * rowData.getOneSigmaAbs().doubleValue() //
                                    * colData.getOneSigmaAbs().doubleValue();
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
                                    * rowData.getOneSigmaSysAbs().doubleValue() //
                                    * colData.getOneSigmaSysAbs().doubleValue();
                    dataCovariancesSysUnct.setValueAt(row, col, covariance);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * gets the <code>modelName</code> of this
     * <code>AbstractRatiosDataModel</code>.
     *
     * @return <code>String</code> - this <code>AbstractRatiosDataModel</code>'s
     * <code>modelName</code>
     * @pre this <code>AbstractRatiosDataModel</code> exists
     * @post returns this <code>AbstractRatiosDataModel</code>'s
     * <code>modelName</code>
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
     * gets the <code>versionNumber</code> of this
     * <code>AbstractRatiosDataModel</code>.
     *
     * @return <code>int</code> - this <code>AbstractRatiosDataModel</code>'s
     * <code>versionNumber</code>
     * @pre this <code>AbstractRatiosDataModel</code> exists
     * @post returns this <code>AbstractRatiosDataModel</code>'s
     * <code>versionNumber</code>
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
     * gets the <code>labName</code> of this
     * <code>AbstractRatiosDataModel</code>.
     *
     * @return <code>String</code> - this <code>AbstractRatiosDataModel</code>'s
     * <code>labName</code>
     * @pre this <code>AbstractRatiosDataModel</code> exists
     * @post returns this <code>AbstractRatiosDataModel</code>'s
     * <code>labName</code>
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
     * gets the <code>dateCertified</code> of this
     * <code>AbstractRatiosDataModel</code>.
     *
     * @return <code>String</code> - this <code>AbstractRatiosDataModel</code>'s
     * <code>dateCertified</code>
     * @pre this <code>AbstractRatiosDataModel</code> exists
     * @post returns this <code>AbstractRatiosDataModel</code>'s
     * <code>dateCertified</code>
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
     * gets a <code>String</code> containing this
     * <code>AbstractRatiosDataModel</code>'s <code>modelName</code> and
     * <code>versionNumber</code>.
     *
     * @return <code>String</code> - this <code>AbstractRatiosDataModel</code>'s
     * <code>modelName</code> and <code>versionNumber</code>
     * @pre this <code>AbstractRatiosDataModel</code> exists
     * @post returns a <code>String</code> containing this
     * <code>AbstractRatiosDataModel</code>'s <code>modelName</code> and
     * <code>versionNumber</code>
     */
    public String getNameAndVersion() {
        return makeNameAndVersion(modelName, versionNumber, minorVersionNumber);
    }

    /**
     * gets the <code>ratios</code> of this
     * <code>AbstractRatiosDataModel</code>.
     *
     * @return <code>ValueModel[]</code> - collection of this
     * <code>AbstractRatiosDataModel</code>'s <code>ratios</code>
     * @pre this <code>AbstractRatiosDataModel</code> exists
     * @post returns this <code>AbstractRatiosDataModel</code>'s
     * <code>ratios</code>
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
            clonedData[i] =  ValueModel.createCopyOfValueModel(ratios[i]);
        }
        return clonedData;
    }

    /**
     * gets a single ratio from this <code>AbstractRatiosDataModel</code>'s
     * <code>ratios</code> specified by argument <code>datumName</code>. Returns
     * a new, empty      <code>
     * ValueModel</code> if no matching ratio is found.
     *
     * @param datumName name of the ratio to search for
     * @return <code>ValueModel</code> - ratio found in <code>ratios</code>
     * whose name matches argument <code>datumName</code> or a new      <code>
     * ValueModel</code> if no match is found
     * @pre argument <code>datumName</code> is a valid <code>String</code>
     * @post returns the <code>ValueModel</code> found in this
     * <code>AbstractRatiosDataModel</code>'s <code>ratios</code> whose name
     * matches argument <code>datumName</code>
     */
    public ValueModel getDatumByName(String datumName) {

        ValueModel retVal = ValueModel.createEmptyNamedValueModel(datumName);
        for (int i = 0; i < ratios.length; i++) {
            if (ratios[i].getName().equals(datumName)) {
                retVal = ratios[i];
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
        if (myRhoValue == null) {
            myRhoValue = BigDecimal.ZERO;
        }

        ValueModel coeffModel
                = ValueModel.createFullNamedValueModel(//
                coeffName,
                myRhoValue,
                BigDecimal.ZERO, BigDecimal.ZERO);

        return coeffModel;
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
        Iterator<String> rhosKeyIterator = rhosUnct.keySet().iterator();
        while (rhosKeyIterator.hasNext()) {
            String key = rhosKeyIterator.next();
            if (rhosUnct.get(key).compareTo(BigDecimal.ZERO) != 0) {
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
            if (rhosSysUnct.get(key).compareTo(BigDecimal.ZERO) != 0) {
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
        rhosUnct.entrySet().stream().forEach((entry) -> {
            clonedRhosVarUnct.put(entry.getKey(), entry.getValue());
        });

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
        if (myRhoValue == null) {
            myRhoValue = BigDecimal.ZERO;
        }

        ValueModel coeffModel
                = ValueModel.createFullNamedValueModel(
                coeffName,
                myRhoValue,
                BigDecimal.ZERO, BigDecimal.ZERO);

        return coeffModel;
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
//
//    /**
//     * @return
//     */
//    protected XStream getXStream() {
//
//        XStream xstream = new XStream(new DomDriver());
//
//        customizeXstream(xstream);
//
//        // clear out existing permissions and set own ones
//        xstream.addPermission(NoTypePermission.NONE);
//        // allow some basics
//        xstream.addPermission(NullPermission.NULL);
//        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
//        xstream.allowTypeHierarchy(Collection.class);
//        xstream.addPermission(AnyTypePermission.ANY);
//
//
//        return xstream;
//    }
//    // END XML Serialization ***************************************************
//
//    /**
//     * registers converter for argument <code>xstream</code> and sets aliases to
//     * make the XML file more human-readable
//     *
//     * @param xstream <code>XStream</code> to be customized
//     * @pre argument <code>xstream</code> is a valid <code>XStream</code>
//     * @post argument <code>xstream</code> is customized to produce a cleaner
//     * output <code>file</code>
//     */
//    protected abstract void customizeXstream(XStream xstream);
//
//    /**
//     * sets the XML schema. Initializes <code>UPbReduxConfigurator</code> and
//     * sets the location of the XML Schema
//     *
//     * @param resourceURI
//     * @pre <code>UPbReduxConfigurator</code> class is available
//     * @post <code>AbstractRatiosDataModelXMLSchemaURL</code> will be set
//     */
//    protected void setClassXMLSchemaURL(String resourceURI) {
//        UPbReduxConfigurator myConfigurator = new UPbReduxConfigurator();
//
//        XMLSchemaURL
//                = myConfigurator.getResourceURI(resourceURI);
//    }
//
//    /**
//     * encodes this <code>AbstractRatiosDataModel</code> to the
//     * <code>file</code> specified by the argument <code>filename</code>
//     *
//     * @param filename location to store data to
//     * @pre this <code>AbstractRatiosDataModel</code> exists
//     * @post this <code>AbstractRatiosDataModel</code> is stored in the
//     * specified XML <code>file</code>
//     */
//    @Override
//    public void serializeXMLObject(String filename) {
//        XStream xstream = getXStream();
//
//        String xml = xstream.toXML(this);
//
//        xml = ReduxConstants.XML_Header + xml;
//
//        xml = xml.replaceFirst(this.getClassNameAliasForXML(),
//                this.getClassNameAliasForXML() + "  "//
//                        + ReduxConstants.XML_ResourceHeader//
//                        + XMLSchemaURL//
//                        + "\"");
//
//        try {
//            FileWriter outFile = new FileWriter(filename);
//            PrintWriter out = new PrintWriter(outFile);
//
//            // Write xml to file
//            out.println(xml);
//            out.flush();
//            out.close();
//            outFile.close();
//
//        } catch (IOException e) {
//        }
//    }
//
//    /**
//     * decodes <code>AbstractRatiosDataModel</code> from <code>file</code>
//     * specified by argument <code>filename</code>
//     *
//     * @param filename   location to read data from
//     * @param doValidate
//     * @return <code>Object</code> - the <code>AbstractRatiosDataModel</code>
//     * created from the specified XML <code>file</code>
//     * @throws TripoliException
//     * @throws java.io.FileNotFoundException
//     * @pre <code>filename</code> references an XML <code>file</code>
//     * @post <code>AbstractRatiosDataModel</code> stored in
//     * <code>filename</code> is returned
//     */
//    @Override
//    public AbstractRatiosDataModel readXMLObject(String filename, boolean doValidate)
//            throws FileNotFoundException, TripoliException {
//        AbstractRatiosDataModel myModelClassInstance = null;
//
////        BufferedReader reader = URIHelper.getBufferedReader(filename);
//
////        if (reader != null) {
////            XStream xstream = getXStream();
////
//        boolean isValidOrAirplaneMode = true;
////
////            if (doValidate) {
////                isValidOrAirplaneMode = URIHelper.validateXML(reader, filename, XMLSchemaURL);
////            }
//
//        if (isValidOrAirplaneMode) {
////                // re-create reader
////                reader = URIHelper.getBufferedReader(filename);
////                try {
////                    myModelClassInstance = (AbstractRatiosDataModel) xstream.fromXML(reader);
////                    myModelClassInstance.initializeModel();
////                } catch (ConversionException e) {
////                    throw new TripoliException(null, e.getMessage());
////                }
//
////            } else {
////                throw new TripoliException(null, "XML data file does not conform to schema.");
////            }
//        } else {
//            throw new FileNotFoundException("Missing XML data file.");
//        }
//
//        return myModelClassInstance;
//    }

    /**
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
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
    protected class DataValueModelNameComparator implements Comparator<ValueModel> {

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