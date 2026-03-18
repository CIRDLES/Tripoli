package org.cirdles.tripoli.utilities.xml;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XMLCleanerForSampleMetaData {
    public static File cleanXML(String fileName) throws IOException {
        File cleanedFile = new File("output_cleaned.xml");
        try {

            String xmlContent = Files.readString(Paths.get(fileName), StandardCharsets.UTF_8);
            System.out.println("File successfully read into a String.");
            int indexOfSample = xmlContent.indexOf("<sampleName>");
            xmlContent =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            + "<SampleMetaData>\n" + xmlContent.substring(indexOfSample);
            xmlContent = xmlContent.replaceAll("\n", "");

            Files.write(Paths.get(cleanedFile.getAbsolutePath()),
                    xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            File xmlFile = cleanedFile;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // Normalize the XML structure for cleaner processing
            doc.getDocumentElement().normalize();

            // Write the modified document back to a file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(cleanedFile);
            transformer.transform(source, result);

            System.out.println("SampleMetaData XML file cleaned successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cleanedFile;
    }

}

