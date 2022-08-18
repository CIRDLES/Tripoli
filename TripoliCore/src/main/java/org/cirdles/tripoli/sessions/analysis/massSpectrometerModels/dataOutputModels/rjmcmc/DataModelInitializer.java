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

// import jama.Matrix;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.InverterTask;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;

/**
 * @author James F. Bowring
 */
public class DataModelInitializer {
    public static DataModellerOutputRecord modellingTest(MassSpecOutputDataRecord massSpecOutputDataRecord) {
        // return initializeModelSynth(massSpecOutputDataRecord);
        try {
            return initializeModelSynth(massSpecOutputDataRecord);
        } catch (RecoverableCondition e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DataModellerOutputRecord initializeModelSynth(MassSpecOutputDataRecord massSpecOutputDataRecord) throws RecoverableCondition {
        /*
            for m=1:d0.Nfar%+1
                x0.BL(m,1) = mean(d0.data(d0.blflag & d0.det_ind(:,m)));
                x0.BLstd(m,1) = std(d0.data(d0.blflag & d0.det_ind(:,m)));
            end
         */

        // double[][] blMeansArray = new double[massSpecOutputDataRecord.faradayCount()][1];
        double[] blMeansArray = new double[massSpecOutputDataRecord.faradayCount()];
        // double[][] blStdArray = new double[massSpecOutputDataRecord.faradayCount()][1];
        double[] blStdArray = new double[massSpecOutputDataRecord.faradayCount()];
        for (int faradayIndex = 0; faradayIndex < massSpecOutputDataRecord.faradayCount(); faradayIndex++) {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            /*
            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                if ((massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().get(row, 0) == 1)
                        &&
                        ((massSpecOutputDataRecord.detectorFlagsForRawDataColumn().get(row, faradayIndex) == 1))) {
                    descriptiveStatistics.addValue(massSpecOutputDataRecord.rawDataColumn().get(row, 0));
                }
            }
            */
            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                if ((massSpecOutputDataRecord.baseLineFlagsForRawDataColumn()[row] == 1)
                        &&
                        ((massSpecOutputDataRecord.detectorFlagsForRawDataColumn()[row][faradayIndex] == 1))) {
                    descriptiveStatistics.addValue(massSpecOutputDataRecord.rawDataColumn()[row]);
                }
            }
            blMeansArray[faradayIndex] = descriptiveStatistics.getMean();
            blStdArray[faradayIndex] = descriptiveStatistics.getStandardDeviation();
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
//        double[][] maxtmpCountsArray = new double[massSpecOutputDataRecord.blockCount()][massSpecOutputDataRecord.isotopeCount()];
//        double[][] mintmpCountsArray = new double[massSpecOutputDataRecord.blockCount()][massSpecOutputDataRecord.isotopeCount()];
        double[][] tmpFaradayMeanArray = new double[massSpecOutputDataRecord.isotopeCount()][1];
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                DescriptiveStatistics descriptiveStatisticsA = new DescriptiveStatistics();
                DescriptiveStatistics descriptiveStatisticsB = new DescriptiveStatistics();
                DescriptiveStatistics descriptiveStatisticsC = new DescriptiveStatistics();

                /*
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 1)) {
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
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 0)) {
                        descriptiveStatisticsC.addValue(
                                massSpecOutputDataRecord.rawDataColumn().get(row, 0)
                                        - blMeansArray[(int) (massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) - 1.0)][0]);
                    }
                }
                */
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 1)) {
                        descriptiveStatisticsA.addValue(massSpecOutputDataRecord.rawDataColumn()[row]);
                    }

                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 1)
                            &&
                            (massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1))) {
                        descriptiveStatisticsB.addValue(massSpecOutputDataRecord.rawDataColumn()[row]);
                    }


                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 0)) {
                        descriptiveStatisticsC.addValue(
                                massSpecOutputDataRecord.rawDataColumn()[row]
                                        - blMeansArray[(int) (massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1.0)]);
                    }
                }
                tmpCountsMeanArray[isotopeIndex][0] = descriptiveStatisticsA.getMean();
