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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.rjmcmc;

import org.ojalgo.matrix.store.MatrixStore;

/**
 * Matlab code >> here
 * d0.data >> rawDataColumn
 * d0.time >> timeColumn
 * d0.time_ind >> timeIndColumn
 * d0.sig_ind >> signalIndicesForRawDataColumn
 * d0.block >> blockIndicesForRawDataColumn (1-based block number for sequence data, BaseLine data is set to block 0)
 * d0.iso_vec >> isotopeIndicesForRawDataColumn (isotopes are indexed starting at 1)
 * d0.iso_ind >> isotopeFlagsForRawDataColumn (each isotope has a column and a 1 denotes it is being read)
 * d0.det_vec >> detectorIndicesForRawDataColumn (detectors are indexed from 1 through all Faraday and the last is the Axial (Daly)))
 * d0.det_ind >> detectorFlagsForRawDataColumn (each Faraday has a column and the last column is for Daly; 1 flags detector used)
 * d0.blflag >> baseLineFlagsForRawDataColumn (contains 1 for baseline, 0 for sequence)
 * d0.axflag >> axialFlagsForRawDataColumn (contains 1 for data from DALY detector, 0 otherwise)
 * d0.InterpMat >> firstBlockInterpolationsMatrix  (matlab actually puts matrices into cells)
 * d0.Nfar >> faradayCount
 * d0.Niso >> isotopeCount
 * d0.Nblock >> blockCount
 *
 * @param rawDataColumn
 * @param timeColumn
 * @param timeIndColumn
 * @param signalIndicesForRawDataColumn
 * @param blockIndicesForRawDataColumn
 * @param isotopeIndicesForRawDataColumn
 * @param isotopeFlagsForRawDataColumn
 * @param detectorIndicesForRawDataColumn
 * @param detectorFlagsForRawDataColumn
 * @param baseLineFlagsForRawDataColumn
 * @param axialFlagsForRawDataColumn
 * @param allBlockInterpolations
 * @param faradayCount
 * @param isotopeCount
 * @param blockCount
 * @param nCycleArray
 */
public record MassSpecOutputDataRecord(
        double[] rawDataColumn,
        double[] timeColumn,
        double[] timeIndColumn,
        double[] signalIndicesForRawDataColumn,
        double[] blockIndicesForRawDataColumn,
        double[] isotopeIndicesForRawDataColumn,
        double[][] isotopeFlagsForRawDataColumn,
        double[] detectorIndicesForRawDataColumn,
        double[][] detectorFlagsForRawDataColumn,
        double[] baseLineFlagsForRawDataColumn,
        double[] axialFlagsForRawDataColumn,
        java.util.ArrayList<MatrixStore<Double>> allBlockInterpolations,
        int faradayCount,
        int isotopeCount,
        int blockCount,
        int[] nCycleArray
) {

}