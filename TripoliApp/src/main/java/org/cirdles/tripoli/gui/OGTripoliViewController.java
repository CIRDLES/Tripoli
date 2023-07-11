package org.cirdles.tripoli.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.BasicScatterPlot;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.util.List;

import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;

public class OGTripoliViewController {
    public static AnalysisInterface analysis;

    @FXML
    private AnchorPane ogTripoliPlotsAnchorPane;

    @FXML
    void initialize() throws TripoliException {
        populatePlots();
    }

    private void populatePlots() throws TripoliException {
        ogTripoliPlotsAnchorPane.getChildren().clear();

        PlotWallPane plotsWallPane = new PlotWallPane();
        PlotWallPane.menuOffset = 0.0;
        plotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        plotsWallPane.setPrefSize(ogTripoliPlotsAnchorPane.getPrefWidth(), ogTripoliPlotsAnchorPane.getPrefHeight());
        ogTripoliPlotsAnchorPane.getChildren().add(plotsWallPane);

        SingleBlockModelRecord[] singleBlockModelRecords = AllBlockInitForMCMC.initBlockModels(analysis);
        int countOfBlocks = singleBlockModelRecords.length;
        int countOfOnPeakCycles = singleBlockModelRecords[0].mapLogRatiosToCycleStats().get(0).keySet().size();
        List<IsotopicRatio> isotopicRatioList = analysis.getAnalysisMethod().getIsotopicRatiosList();
        for (int logRatioIndex = 0; logRatioIndex < singleBlockModelRecords[0].logRatios().length; logRatioIndex++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotsWallPane);
            double[] cycleMeans = new double[countOfBlocks * countOfOnPeakCycles];
            double[] xAxis = new double[cycleMeans.length];
            for (int blockIndex = 0; blockIndex < singleBlockModelRecords.length; blockIndex++) {
                double[] logRatios = singleBlockModelRecords[blockIndex].logRatios();
                double[] cycleMeansForBlock = singleBlockModelRecords[blockIndex].assembleCycleMeansForLogRatio(logRatioIndex);
                System.arraycopy(cycleMeansForBlock, 0, cycleMeans, blockIndex * countOfOnPeakCycles, cycleMeansForBlock.length);
                for (int i = 0; i < countOfOnPeakCycles; i ++){
                    xAxis[blockIndex * countOfOnPeakCycles + i] = //singleBlockModelRecords[blockIndex].timeAccumulatorList().get(singleBlockModelRecords[blockIndex].onPeakStartingIndicesOfCycles()[i]);
                            blockIndex * countOfOnPeakCycles + i;
                }

            }
            LinePlotBuilder linePlotBuilder = LinePlotBuilder.initializeLinePlot(xAxis, cycleMeans, new String[]{isotopicRatioList.get(logRatioIndex).prettyPrint()}, "TIME", "LOGRATIO");
            AbstractPlot plot = BasicScatterPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), linePlotBuilder);
            tripoliPlotPane.addPlot(plot);
        }
    }


}