//                maxtmpCountsArray[blockIndex][isotopeIndex] = descriptiveStatisticsB.getMax();
//                mintmpCountsArray[blockIndex][isotopeIndex] = descriptiveStatisticsB.getMin();
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
        // double[][] logRatios = new double[massSpecOutputDataRecord.isotopeCount()][1];
        double[] logRatios = new double[massSpecOutputDataRecord.isotopeCount()];
        for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
            // logRatios[isotopeIndex][0] = log(tmpCountsMeanArray[isotopeIndex][0] / tmpCountsMeanArray[iden][0]);
            logRatios[isotopeIndex] = log(tmpCountsMeanArray[isotopeIndex][0] / tmpCountsMeanArray[iden][0]);
        }

        /*
        for m=1:d0.Nblock
            II = d0.InterpMat{m};
            dind = ( d0.axflag & d0.block(:,m));
            dd=d0.data(dind)./exp(x0.lograt(d0.iso_vec(dind)));
            [~,dsort]=sort(d0.time_ind(dind));
            dd=dd(dsort);
            I0=(II'*II)^-1*II'*dd;
            x0.I{m} = I0;
        end
         */
        // just playing with first block for now
        // Matrix IO = null;
        // MatrixStore<Double> IO = null;
        double[] IO = null;
        for (int blockIndex = 0; blockIndex < 1; blockIndex++) {
            // Matrix interpolatedKnotData = massSpecOutputDataRecord.firstBlockInterpolations();
            MatrixStore<Double> interpolatedKnotData = massSpecOutputDataRecord.firstBlockInterpolations();
//            double[][] dind = new double[massSpecOutputDataRecord.rawDataColumn().getRowDimension()][1];
            List<Double> dd = new ArrayList<>();
            List<Double> timeIndForSorting = new ArrayList<>();
            /*
            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                if ((massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 1)
                        &&
                        (massSpecOutputDataRecord.blockIndicesForRawDataColumn().get(row, 0) == (blockIndex + 1))) {
//                    dind[row][0] = 1;
                    dd.add(massSpecOutputDataRecord.rawDataColumn().get(row, 0)
                            / exp(logRatios[(int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn().get(row, 0) - 1][0]));
                    // convert to 0-based index
                    timeIndForSorting.add(massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1);
                }
            }
            */
            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                if ((massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 1)
                        &&
                        (massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1))) {
//                    dind[row][0] = 1;
                    dd.add(massSpecOutputDataRecord.rawDataColumn()[row]
                            / exp(logRatios[(int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] - 1]));
                    // convert to 0-based index
                    timeIndForSorting.add(massSpecOutputDataRecord.timeIndColumn()[row] - 1);
                }
            }
            double[] ddArray = dd.stream().mapToDouble(d -> d).toArray();

            // get indices used in sorting per Matlab [~,dsort]=sort(d0.time_ind(dind));
            double[] timeIndForSortingArray = timeIndForSorting.stream().mapToDouble(d -> d).toArray();
            ArrayIndexComparator comparator = new ArrayIndexComparator(timeIndForSortingArray);
            Integer[] dsortIndices = comparator.createIndexArray();
            Arrays.sort(dsortIndices, comparator);

            double[] ddSortedArray = new double[ddArray.length];
            for (int i = 0; i < ddArray.length; i++) {
                ddSortedArray[i] = ddArray[dsortIndices[i]];
            }

            // Matrix ddMatrix = new Matrix(ddSortedArray, ddSortedArray.length);
            // IO = (interpolatedKnotData.transpose().times(interpolatedKnotData)).inverse()
            //         .times(interpolatedKnotData.transpose()).times(ddMatrix);

            MatrixStore<Double> ddMatrix = storeFactory.columns(ddSortedArray);
            MatrixStore<Double> tempMatrix = interpolatedKnotData.transpose().multiply(interpolatedKnotData);
            MatrixStore<Double> tempMatrix2;
            InverterTask<Double> inverter = InverterTask.PRIMITIVE.make(tempMatrix, false, false);
            tempMatrix2 = inverter.invert(tempMatrix);
            IO = tempMatrix2.multiply(interpolatedKnotData.transpose()).multiply(ddMatrix).toRawCopy1D();
        }
            /*
                %%% MODEL DATA WITH INITIAL MODEL
                II = d0.InterpMat;

                for m=1:d0.Nfar%+1
                    d(d0.blflag & d0.det_ind(:,m),1) = x0.BL(m);  blMeansArray
                end

                for n = 1:d0.Nblock
                    Intensity{n} = II{n}*x0.I{n};
                    for m=1:d0.Niso;
                        itmp = d0.iso_ind(:,m) & d0.axflag & d0.block(:,n);
                        d(itmp) = exp(x0.lograt(m))*Intensity{n}(d0.time_ind(itmp));

                        itmp = d0.iso_ind(:,m) & ~d0.axflag & d0.block(:,n);
                        d(itmp) = exp(x0.lograt(m))*x0.DFgain^-1 *Intensity{n}(d0.time_ind(itmp)) + x0.BL(d0.det_vec(itmp));
                    end
                end
             */

        /*
        double[] dataArray = new double[massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().getRowDimension()];
        for (int faradayIndex = 0; faradayIndex < massSpecOutputDataRecord.faradayCount(); faradayIndex++) {
            for (int row = 0; row < massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().getRowDimension(); row++) {
                if ((massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().get(row, 0) == 1)
                        &&
                        (massSpecOutputDataRecord.detectorFlagsForRawDataColumn().get(row, faradayIndex) == 1)) {
                    dataArray[row] = blMeansArray[faradayIndex][0];
                }
            }
        }
        */
        double[] dataArray = new double[massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().length];
        for (int faradayIndex = 0; faradayIndex < massSpecOutputDataRecord.faradayCount(); faradayIndex++) {
            for (int row = 0; row < massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().length; row++) {
                if ((massSpecOutputDataRecord.baseLineFlagsForRawDataColumn()[row] == 1)
                        &&
                        (massSpecOutputDataRecord.detectorFlagsForRawDataColumn()[row][faradayIndex] == 1)) {
                    dataArray[row] = blMeansArray[faradayIndex];
                }
            }
        }

        /*
        Matrix[] intensityPerBlock = new Matrix[1];
        for (int blockIndex = 0; blockIndex < 1; blockIndex++) {
            intensityPerBlock[blockIndex] = massSpecOutputDataRecord.firstBlockInterpolations().times(IO);
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().getRowDimension(); row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 1)
                            &&
                            (massSpecOutputDataRecord.blockIndicesForRawDataColumn().get(row, 0) == (blockIndex + 1))) {
                        dataArray[row] = exp(logRatios[isotopeIndex][0])
                                * intensityPerBlock[blockIndex].get((int) massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1, 0);
                    }
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 0)
                            &&
                            (massSpecOutputDataRecord.blockIndicesForRawDataColumn().get(row, 0) == (blockIndex + 1))) {
                        dataArray[row] = exp(logRatios[isotopeIndex][0]) / dfGain
                                * intensityPerBlock[blockIndex].get((int) massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1, 0)
                                + blMeansArray[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) - 1][0];
                    }
                }
            }
        }
        */
        // ArrayList<MatrixStore<Double>> intensityPerBlock = new ArrayList<>(1);
        ArrayList<double []> intensityPerBlock = new ArrayList<>(1);
        for (int blockIndex = 0; blockIndex < 1; blockIndex++) {
            MatrixStore<Double> intensity = massSpecOutputDataRecord.firstBlockInterpolations().multiply(storeFactory.columns(IO));
            intensityPerBlock.add(blockIndex, intensity.toRawCopy1D());
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().length; row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 1)
                            &&
                            (massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1))) {
                        dataArray[row] = exp(logRatios[isotopeIndex])
                                * intensity.get((int) massSpecOutputDataRecord.timeIndColumn()[row] - 1, 0);
                    }
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            &&
                            (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 0)
                            &&
                            (massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1))) {

                        dataArray[row] = exp(logRatios[isotopeIndex])/ dfGain
                                * intensity.get((int) massSpecOutputDataRecord.timeIndColumn()[row] - 1, 0)
                                + blMeansArray[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1];
                    }
                }
            }
        }

        /*
            % Define initial sigmas based on baseline
            for m = 1:d0.Nfar%+1
                itmp = d0.det_vec==m & d0.blflag==1;
                x0.sig(m,1) = 1*std(d0.data(itmp));
            end

            x0.sig(d0.Nfar+1,1) = 0;

            for m = 1: d0.Niso;
                itmp = d0.iso_vec==m ;
                x0.sig(d0.Ndet + m,1) = 1.1*10;
            end
         */
        double[] sigmas = new double[massSpecOutputDataRecord.faradayCount() + 1 + massSpecOutputDataRecord.isotopeCount()];
        for (int faradayIndex = 0; faradayIndex < massSpecOutputDataRecord.faradayCount(); faradayIndex++) {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            /*
            for (int row = 0; row < massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().getRowDimension(); row++) {
                if ((massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) == (faradayIndex + 1))
                        &&
                        (massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().get(row, 0) == 1)) {
                    descriptiveStatistics.addValue(massSpecOutputDataRecord.rawDataColumn().get(row, 0));
                }
            }
            */
            for (int row = 0; row < massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().length; row++) {
                if ((massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] == (faradayIndex + 1))
                        &&
                        (massSpecOutputDataRecord.baseLineFlagsForRawDataColumn()[row] == 1)) {
                    descriptiveStatistics.addValue(massSpecOutputDataRecord.rawDataColumn()[row]);
                }
            }
            sigmas[faradayIndex] = descriptiveStatistics.getStandardDeviation();
        }

        for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
            // for (int row = 0; row < massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().getRowDimension(); row++) {
            for (int row = 0; row < massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().length; row++) {
                // if (massSpecOutputDataRecord.isotopeIndicesForRawDataColumn().get(row, 0) == (isotopeIndex + 1)) {
                if (massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] == (isotopeIndex + 1)) {
                    sigmas[massSpecOutputDataRecord.faradayCount() + 1 + isotopeIndex] = 11.0;
                    break;
                }
            }
        }


        return new DataModellerOutputRecord(
                // new Matrix(blMeansArray),
                blMeansArray,
                // new Matrix(blStdArray),
                blStdArray,
                dfGain,
                // new Matrix(logRatios),
                logRatios,
                // new Matrix(sigmas, sigmas.length),
                sigmas,
                // new Matrix(dataArray, dataArray.length),
                dataArray,
                IO,
                intensityPerBlock
        );
    }

    // provides the indexes as the result of sorting as per Matlab sort function's second return argument
    static class ArrayIndexComparator implements Comparator<Integer> {
        private final double[] array;

        public ArrayIndexComparator(double[] array) {
            this.array = array;
        }

        public Integer[] createIndexArray() {
            Integer[] indexes = new Integer[array.length];
            for (int i = 0; i < array.length; i++) {
                indexes[i] = i; // Autoboxing
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2) {
            return Double.compare(array[index1], array[index2]);
        }
    }

}