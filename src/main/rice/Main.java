package main.rice;

import main.rice.basegen.BaseSetGenerator;
import main.rice.concisegen.ConciseSetGenerator;
import main.rice.parse.ConfigFile;
import main.rice.parse.ConfigFileParser;
import main.rice.parse.InvalidConfigException;
import main.rice.test.TestCase;
import main.rice.test.Tester;

import java.io.IOException;
import java.util.List;
import java.util.Set;

// TODO: implement the Main class here

/**
 * Initializes and runs the entirety of feat and presents the results.
 */
public class Main {
    /**
     * starts it and gives a brief description
     * @param args the args needed to start, includes config path, solution path, buggy implementation path
     * @throws IOException if i/o produces errors
     * @throws InvalidConfigException if error parsing
     * @throws InterruptedException if error running
     */
    public static void main(String[] args) throws IOException, InvalidConfigException, InterruptedException {
        Set<TestCase> results = generateTests(args);
        System.out.println("This is the concise test set of test cases that will cover all buggy implementations given " +
                "approximated through a greedy algorithm.");
        System.out.println(results.toString());
    }

    /**
     * Runs the entirety of FEAT from start to finish.
     *
     * @param args same as above
     * @return the greedy concise covering set
     * @throws IOException if something wrong I/O
     * @throws InvalidConfigException if parsing goes wrong
     * @throws InterruptedException if running is interrupted
     */
    public static Set<TestCase> generateTests(String[] args) throws IOException, InvalidConfigException, InterruptedException {
        ConfigFile parsedConfig = ConfigFileParser.parse(ConfigFileParser.readFile(args[0]));
        BaseSetGenerator baseSetGen = new BaseSetGenerator(parsedConfig.getNodes(), parsedConfig.getNumRand());
        Tester implTester = new Tester(parsedConfig.getFuncName(), args[1], args[2], baseSetGen.genBaseSet());
        implTester.computeExpectedResults();
        return ConciseSetGenerator.setCover(implTester.runTests());
    }
}