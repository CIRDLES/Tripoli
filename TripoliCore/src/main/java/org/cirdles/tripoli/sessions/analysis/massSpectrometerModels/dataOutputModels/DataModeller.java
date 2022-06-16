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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import static java.lang.StrictMath.log;

/**
 * @author James F. Bowring
 */
public class DataModeller {
    public static void modellingTest(MassSpecOutputDataRecord massSpecOutputDataRecord) {
        initializeModelSynth(massSpecOutputDataRecord);
    }

    private static void initializeModelSynth(MassSpecOutputDataRecord massSpecOutputDataRecord) {
        /*
            for m=1:d0.Nfar%+1
                x0.BL(m,1) = mean(d0.data(d0.blflag & d0.det_ind(:,m)));
                x0.BLstd(m,1) = std(d0.data(d0.blflag & d0.det_ind(:,m)));
            end
         */

        double[][] blMeansArray = new double[massSpecOutputDataRecord.faradayCount()][1];
        double[][] blStdArray = new double[massSpecOutputDataRecord.faradayCount()][1];
        for (int faradayIndex = 0; faradayIndex < massSpecOutputDataRecord.faradayCount(); faradayIndex++) {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                if ((massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().get(row, 0) == 1)
                        &&
                        ((massSpecOutputDataRecord.detectorFlagsForRawDataColumn().get(row, faradayIndex) == 1))) {
                    descriptiveStatistics.addValue(massSpecOutputDataRecord.rawDataColumn().get(row, 0));
                }
            }
            blMeansArray[faradayIndex][0] = descriptiveStatistics.getMean();
            blStdArray[faradayIndex][0] = descriptiveStatistics.getStandardDeviation();
        }

        /*
        for m=1:d0.Niso;
            tmpCounts(m,1) = mean(d0.data( (d0.iso_ind(:,m) & d0.axflag)));

            for n = 1:d0.Nblock
                maxtmpCounts(n,m) = max(d0.data( (d0.iso_ind(:,m) & d0.axflag & d0.block(:,n))));
                mintmpCounts(n,m) = min(d0.data( (d0.iso_ind(:,m) & d0.axflag & d0.block(:,n))));
            end

            itmp = (d0.iso_ind(:,m) & ~d0.axflag);
            tmpFar(m,1)  = mean(d0.data(itmp)-x0.BL(d0.det_vec(itmp)));
        end
         */
        double[][] tmpCountsMeanArray = new double[massSpecOutputDataRecord.isotopeCount()][1];
        double[][] maxtmpCountsArray = new double[massSpecOutputDataRecord.blockCount()][massSpecOutputDataRecord.isotopeCount()];
        double[][] mintmpCountsArray = new double[massSpecOutputDataRecord.blockCount()][massSpecOutputDataRecord.isotopeCount()];
        double[][] tmpFaradayMeanArray = new double[massSpecOutputDataRecord.isotopeCount()][1];

        for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                DescriptiveStatistics descriptiveStatisticsA = new DescriptiveStatistics();
                DescriptiveStatistics descriptiveStatisticsB = new DescriptiveStatistics();
                DescriptiveStatistics descriptiveStatisticsC = new DescriptiveStatistics();

                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            &&
                            ((massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 1))) {
                        descriptiveStatisticsA.addValue(massSpecOutputDataRecord.rawDataColumn().get(row, 0));
                    }

                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 1)
                            &&
                            (massSpecOutputDataRecord.blockIndicesForRawDataColumn().get(row, 0) == (blockIndex + 1))) {
                        descriptiveStatisticsB.addValue(massSpecOutputDataRecord.rawDataColumn().get(row, 0));
                    }


                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            &&
                            ((massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 0))) {
                        descriptiveStatisticsC.addValue(
                                massSpecOutputDataRecord.rawDataColumn().get(row, 0)
                                        - blMeansArray[(int) (massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) - 1.0)][0]);
                    }
                }
                tmpCountsMeanArray[isotopeIndex][0] = descriptiveStatisticsA.getMean();
                maxtmpCountsArray[blockIndex][isotopeIndex] = descriptiveStatisticsB.getMax();
                mintmpCountsArray[blockIndex][isotopeIndex] = descriptiveStatisticsB.getMin();
                tmpFaradayMeanArray[isotopeIndex][0] = descriptiveStatisticsC.getMean();
            }
        }
        /*
            [~,imaxC] = max(tmpCounts);
            iden = d0.Niso;
            x0.DFgain = tmpCounts(imaxC)/tmpFar(imaxC);
            for m=1:d0.Niso
                x0.lograt(m,1) = log(tmpCounts(m)/tmpCounts(iden));
            end
         */
        // java array is 0-based
        int imaxC = tmpCountsMeanArray.length - 1;
        int iden = massSpecOutputDataRecord.isotopeCount() - 1;
        double dfGain = tmpCountsMeanArray[imaxC][0] / tmpFaradayMeanArray[imaxC][0];
        double[][] logRatios = new double[ massSpecOutputDataRecord.isotopeCount()][1];
        for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
            logRatios[isotopeIndex][0] = log(tmpCountsMeanArray[isotopeIndex][0]/tmpCountsMeanArray[iden][0]);
        }

    }
}