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

package org.cirdles.tripoli.constants;

import java.nio.CharBuffer;

/**
 * @author James F. Bowring
 */
public enum ConstantsTripoliCore {
    ;
    public static final String SPACES_100 = CharBuffer.allocate(100).toString().replace('\0', ' ');

    public static final String MISSING_STRING_FIELD = "MISSING";

    /**
     * elementary charge e is exactly 1.602176634×10−19 coulomb (C).
     * see: https://en.wikipedia.org/wiki/2019_redefinition_of_the_SI_base_units
     */
    public static final double ELEMENTARY_CHARGE_E = 1.602176634e-19;
    public static final double ONE_COULOMB = 1.0 / ELEMENTARY_CHARGE_E; // s.b. 6.2415091e18 == 6.2415090744607631E18

}