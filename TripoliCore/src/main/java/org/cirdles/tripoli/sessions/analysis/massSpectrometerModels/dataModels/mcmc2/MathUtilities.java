package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

public class MathUtilities {


    /*
         %% caluclate log-likelihood as a function of model parameters
         % minimize ll for least squares

         function ll = loglikLeastSquares(m, data, setup)

         dhat = evaluateModel(m, setup);
         dvar = updateDataVariance(m, setup);
         ll = loglik(dhat, data, dvar);

         end % function loglikLeastSquares
 */
    private static double loglikLeastSquares(MCMC2ModelRecord m, double[] data){

        return 0.0;
    }

    /**
     * function dhat = evaluateModel(m, setup)
     *
     *     % y = slope * x       + y-intercept
     *     % dhat = m(1) * setup.x + m(2);
     *
     *     lograb = m(1);
     *     logCb  = m(2) * ones(setup.nOPIntegrations, 1);
     *     ref1   = m(3);
     *     ref2   = m(4);
     *
     *     BL_det1 = ref1*ones(setup.nBLIntegrations,1);
     *     BL_det2 = ref2*ones(setup.nBLIntegrations,1);
     *     OP_det1 = exp(lograb + logCb) + ref1;
     *     OP_det2 = exp(logCb) + ref2;
     *
     *     dhat = [BL_det1; BL_det2; OP_det1; OP_det2];
     *
     * end % function evaluateModel
     */
    private static void evaluateModel(){

    }






    /*
    %% evaluate this particular model
function dhat = evaluateModel(m, setup)
    lograb = m(1);
    logCb  = m(2) * ones(setup.nOPIntegrations, 1);
    ref1   = m(3);
    ref2   = m(4);

    BL_det1 = ref1*ones(setup.nBLIntegrations,1);
    BL_det2 = ref2*ones(setup.nBLIntegrations,1);
    OP_det1 = exp(lograb + logCb) + ref1;
    OP_det2 = exp(logCb) + ref2;

    dhat = [BL_det1; BL_det2; OP_det1; OP_det2];

end % function evaluateModel

     */


    /*
    %% update data covariance matrix
function dvar = updateDataVariance(m, setup)

    % estimate ion beam variances from model parameters
    lograb = m(1);
    logCb = m(2);

    BL_det1_var = estimateIonBeamVariance(...
        zeros(setup.nBLIntegrations,1), ...
        setup.blIntegrationTimes, ...
        setup.detector);
    BL_det2_var = estimateIonBeamVariance(...
        zeros(setup.nBLIntegrations,1), ...
        setup.blIntegrationTimes, ...
        setup.detector);
    OP_det1_var = estimateIonBeamVariance(...
        exp(lograb + logCb), ...
        setup.blIntegrationTimes, ...
        setup.detector);
    OP_det2_var = estimateIonBeamVariance(...
        exp(logCb), ...
        setup.blIntegrationTimes, ...
        setup.detector);

    dvar = [BL_det1_var; BL_det2_var; OP_det1_var; OP_det2_var];

end % updateDataCovariance
     */

    /*
    %% calculate log-likelihood (including -1/2 term)
function ll = loglik(dhat, data, dvar)
    residuals = (data.int - dhat);
    chiSqTerms = residuals.^2 ./ dvar;
    ll = -1/2 * sum(chiSqTerms) - 1/2 * sum(log(dvar));
end % function loglik
     */


}
