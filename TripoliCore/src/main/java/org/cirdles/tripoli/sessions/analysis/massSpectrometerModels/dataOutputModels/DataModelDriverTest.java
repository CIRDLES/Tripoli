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

import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethodBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.DataSourceProcessor_OPPhoenix;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author James F. Bowring
 */
public class DataModelDriverTest {

    public static DataModellerOutputRecord driveModelTest(Path dataFilePath) throws IOException {
        DataSourceProcessor_OPPhoenix dataSourceProcessorOPPhoenix
                = DataSourceProcessor_OPPhoenix.initializeWithAnalysisMethod(AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("BurdickBlSyntheticData"));
        MassSpecOutputDataRecord massSpecOutputDataRecord = dataSourceProcessorOPPhoenix.prepareInputDataModelFromFile(dataFilePath);
        DataModellerOutputRecord dataModelInit = DataModelInitializer.modellingTest(massSpecOutputDataRecord);

        applyInversionWithRJ_MCMC(massSpecOutputDataRecord, dataModelInit);




        return dataModelInit;
    }

    static void applyInversionWithRJ_MCMC(MassSpecOutputDataRecord massSpecOutputDataRecord , DataModellerOutputRecord dataModelInit){
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


            % Range for ratios and intensity parameters
            prior.BL = [-1 1]*1e6;  % Faraday baseline
            prior.BLdaly = [0 0];   % Daly baseline (no baseline uncertainty)
            prior.lograt = [-20 20]; % Log ratio
            prior.I = [0 1.5*max([x0.I{:}])];  % Intensity
            prior.DFgain = [0.8 1.0];  % Daly-Faraday gain

            prior.sig = [0 1e6];  % Noise hyperparameter for Faraday
            prior.sigdaly = [0 0]; % Gaussian noise on Daly
            prior.sigpois = [0 10]; % Poisson noise on Daly
         */
        int maxCount = 2000;
        boolean hierarchical = true;
        int stepCountForcedSave = 100;
        int burn = 10;


    }
}