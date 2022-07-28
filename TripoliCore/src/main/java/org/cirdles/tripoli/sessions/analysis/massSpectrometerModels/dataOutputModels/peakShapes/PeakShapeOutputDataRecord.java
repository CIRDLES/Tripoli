package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.peakShapes;

import jama.Matrix;

/**
 *
 * @param magnetMasses
 * @param measuredPeakIntensities
 * @param peakCenterMass
 * @param integrationPeriodMS
 * @param massID
 * @param detectorName
 * @param collectorWidthAMU
 * @param theoreticalBeamWidthAMU
 * @param collectorLimits
 * @param deltaMagnetMass
 * @param beamWindow
 */
public record PeakShapeOutputDataRecord(
         Matrix magnetMasses,               // vector of masses for intensity measurements
         Matrix measuredPeakIntensities,    // vector of corresponding peak intensities
         double peakCenterMass,             // mass at center of peak from header
         double integrationPeriodMS,              // integration period of measurements in ms
         String massID,                     // name of peak getting centered e.g. "205Pb"
         String detectorName,               // name of detector as string e.g. "L2"
         double collectorWidthAMU,          // width of collector aperture in AMU at center mass
         double theoreticalBeamWidthAMU,     // width of beam in AMU at center mass
         Matrix collectorLimits,
         double deltaMagnetMass,
         double beamWindow
) {
}