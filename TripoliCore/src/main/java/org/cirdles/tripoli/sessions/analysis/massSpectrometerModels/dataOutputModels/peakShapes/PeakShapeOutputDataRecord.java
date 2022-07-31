package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.peakShapes;


import org.ojalgo.matrix.store.Primitive64Store;

/**
 * @param magnetMassesOJ
 * @param measuredPeakIntensitiesOJ
 * @param peakCenterMass
 * @param integrationPeriodMS
 * @param massID
 * @param detectorName
 * @param collectorWidthAMU
 * @param theoreticalBeamWidthAMU
 * @param collectorLimitsOJ
 * @param deltaMagnetMass
 * @param beamWindow
 */
public record PeakShapeOutputDataRecord(
        Primitive64Store magnetMassesOJ, // vector of masses for intensity measurements
        Primitive64Store measuredPeakIntensitiesOJ,    // vector of corresponding peak intensities
        double peakCenterMass,             // mass at center of peak from header
        double integrationPeriodMS,              // integration period of measurements in ms
        String massID,                     // name of peak getting centered e.g. "205Pb"
        String detectorName,               // name of detector as string e.g. "L2"
        double collectorWidthAMU,          // width of collector aperture in AMU at center mass
        double theoreticalBeamWidthAMU,     // width of beam in AMU at center mass
        Primitive64Store collectorLimitsOJ,
        double deltaMagnetMass,
        double beamWindow
) {
}