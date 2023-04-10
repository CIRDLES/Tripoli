/*
 * TripoliPersistentState.java
 *
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cirdles.tripoli.utilities.stateUtilities;

import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.cirdles.tripoli.constants.TripoliConstants.TRIPOLI_USERS_DATA_FOLDER_NAME;

/**
 * @author James F. Bowring
 */
public class TripoliPersistentState implements Serializable {

    // class variables
    private static final long serialVersionUID = -7177208336686295496L;
    private static final String TRIPOLI_PERSISTENT_STATE_FILE_NAME = "TripoliPersistentState.ser";
    private static final int MRU_COUNT = 10;
    private static TripoliPersistentState myInstance;
    private String tripoliUserHomeDirectoryLocal;
    // instance variables
    private File MRUSessionFile;
    private List<String> MRUSessionList;
    private String MRUSessionFolderPath;
    private File MRUDataFile;
    private List<String> MRUDataFileList;
    private String MRUDataFileFolderPath;
    private File MRUMethodXMLFile;
    private List<String> MRUMethodXMLList;
    private String MRUMethodXMLFolderPath;

//    private void readObject(ObjectInputStream stream) throws IOException,
//            ClassNotFoundException {
//        stream.defaultReadObject();
//
//        ObjectStreamClass myObject = ObjectStreamClass.lookup(
//                Class.forName(TripoliPersistentState.class.getCanonicalName()));
//        long theSUID = myObject.getSerialVersionUID();
//
//        System.out.println("Customized De-serialization of TripoliPersistentState "
//                + theSUID);
//    }

    /**
     *
     */
    private TripoliPersistentState() {

        initMRULists();
        tripoliUserHomeDirectoryLocal = System.getProperty("user.home");

        // check if user data folder exists and create if it does not
        File dataFolder = new File(
                File.separator + tripoliUserHomeDirectoryLocal + File.separator + TRIPOLI_USERS_DATA_FOLDER_NAME);
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        MRUSessionFile = null;
        MRUSessionList = new ArrayList<>();
        MRUSessionFolderPath = "";

        MRUDataFile = null;
        MRUDataFileList = new ArrayList<>();
        MRUDataFileFolderPath = "";

        MRUMethodXMLFile = null;
        MRUMethodXMLList = new ArrayList<>();
        MRUMethodXMLFolderPath = "";

        serializeSelf();
    }

    /**
     * @return
     */
    public static TripoliPersistentState getExistingPersistentState() throws TripoliException {

        String mySerializedName
                = File.separator//
                + System.getProperty("user.home")//
                + File.separator//
                + TRIPOLI_USERS_DATA_FOLDER_NAME //
                + File.separator + TRIPOLI_PERSISTENT_STATE_FILE_NAME;

        if (myInstance == null) {
            myInstance = (TripoliPersistentState) TripoliSerializer.getSerializedObjectFromFile(mySerializedName, false);
        }


        if (myInstance == null) {
            // check if user data folder exists and create if it does not
            File dataFolder = new File(
                    File.separator + System.getProperty("user.home") + File.separator + TRIPOLI_USERS_DATA_FOLDER_NAME);
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            myInstance = new TripoliPersistentState();
            myInstance.serializeSelf();
        }
        return myInstance;
    }

    /**
     * @return
     */
    public static String getMySerializedName() throws TripoliException {
        String mySerializedName
                = File.separator//
                + System.getProperty("user.home")//
                + File.separator//
                + TRIPOLI_USERS_DATA_FOLDER_NAME //
                + File.separator + TRIPOLI_PERSISTENT_STATE_FILE_NAME;
        return mySerializedName;
    }

    public String getTripoliUserHomeDirectoryLocal() {
        if (tripoliUserHomeDirectoryLocal == null) {
            tripoliUserHomeDirectoryLocal = System.getProperty("user.home");
        }
        return tripoliUserHomeDirectoryLocal;
    }

    private void serializeSelf() {
        // save initial persistent state serialized file
        try {
            TripoliSerializer.serializeObjectToFile(this, getMySerializedName());
        } catch (TripoliException tripoliException) {
        }
    }
    //properties

    public void updateTripoliPersistentState() {
        serializeSelf();
    }

