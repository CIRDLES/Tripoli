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

package org.cirdles.tripoli.utilities.stateUtilities;

import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.*;

/**
 * @author James F. Bowring
 */
public enum TripoliSerializer {
    ;

    /**
     * @param serializableObject
     * @param fileName
     * @throws TripoliException
     */
    public static void serializeObjectToFile(Object serializableObject, String fileName) throws TripoliException {

        // https://dzone.com/articles/fast-java-file-serialization
        // Sept 2018 speedup per Rayner request
        ObjectOutputStream objectOutputStream = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            FileOutputStream fos = new FileOutputStream(raf.getFD());
            objectOutputStream = new ObjectOutputStream(fos);
            objectOutputStream.writeObject(serializableObject);
        } catch (IOException ex) {
            throw new TripoliException("Cannot serialize object of " + serializableObject.getClass().getSimpleName() + " to: " + fileName
                    + "\n\nbecause: \n" + ex.getMessage());

        } finally {
            if (null != objectOutputStream) {
                try {
                    objectOutputStream.close();
                } catch (IOException iOException) {
                }
            }
        }
    }

    /**
     * @param filename
     * @param verbose
     * @return
     * @throws TripoliException
     */
    public static Object getSerializedObjectFromFile(String filename, boolean verbose) throws TripoliException {
        //FileInputStream inputStream;
        ObjectInputStream deserializedInputStream;
        Object deserializedObject = null;

        try (FileInputStream inputStream = new FileInputStream(filename)) {
            deserializedInputStream = new ObjectInputStream(inputStream);
            deserializedObject = deserializedInputStream.readObject();
            inputStream.close();

        } catch (FileNotFoundException ex) {
            if (verbose) {
                throw new TripoliException("The file you are attempting to open does not exist:\n"
                        + " " + filename);
            }
        } catch (IOException ex) {
            if (verbose) {
                throw new TripoliException("The file you are attempting to open is not a valid '*.tripoli' file.");
            }
        } catch (ClassNotFoundException | ClassCastException ex) {
            if (verbose) {
                throw new TripoliException("The file you are attempting to open is not compatible with this version of Tripoli.");
            }
        }

        return deserializedObject;
    }

}