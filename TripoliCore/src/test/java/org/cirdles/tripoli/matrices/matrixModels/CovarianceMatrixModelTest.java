package org.cirdles.tripoli.matrices.matrixModels;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;

class CovarianceMatrixModelTest {

    @Test
    void initializeMatrixModelWithVariances() {
        AbstractMatrixModel myMatrix = new CovarianceMatrixModel();

        String[] rowNames = new String[]{"first", "second", "third", "fourth", "fifth"};
        myMatrix.setRows( rowNames );
        myMatrix.setCols( myMatrix.getRows() );

        ConcurrentMap<String, BigDecimal> varianceTerms = new ConcurrentHashMap<>();
        varianceTerms.put( "third", new BigDecimal( 1 ) );
        varianceTerms.put( "fourth", new BigDecimal( 2 ) );
        varianceTerms.put( "fifth", new BigDecimal( 3 ) );

        Map<String, BigDecimal> coVariances = new HashMap<>();
        coVariances.put( "covThird__fourth", new BigDecimal( 9 ) );

        if (((CovarianceMatrixModel) myMatrix).initializeMatrixModelWithVariances(varianceTerms)) {
            ((CovarianceMatrixModel)myMatrix).initializeCoVariances(coVariances);
            System.out.println(myMatrix.ToStringWithLabels());
        }
    }
}