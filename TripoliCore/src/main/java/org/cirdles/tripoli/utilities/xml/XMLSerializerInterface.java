/*
 * Copyright 2016 James F. Bowring and CIRDLES.org.
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author James F. Bowring <bowring at gmail.com>
 */
public interface XMLSerializerInterface {

    /**
     * Use XStream to serialize object to XML
     *
     * @param filename
     */
    default void serializeXMLObject(String filename) {
        OutputStreamWriter outFile = null;
        try {
            XStream xstream = new XStream(new DomDriver());
            xstream.addPermission(AnyTypePermission.ANY);
            customizeXstream(xstream);
            String xml = xstream.toXML(this).trim();
            xml = customizeXML(xml).trim();

            outFile = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
            try (PrintWriter out = new PrintWriter(outFile)) {
                // Write xml to file
                out.println(xml);
                out.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(XMLSerializerInterface.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (outFile != null) {
                    outFile.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(XMLSerializerInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @param filename
     * @param doValidate
     * @return
     */
    default Object readXMLObject(String filename, boolean doValidate) {

        Object myModelClassInstance = null;

        try {
            InputStream bis = new ByteArrayInputStream(Files.readAllBytes(Paths.get(filename)));
            Reader reader = new InputStreamReader(bis, StandardCharsets.UTF_8);

            XStream xstream = new XStream(new DomDriver());
            xstream.addPermission(AnyTypePermission.ANY);
            customizeXstream(xstream);

            myModelClassInstance = xstream.fromXML(reader);
        } catch (Exception iOException) {
            // do nothing for now
        }

        return myModelClassInstance;
    }

    /**
     * @param xstream
     */
    void customizeXstream(XStream xstream);

    /**
     * @param xml
     * @return
     */
    default String customizeXML(String xml) {
        return xml;
    }
}