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
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.ETReduxFraction;
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.MeasuredUserFunction;

import java.util.ArrayList;

public class ETReduxFractionXMLConverter implements Converter {
    /**
     * @param clazz
     * @return
     */
    @Override
    public boolean canConvert(Class clazz) {
        return clazz.equals(ETReduxFraction.class);
    }

    /**
     * @param value
     * @param writer
     * @param context
     */
    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer,
                        MarshallingContext context) {

        ETReduxFraction etReduxFraction = (ETReduxFraction) value;

        writer.startNode("sampleName");
        writer.setValue(etReduxFraction.getSampleName());
        writer.endNode();

        writer.startNode("fractionID");
        writer.setValue(etReduxFraction.getFractionID());
        writer.endNode();

        writer.startNode("ratioType");
        writer.setValue(etReduxFraction.getEtReduxExportType().name());
        writer.endNode();

        writer.startNode("pedigree");
        writer.setValue(etReduxFraction.getPedigree());
        writer.endNode();

        writer.startNode("measuredRatios");
        context.convertAnother(etReduxFraction.getMeasuredRatios());
        writer.endNode();

        writer.startNode("meanAlphaU");
        writer.setValue(String.valueOf(etReduxFraction.getMeanAlphaU()));
        writer.endNode();

        writer.startNode("meanAlphaPb");
        writer.setValue(String.valueOf(etReduxFraction.getMeanAlphaPb()));
        writer.endNode();

        writer.startNode("r18O16O");
        writer.setValue(String.valueOf(etReduxFraction.getR18O_16O()));
        writer.endNode();

        writer.startNode("labUBlankMass");
        writer.setValue(String.valueOf(etReduxFraction.getLabUBlankMass()));
        writer.endNode();

        writer.startNode("r238235b");
        writer.setValue(String.valueOf(etReduxFraction.getR238_235b()));
        writer.endNode();

        writer.startNode("r238235s");
        writer.setValue(String.valueOf(etReduxFraction.getR238_235s()));
        writer.endNode();

        writer.startNode("tracerMass");
        writer.setValue(String.valueOf(etReduxFraction.getTracerMass()));
        writer.endNode();

    }

    /**
     * @param reader
     * @param context
     * @return
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {

        ETReduxFraction etReduxFraction = new ETReduxFraction();

        reader.moveDown();
        etReduxFraction.setSampleName(reader.getValue());
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setFractionID(reader.getValue());
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setEtReduxExportType(TripoliConstants.ETReduxExportTypeEnum.valueOf(reader.getValue()));
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setPedigree(reader.getValue());
        reader.moveUp();

        reader.moveDown();
        if ("measuredRatios".equals(reader.getNodeName())) {
            ArrayList<MeasuredUserFunction> ratios = new ArrayList<>();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                MeasuredUserFunction item = new MeasuredUserFunction();
                item = (MeasuredUserFunction) context.convertAnother(item, MeasuredUserFunction.class);
                ratios.add(item);
                reader.moveUp();
            }
            // Convert to array
            MeasuredUserFunction[] measuredRatios = new MeasuredUserFunction[ratios.size()];
            for (int i = 0; i < ratios.size(); i++) {
                measuredRatios[i] = ratios.get(i);
            }
            etReduxFraction.setMeasuredRatios(measuredRatios);
        }
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setMeanAlphaU(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setMeanAlphaPb(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setR18O_16O(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setLabUBlankMass(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setR238_235b(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setR238_235s(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        reader.moveDown();
        etReduxFraction.setTracerMass(Double.parseDouble(reader.getValue()));
        reader.moveUp();

        return etReduxFraction;
    }
}
