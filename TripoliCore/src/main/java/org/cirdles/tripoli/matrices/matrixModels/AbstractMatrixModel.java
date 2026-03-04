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

package org.cirdles.tripoli.matrices.matrixModels;

import jama.Matrix;

import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author James F. Bowring
 */
public abstract class AbstractMatrixModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 19645743150111042L;
    /**
     *
     */
    protected String levelName;
    /**
     *
     */
    protected Map<Integer, String> rows;
    /**
     *
     */
    protected Map<String, Integer> cols;
    /**
     *
     */
    protected Matrix matrix;

    /**
     * @param levelName name of internal matrix
     */
    protected AbstractMatrixModel(
            String levelName) {
        this.levelName = levelName;
        rows = new HashMap<>();
        cols = new HashMap<>();
        matrix = null;
    }

    /**
     * @param rowMap maps row indices to their names
     * @return map of columns with rownames and keys
     */
    public static Map<String, Integer> invertRowMap(Map<Integer, String> rowMap) {
        Map<String, Integer> myCols = new HashMap<>();
        for (Integer key : rowMap.keySet()) {
            myCols.put(rowMap.get(key), key);
        }

        return myCols;
    }

    /**
     * Inverts column map (Name, col number) to row map (number, Name).
     *
     * @param colMap
     * @return
     */
    public static Map<Integer, String> invertColMap(Map<String, Integer> colMap) {
        Map<Integer, String> myRows = new HashMap<>();
        for (String key : colMap.keySet()) {
            myRows.put(colMap.get(key), key);
        }

        return myRows;
    }

    /**
     * @return
     */
    public abstract AbstractMatrixModel copy();

    /**
     * @param row
     * @param col
     * @param value
     */
    public abstract void setValueAt(int row, int col, double value);

    /**
     * @param matrixModel
     */
    public void copyValuesFrom(AbstractMatrixModel matrixModel) {

        rows = matrixModel.rows;
        cols = matrixModel.cols;
        matrix = matrixModel.matrix.copy();
    }

    /**
     * @return
     */
    public boolean isCovMatrixSymmetricAndPositiveDefinite() {
        try {
            return !matrix.chol().isSPD();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * @return
     */
    public String ToStringWithLabels() {
        String formatCell = "%1$-23s";

        StringBuilder retVal = new StringBuilder(String.format(formatCell, "MATRIX#=" + levelName));

        // make an inverse map of columns
        Map<Integer, String> tempCols = new HashMap<>();
        for (String colKey : cols.keySet()) {
            tempCols.put(cols.get(colKey), colKey);
        }

        for (int i = 0; i < tempCols.size(); i++) {
            retVal.append(String.format(formatCell, tempCols.get(i)));
        }

        retVal.append("\n");

        NumberFormat formatter = new DecimalFormat("0.000000000E00", new DecimalFormatSymbols(Locale.ENGLISH));

        for (int row = 0; row < rows.size(); row++) {
            retVal.append(String.format(formatCell, rows.get(row)));
            try {
                for (int col = 0; col < matrix.getColumnDimension(); col++) {
                    retVal.append(String.format(formatCell, formatter.format(matrix.get(row, col))));
                }
            } catch (Exception e) {
            }
            retVal.append("\n");
        }

        return retVal.toString();
    }

    /**
     * @return the rows
     */
    public Map<Integer, String> getRows() {
        return rows;
    }

    /**
     * @param rowNames
     */
    public void setRows(String[] rowNames) {
        Map<Integer, String> myRows = new HashMap<>();
        for (int i = 0; i < rowNames.length; i++) {
            myRows.put(i, rowNames[i]);
        }
        rows = myRows;
    }

    /**
     * @param myRows
     */
    public void setRows(Map<Integer, String> myRows) {
        rows = myRows;
    }

    /**
     * @return the cols
     */
    public Map<String, Integer> getCols() {
        return cols;
    }

    /**
     * @param rowMap
     */
    public void setCols(Map<Integer, String> rowMap) {
        cols = invertRowMap(rowMap);
    }

    /**
     * @param colMap
     */
    public void copyCols(Map<String, Integer> colMap) {
        cols = colMap;
    }

    /**
     * @return the levelName
     */
    public String getLevelName() {
        return levelName;
    }

    /**
     * @param levelName the levelName to set
     */
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    /**
     * @return the matrix
     */
    public Matrix getMatrix() {
        return matrix;
    }

    /**
     * @param matrix the matrix to set
     */
    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    /**
     *
     */
    public void initializeMatrix() {
        try {
            matrix = new Matrix(new double[rows.size()][cols.size()]);
        } catch (Exception e) {
            //Jan 2015 jama won't take 0,0
            matrix = new Matrix(new double[1][1]);
        }
    }

    /**
     * @return
     */
    public double[] sumOfRowsMatrix() {
        double[] retval = new double[matrix.getRowDimension()];

        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                retval[i] += matrix.get(i, j);
            }
        }

        return retval;
    }

    /**
     * @param parentModel
     */
    public void initializeMatrixModelFromMatrixModel(AbstractMatrixModel parentModel) {
        // requires that rows be identical; we are slicing out columns
        boolean retVal = !(rows.isEmpty() || cols.isEmpty());
        if (retVal) {
            initializeMatrix();
            for (String colName : parentModel.cols.keySet()) {
                Integer col = cols.get(colName);
                if (null != col) {
                    //copy values from this column
                    matrix.setMatrix(0, matrix.getRowDimension() - 1, new int[]{col}, parentModel.matrix.getMatrix(0, matrix.getRowDimension() - 1, new int[]{parentModel.cols.get(colName)}));
                }
            }
        }
    }

    /**
     * @param variableName
     * @return
     */
    protected String createPartialDerivName(String variableName) {
        return "d" + variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
    }

//    private void readObject ( ObjectInputStream stream ) throws IOException,
//            ClassNotFoundException {
//        stream.defaultReadObject();
//
//        ObjectStreamClass myObject = ObjectStreamClass.lookup(
//                Class.forName( AbstractMatrixModel.class.getCanonicalName()) );
//        long theSUID = myObject.getSerialVersionUID();
//
//        System.err.println( "Customized De-serialization of AbstractMatrixModel "
//                + theSUID );
//    }
}