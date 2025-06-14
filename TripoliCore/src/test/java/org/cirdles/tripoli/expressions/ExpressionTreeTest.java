package org.cirdles.tripoli.expressions;

import jakarta.xml.bind.JAXBException;
import org.cirdles.commons.util.ResourceExtractor;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionTreeTest {
    static AnalysisInterface analysis;
    static Double[][] treeEvalOracle;

    public static void initializeAnalysis(Path dataFilePathPath) throws JAXBException, TripoliException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        analysis = AnalysisInterface.initializeNewAnalysis(0);
        analysis.setAnalysisName(analysis.extractMassSpecDataFromPath(dataFilePathPath));
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
    }

    @BeforeAll
    public static void setUp() throws URISyntaxException, JAXBException, TripoliException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String dataDirectoryString = "/org/cirdles/tripoli/dataSourceProcessors/dataSources/ogTripoli/isotopxPhoenixTIMS/kU_IGL/isolinxVersion1/NBS981_210325b-392.TIMSDP";
        File directoryFile = new File(Objects.requireNonNull(Tripoli.class.getResource(dataDirectoryString)).toURI());
        initializeAnalysis(directoryFile.toPath());

        treeEvalOracle = csvToMatrix();
    }

    private List<String> buildInfixList(){
        List<UserFunction> ufList = analysis.getUserFunctions();
        List<String> infixList = new ArrayList<>();

        // 204Pb + 205Pb * 206Pb - 207Pb * ( 208Pb + 204/206 )
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

        return infixList;
    }

    /**
     * Tests evaluation precedence of expression tree structure
     */
    @Test
    public void expressionTreeTest() {
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
        assertEquals(1.0, tree2.eval(analysis)[0][0]);
    }

    /**
     * Tests shunting yard, tree building, and expression evaluation
     */
    @Test
    public void shuntingTreeTest() {
        List<String> infixList = buildInfixList();
        System.out.println("Infix   = " + infixList);

        List<String> postfixList = ShuntingYard.infixToPostfix(infixList);
        System.out.println("Postfix = " + postfixList);

        ExpressionTree tree = (ExpressionTree) ExpressionTree.buildTree(postfixList);

        System.out.println(tree.prettyPrint(tree, analysis, true));

        for (int i = 0; i < treeEvalOracle.length; i++) {
            assertArrayEquals(treeEvalOracle[i], tree.eval(analysis)[i]);
        }

    }

    private static Double[][] csvToMatrix() throws IOException {
        ResourceExtractor tripoliExtractor = new ResourceExtractor(ExpressionTreeTest.class);
        File filename = tripoliExtractor.extractResourceAsFile("/org/cirdles/tripoli/core/expressions/NBS981_210325b-392_ExpressionEval.txt");

        ArrayList<Double[]> oracleArray = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                oracleArray.add(convertCSVLineToArray(line));
            }
        }
        Double[][] retVal = new Double[oracleArray.size()][];
        for (int i = 0; i < oracleArray.size(); i++) {
            retVal[i] = oracleArray.get(i);
        }

        return retVal;
    }

    private static Double[] convertCSVLineToArray(String csvLine) {
        return Arrays.stream(csvLine.split(","))
                .map(Double::parseDouble)
                .toArray(Double[]::new);
    }
}
