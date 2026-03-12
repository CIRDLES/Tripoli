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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import java.io.*;
import java.util.Collection;

/**
 * @author James F. Bowring
 */
public class SampleMetaDataUnmarshaller {

    public static SampleMetaData unmarshall(String sampleMetaDataFilePath) throws FileNotFoundException {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("SampleMetaData", SampleMetaData.class);
        xstream.alias("FractionMetaData", FractionMetaData.class);
        // clear out existing permissions and set own ones
        xstream.addPermission(NoTypePermission.NONE);
        // allow some basics
        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xstream.allowTypeHierarchy(Collection.class);
        xstream.addPermission(AnyTypePermission.ANY);

        Reader inReader = new FileReader(sampleMetaDataFilePath);
        BufferedReader reader = new BufferedReader(inReader);

        return (SampleMetaData) xstream.fromXML(reader);
    }
}
