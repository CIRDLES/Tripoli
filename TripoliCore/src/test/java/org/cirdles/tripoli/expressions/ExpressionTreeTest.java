package org.cirdles.tripoli.expressions;

import jakarta.xml.bind.JAXBException;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTree;
import org.cirdles.tripoli.expressions.operations.Add;
import org.cirdles.tripoli.expressions.operations.Divide;
import org.cirdles.tripoli.expressions.parsing.ShuntingYard;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.expressions.userFunctions.UserFunctionNode;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionTreeTest {
    AnalysisInterface analysis;

    public void initializeAnalysis(Path dataFilePathPath) throws JAXBException, TripoliException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        analysis = AnalysisInterface.initializeNewAnalysis(0);
        analysis.setAnalysisName(analysis.extractMassSpecDataFromPath(dataFilePathPath));
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
    }

    public void printPlainExpressionTree() {
        List<UserFunction> ufList = analysis.getUserFunctions();
        System.out.println("Plain Expression: " +
                ufList.get(0).getName() +
                " + "+
                ufList.get(1).getName() +
                " = " +
                (ufList.get(0).getAnalysisStatsRecord().cycleModeMean() + ufList.get(1).getAnalysisStatsRecord().cycleModeMean()));
    }

    public List<Path> generateListOfPaths(String directoryString) throws URISyntaxException, IOException {

        File directoryFile = new File(Objects.requireNonNull(Tripoli.class.getResource(directoryString)).toURI());
        List<Path> filePathsList;
        try (Stream<Path> pathStream = Files.walk(directoryFile.toPath())) {
            filePathsList = pathStream.filter(Files::isRegularFile)
                    .toList();
        }
        return filePathsList;
    }

    @Test
    public void expressionTreeTest() throws JAXBException, TripoliException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, URISyntaxException {
        String dataDirectoryString = "/org/cirdles/tripoli/dataSourceProcessors/dataSources/ogTripoli/";
        List<Path> pathList = generateListOfPaths(dataDirectoryString);
        initializeAnalysis(pathList.get(2));
        List<UserFunction> ufList = analysis.getUserFunctions();
        ExpressionTree tree = new ExpressionTree(
                "Test Tree",
                new UserFunctionNode(ufList.get(0).getName()),
                new UserFunctionNode(ufList.get(1).getName()),
                new Add()
                );
        ExpressionTree tree2 = new ExpressionTree(
                "Nested Tree",
                tree,
                tree,
                new Divide());
        printPlainExpressionTree();
        System.out.println(tree2.eval(analysis));
        assertEquals(1.0, tree2.eval(analysis));
    }

    @Test
    public void shuntingTreeTest() throws JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, URISyntaxException, TripoliException {
        String dataDirectoryString = "/org/cirdles/tripoli/dataSourceProcessors/dataSources/ogTripoli/";
        List<Path> pathList = generateListOfPaths(dataDirectoryString);
        initializeAnalysis(pathList.get(2));
        List<UserFunction> ufList = analysis.getUserFunctions();
        List<String> infixList = new ArrayList<>();
        infixList.add(ufList.get(0).getName());
        infixList.add("+");
        infixList.add(ufList.get(1).getName());
        infixList.add("*");
        infixList.add(ufList.get(2).getName());
        infixList.add("-");
        infixList.add(ufList.get(3).getName());
        infixList.add("*");
        infixList.add("(");
        infixList.add(ufList.get(4).getName());
        infixList.add("+");
        infixList.add(ufList.get(5).getName());
        infixList.add(")");
        System.out.println("Infix   = " + infixList);
        List<String> postfixList = ShuntingYard.infixToPostfix(infixList);
        System.out.println("Postfix = " + postfixList);
        ExpressionTree tree = (ExpressionTree) ExpressionTree.buildTree(postfixList);
        System.out.println(tree.prettyPrint(tree, analysis));
        System.out.println("Val = " + tree.eval(analysis));

        assertEquals(-3.3786352844263718E10, tree.eval(analysis));
    }

}