    // General methods *********************************************************
    private void initMRULists() {
        MRUSessionList = new ArrayList<>(MRU_COUNT);
        MRUDataFileList = new ArrayList<>(MRU_COUNT);
    }

    private void cleanListMRU(List<String> MRUfileList) {
        ArrayList<String> missingFileNames = new ArrayList<>();
        // test for missing files
        for (String projectFileName : MRUfileList) {
            File projectFile = new File(projectFileName);
            if (!projectFile.exists()) {
                missingFileNames.add(projectFileName);
            }
        }

        // remove missing fileNames
        for (String projectFileName : missingFileNames) {
            removeSessionFileNameFromMRU(projectFileName);
        }

        serializeSelf();
    }

    // MRU Session Data *********************************************************

    /**
     * @param sessionFileMRU
     */
    public void updateSessionListMRU(File sessionFileMRU) {

        if (sessionFileMRU != null) {
            try {
                // remove if exists in MRU list
                String MRUProjectFileName = sessionFileMRU.getCanonicalPath();
                MRUSessionList.remove(MRUProjectFileName);
                MRUSessionList.add(0, MRUProjectFileName);

                // trim list
                if (MRUSessionList.size() > MRU_COUNT) {
                    MRUSessionList.remove(MRU_COUNT);
                }

                // update MRU folder
                MRUSessionFolderPath = sessionFileMRU.getParent();
                if (MRUSessionFolderPath == null) {
                    MRUSessionFolderPath = "";
                }

                // update current file
                MRUSessionFile = sessionFileMRU;

            } catch (IOException iOException) {
            }
        }

        // save
        try {
            TripoliSerializer.serializeObjectToFile(this, getMySerializedName());
        } catch (TripoliException tripoliException) {
        }
    }

    public void removeFileNameFromSessionListMRU(String mruSessionFileName) {
        MRUSessionList.remove(mruSessionFileName);
    }

    public void cleanSessionListMRU() {
        cleanListMRU(MRUSessionList);
    }

    public void removeSessionFileNameFromMRU(String sessionFileName) {
        MRUSessionList.remove(sessionFileName);
    }

    /**
     * @return the MRUSessionFile
     */
    public File getMRUSessionFile() {
        return MRUSessionFile;
    }

    /**
     * @param MRUSessionFile the MRUSessionFile to set
     */
    public void setMRUSessionFile(File MRUSessionFile) {
        this.MRUSessionFile = MRUSessionFile;
    }

    /**
     * @return
     */
    public List<String> getMRUSessionList() {
        cleanSessionListMRU();
        return MRUSessionList;
    }

    /**
     * @param MRUSessionList
     */
    public void setMRUSessionList(ArrayList<String> MRUSessionList) {
        this.MRUSessionList = MRUSessionList;
    }

    /**
     * @return the MRUSessionFolderPath
     */
    public String getMRUSessionFolderPath() {
        if (MRUSessionFolderPath == null) {
            MRUSessionFolderPath = "";
        }
        return MRUSessionFolderPath;
    }

    /**
     * @param MRUSessionFolderPath the MRUSessionFolderPath to set
     */
    public void setMRUSessionFolderPath(String MRUSessionFolderPath) {
        this.MRUSessionFolderPath = MRUSessionFolderPath;
    }

    // MRU DataFile ***************************************************

    /**
     * @param dataFileMRU
     */
    public void updateDataFileListMRU(File dataFileMRU) {
        if (MRUDataFileList == null) {
            MRUDataFileList = new ArrayList<>();
        }

        if (dataFileMRU != null) {
            try {
                // remove if exists in MRU list
                String MRUDataFileName = dataFileMRU.getCanonicalPath();
                MRUDataFileList.remove(MRUDataFileName);
                MRUDataFileList.add(0, MRUDataFileName);

                // trim list
                if (MRUDataFileList.size() > MRU_COUNT) {
                    MRUDataFileList.remove(MRU_COUNT);
                }

                // update MRU folder
                MRUDataFileFolderPath = dataFileMRU.getParent();

                // update current file
                MRUDataFile = dataFileMRU;

            } catch (IOException iOException) {
            }
        }

        // save
        try {
            TripoliSerializer.serializeObjectToFile(this, getMySerializedName());
        } catch (TripoliException tripoliException) {
        }
    }

    public void removeFileNameFromDataFileListMRU(String mruDataFileName) {
        MRUDataFileList.remove(mruDataFileName);
    }

