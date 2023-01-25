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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2;

import java.util.Arrays;

import static java.lang.Math.pow;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2.ProposedModelParameters.buildProposalRangesRecord;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2.ProposedModelParameters.buildProposalSigmasRecord;

/**
 * @author James F. Bowring
 */
public class MCMCProcess {

    private final SingleBlockModelRecord singleBlockInitialModelRecord_X0;
    private int faradayCount;
    private int isotopeCount;
    private int maxIterationCount;
    private boolean hierarchical;
    private int stepCountForcedSave;
    private int burnInThreshold;
    private double[] baselineMultiplier;
    private SingleBlockDataSetRecord singleBlockDataSetRecord;
    private ProposedModelParameters.ProposalRangesRecord proposalRangesRecord;
    private ProposedModelParameters.ProposalSigmasRecord proposalSigmasRecord;

    public MCMCProcess(SingleBlockDataSetRecord singleBlockDataSetRecord, SingleBlockModelRecord singleBlockInitialModelRecord) {
        this.singleBlockDataSetRecord = singleBlockDataSetRecord;
        this.singleBlockInitialModelRecord_X0 = singleBlockInitialModelRecord;
    }

    public static MCMCProcess initializeMCMCProcess(SingleBlockDataSetRecord singleBlockDataSetRecord, SingleBlockModelRecord singleBlockInitialModelRecord) {
        /*
            % MCMC Parameters
            maxcnt = 2000;  % Maximum number of models to save
            hier = 1;  % Hierachical?
            datsav=100;  % Save model every this many steps

            burn = 10;  % Burn-in, start doing stats after this many saved models

            temp=1; % Unused parameter for parallel tempering algorithm

            % Baseline multiplier - weight Daly more strongly (I think)
            blmult = ones(size(d0.data));
            blmult(d0.axflag)=0.1;


            Ndata=d0.Ndata; % Number of picks
            Nsig = d0.Nsig; % Number of noise variables
         */
        MCMCProcess mcmcProcess = new MCMCProcess(singleBlockDataSetRecord, singleBlockInitialModelRecord);

        mcmcProcess.faradayCount = singleBlockInitialModelRecord.faradayCount();
        mcmcProcess.isotopeCount = singleBlockInitialModelRecord.isotopeCount();

        mcmcProcess.maxIterationCount = 2000;
        mcmcProcess.hierarchical = true;
        mcmcProcess.stepCountForcedSave = 100;
        mcmcProcess.burnInThreshold = 10;

        mcmcProcess.baselineMultiplier = new double[singleBlockInitialModelRecord.dataArray().length];
        Arrays.fill(mcmcProcess.baselineMultiplier, 1.0);
        int startingIndexOfPhotoMultiplierData =
                singleBlockDataSetRecord.getCountOfBaselineIntensities() + singleBlockDataSetRecord.getCountOfOnPeakFaradayIntensities();
        for (int row = startingIndexOfPhotoMultiplierData; row < singleBlockInitialModelRecord.dataArray().length; row++) {
            mcmcProcess.baselineMultiplier[row] = 0.1;
        }

        mcmcProcess.proposalRangesRecord =
                buildProposalRangesRecord(singleBlockInitialModelRecord.intensities().toRawCopy1D());
        mcmcProcess.proposalSigmasRecord =
                buildProposalSigmasRecord(singleBlockInitialModelRecord.baselineStandardDeviationsArray(), mcmcProcess.proposalRangesRecord);

        mcmcProcess.buildForwardModel();

        return mcmcProcess;
    }

    private void buildForwardModel(){
        /*
            % Assign initial values for model x
            x=x0;

            %% Forward model data from initial model
            % Forward model baseline measurements
            for mm=1:d0.Nfar%+1  % Iterate over Faradays
                d(d0.blflag & d0.det_ind(:,mm),1) = x0.BL(mm); % Faraday Baseline
                dnobl(d0.blflag & d0.det_ind(:,mm),1) = 0; % Data with No Baseline
            end

        % Forward model isotope measurements
        for n = 1:d0.Nblock  % Iterate over blocks
            % Calculate block blockIntensities from blockIntensities variables
            Intensity{n} = InterpMat{n}*x0.I{n};
            Intensity2{n} = Intensity{n};

            %Iterate over Isotopes
            for mm=1:d0.Niso;
                % Calculate Daly data
                itmp = d0.iso_ind(:,mm) & d0.axflag & d0.block(:,n); % If isotope and axial and block number
                d(itmp) = exp(x0.lograt(mm))*Intensity{n}(d0.time_ind(itmp));
                dnobl(itmp) = d(itmp);

                % Calculate Faraday datas
                itmp = d0.iso_ind(:,mm) & ~d0.axflag & d0.block(:,n);
                dnobl(itmp) = exp(x0.lograt(mm))*x0.DFgain^-1 *Intensity{n}(d0.time_ind(itmp)); % Data w/o baseline
                d(itmp) = dnobl(itmp) + x0.BL(d0.det_vec(itmp)); % Add baseline
            end
        end
        */

        // NOTE: these already done in the initial model
        double[] dataArray = singleBlockInitialModelRecord_X0.dataArray().clone();
        double[] dataWithNoBaselineArray = singleBlockInitialModelRecord_X0.dataWithNoBaselineArray().clone();
        double[] dSignalNoiseArray = singleBlockInitialModelRecord_X0.dSignalNoiseArray();

        /*
            % New data covariance vector
            Dsig = sqrt(x0.sig(d0.det_vec).^2 + x0.sig(d0.iso_vec+d0.Ndet).*dnobl);

            % Initialize data residual vectors
            restmp=zeros(size(Dsig));
            restmp2=zeros(size(Dsig));

            % Calculate data residuals from starting model
            restmp = (d0.data-d).^2;

            % Calculate error function
            E=sum(restmp.*blmult./Dsig);  % Weighted by noise variance (for acceptance)
            E0=sum(restmp);  % Unweighted (for tracking convergence)
        */

        // not used double[] residualTmpArray = new double[dSignalNoiseArray.length];
        // not used?? Matrix residualTmp2 = new Matrix(dSignalNoise.getRowDimension(), 1);
        double initialModelErrorWeighted_E = 0.0;
        double initialModelErrorUnWeighted_E0 = 0.0;

        for (int row = 0; row < dSignalNoiseArray.length; row++) {
            System.out.println("" + row + ", " + singleBlockDataSetRecord.getBlockIntensityArray()[row]+ ", " + dataArray[row]+ ", " + (singleBlockDataSetRecord.getBlockIntensityArray()[row] - dataArray[row]));
            double calculatedValue = StrictMath.pow(singleBlockDataSetRecord.getBlockIntensityArray()[row] - dataArray[row], 2);
            initialModelErrorWeighted_E = initialModelErrorWeighted_E + (calculatedValue * baselineMultiplier[row] / dSignalNoiseArray[row]);
            initialModelErrorUnWeighted_E0 = initialModelErrorUnWeighted_E0 + calculatedValue;
        }


    }
}