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

package org.cirdles.tripoli.utilities.stateUtilities.liveWorkFlow;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author James F. Bowring
 */
@XmlRootElement
public class FractionMetaData {
    String fractionID;
    String aliquotName;
    String fractionXMLUPbReduxFileName_U;
    String fractionXMLUPbReduxFileName_Pb;

    public String getFractionID() {
        return fractionID;
    }

    public void setFractionID(String fractionID) {
        this.fractionID = fractionID;
    }

    public String getAliquotName() {
        return aliquotName;
    }

    public void setAliquotName(String aliquotName) {
        this.aliquotName = aliquotName;
    }

    public String getFractionXMLUPbReduxFileName_U() {
        return fractionXMLUPbReduxFileName_U;
    }

    public void setFractionXMLUPbReduxFileName_U(String fractionXMLUPbReduxFileName_U) {
        this.fractionXMLUPbReduxFileName_U = fractionXMLUPbReduxFileName_U;
    }

    public String getFractionXMLUPbReduxFileName_Pb() {
        return fractionXMLUPbReduxFileName_Pb;
    }

    public void setFractionXMLUPbReduxFileName_Pb(String fractionXMLUPbReduxFileName_Pb) {
        this.fractionXMLUPbReduxFileName_Pb = fractionXMLUPbReduxFileName_Pb;
    }


}