    public void cleanDataFileListMRU() {
        cleanListMRU(MRUDataFileList);
    }

    public void removeDataFileNameFromMRU(String dataFileName) {
        MRUDataFileList.remove(dataFileName);
    }

    /**
     * @return the MRUDataFile
     */
    public File getMRUDataFile() {
        return MRUDataFile;
    }

    /**
     * @param MRUDataFile the MRUDataFile to set
     */
    public void setMRUDataFile(File MRUDataFile) {
        this.MRUDataFile = MRUDataFile;
    }

    /**
     * @return the MRUDataFileList
     */
    public List<String> getMRUDataFileList() {
        return MRUDataFileList;
    }

    /**
     * @param MRUDataFileList the MRUDataFileList to set
     */
    public void setMRUDataFileList(List<String> MRUDataFileList) {
        this.MRUDataFileList = MRUDataFileList;
    }

    /**
     * @return the MRUDataFileFolderPath
     */
    public String getMRUDataFileFolderPath() {
        return MRUDataFileFolderPath;
    }

    public void setMRUDataFileFolderPath(String MRUDataFileFolderPath) {
        this.MRUDataFileFolderPath = MRUDataFileFolderPath;
    }


    // MRU MethodXML File Data ***************************************************

    /**
     * @param methodXMLMRU
     */
    public void updateMethodXMLFileListMRU(File methodXMLMRU) {
        if (MRUMethodXMLList == null) {
            MRUMethodXMLList = new ArrayList<>();
        }

        if (methodXMLMRU != null) {
            try {
                // remove if exists in MRU list
                String MRUTaskXMLName = methodXMLMRU.getCanonicalPath();
                MRUMethodXMLList.remove(MRUTaskXMLName);
                MRUMethodXMLList.add(0, MRUTaskXMLName);

                // trim list
                if (MRUMethodXMLList.size() > MRU_COUNT) {
                    MRUMethodXMLList.remove(MRU_COUNT);
                }

                // update MRU folder
                MRUDataFileFolderPath = methodXMLMRU.getParent();

                // update current file
                MRUDataFile = methodXMLMRU;

            } catch (IOException iOException) {
            }
        }

        // save
        try {
            TripoliSerializer.serializeObjectToFile(this, getMySerializedName());
        } catch (TripoliException tripoliException) {
        }
    }

    public void removeFileNameFromMethodXMLFileListMRU(String mruMethodXMLFileName) {
        MRUMethodXMLList.remove(mruMethodXMLFileName);
    }

    public void cleanMethodXMLFileListMRU() {
        cleanListMRU(MRUMethodXMLList);
    }

    /**
     * @return the MRUMethodXMLFile
     */
    public File getMRUMethodXMLFile() {
        return MRUMethodXMLFile;
    }

    /**
     * @param MRUMethodXMLFile the MRUMethodXMLFile to set
     */
    public void setMRUMethodXMLFile(File MRUMethodXMLFile) {
        this.MRUMethodXMLFile = MRUMethodXMLFile;
    }

    /**
     * @return the MRUMethodXMLList
     */
    public List<String> getMRUMethodXMLList() {
        if (MRUMethodXMLList == null) {
            MRUMethodXMLList = new ArrayList<>();
        }
        return MRUMethodXMLList;
    }

    /**
     * @param MRUMethodXMLList the MRUMethodXMLList to set
     */
    public void setMRUMethodXMLList(List<String> MRUMethodXMLList) {
        this.MRUMethodXMLList = MRUMethodXMLList;
    }

    /**
     * @return the MRUMethodXMLFolderPath
     */
    public String getMRUMethodXMLFolderPath() {
        if (MRUMethodXMLFolderPath == null) {
            MRUMethodXMLFolderPath = "";
        }
        return MRUMethodXMLFolderPath;
    }

    /**
     * @param MRUMethodXMLFolderPath the MRUMethodXMLFolderPath to set
     */
    public void setMRUMethodXMLFolderPath(String MRUMethodXMLFolderPath) {
        this.MRUMethodXMLFolderPath = MRUMethodXMLFolderPath;
    }

    public void removeMethodXMLFileNameFromMRU(String taskXMLFileName) {
        MRUMethodXMLList.remove(taskXMLFileName);
    }
}