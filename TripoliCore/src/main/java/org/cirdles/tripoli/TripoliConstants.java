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

package org.cirdles.tripoli;

import java.io.File;

public final class TripoliConstants {

    public static final File TRIPOLI_RESOURCES_FOLDER = new File("TripoliResources");
    public static final File SCHEMA_FOLDER = new File(TRIPOLI_RESOURCES_FOLDER.getAbsolutePath() + File.separator + "Schema");

    public static final File PARAMETER_MODELS_FOLDER = new File(TRIPOLI_RESOURCES_FOLDER.getAbsolutePath() + File.separator + "ParameterModels");
    public static final File REFMAT_MODELS_FOLDER = new File(PARAMETER_MODELS_FOLDER.getAbsolutePath() + File.separator + "ReferenceMaterialModels");
    public static final File PHYSCONST_MODELS_FOLDER = new File(PARAMETER_MODELS_FOLDER.getAbsolutePath() + File.separator + "PhysicalConstantsModels");
    public static final File TRACER_MODELS_FOLDER = new File(PARAMETER_MODELS_FOLDER.getAbsolutePath() + File.separator + "TracerModels");

}