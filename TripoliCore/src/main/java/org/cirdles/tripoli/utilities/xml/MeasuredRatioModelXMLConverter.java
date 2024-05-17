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

package org.cirdles.tripoli.utilities.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.MeasuredUserFunctionModel;

public class MeasuredRatioModelXMLConverter implements Converter {
    /**
     * checks the argument <code>clazz</code> against this <code>MeasuredUserFunctionModel</code>'s
     * <code>Class</code>. Used to ensure that the object about to be
     * marshalled/unmarshalled is of the correct type.
     *
     * @param clazz <code>Class</code> of the <code>Object</code> you wish
     *              to convert to/from XML
     * @return <code>boolean</code> - <code>true</code> if <code>clazz</code> matches
     * <code>MeasuredUserFunctionModel</code>'s <code>Class</code>; else <code>false</code>.
     * @pre argument <code>clazz</code> is a valid <code>Class</code>
     * @post <code>boolean</code> is returned comparing <code>clazz</code>
     * against <code>MeasuredUserFunctionModel.class</code>
     */
    public boolean canConvert(Class clazz) {
        return clazz.equals(MeasuredUserFunctionModel.class);
    }

    /**
     * writes the argument <code>value</code> to the XML file specified through <code>writer</code>
     *
     * @param value   <code>MeasuredUserFunctionModel</code> that you wish to write to a file
     * @param writer  stream to write through
     * @param context <code>MarshallingContext</code> used to store generic data
     * @pre <code>value</code> is a valid <code>MeasuredUserFunctionModel</code>,
     * <code>writer</code> is a valid <code>HierarchicalStreamWriter</code>,
     * and <code>context</code> is a valid <code>MarshallingContext</code>
     * @post <code>value</code> is written to the XML file specified via <code>writer</code>
     */
    public void marshal(Object value, HierarchicalStreamWriter writer,
                        MarshallingContext context) {

        MeasuredUserFunctionModel measuredRatio = (MeasuredUserFunctionModel) value;

        writer.startNode("name");
        writer.setValue(measuredRatio.getName());
        writer.endNode();

        writer.startNode("value");
        writer.setValue(String.valueOf(measuredRatio.getValue()));
        writer.endNode();

        writer.startNode("uncertaintyType");
        writer.setValue(measuredRatio.getUncertaintyType());
        writer.endNode();

        writer.startNode("oneSigma");
        writer.setValue(String.valueOf(measuredRatio.getOneSigma()));
        writer.endNode();

        writer.startNode("fracCorr");
        writer.setValue(Boolean.toString(measuredRatio.isFracCorr()));
        writer.endNode();

        writer.startNode("oxideCorr");
        writer.setValue(Boolean.toString(measuredRatio.isOxideCorr()));
        writer.endNode();

    }

    /**
     * reads a <code>MeasuredUserFunctionModel</code> from the XML file specified
     * through <code>reader</code>
     *
     * @param reader  stream to read through
     * @param context <code>UnmarshallingContext</code> used to store generic data
     * @return <code>MeasuredUserFunctionModel</code> - <code>MeasuredUserFunctionModel</code>
     * read from file specified by <code>reader</code>
     * @pre <code>reader</code> leads to a valid <code>MeasuredUserFunctionModel</code>
     * @post returns the <code>MeasuredUserFunctionModel</code> read from the XML file
     */
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        MeasuredUserFunctionModel measuredRatio = new MeasuredUserFunctionModel();

        reader.moveDown();
        measuredRatio.setName(reader.getValue());
        reader.moveUp();

        reader.moveDown();
        measuredRatio.setValue(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        // temp hack dec 2007 during transition to new data format
        reader.moveDown();
        if ("uncertaintyType".equals(reader.getNodeName())) {
            measuredRatio.setUncertaintyType(reader.getValue());
            reader.moveUp();

            reader.moveDown();
        }
        measuredRatio.setOneSigma(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        reader.moveDown();
        measuredRatio.setFracCorr((reader.getValue().equalsIgnoreCase("true")) ? true : false);
        reader.moveUp();

        reader.moveDown();
        measuredRatio.setOxideCorr((reader.getValue().equalsIgnoreCase("true")) ? true : false);
        reader.moveUp();

        return measuredRatio;
    }
}
