package main.rice.parse;
import main.rice.node.APyNode;
import java.util.List;

// TODO: implement the ConfigFile class here

/**
 * A ConfigFile class that bundles together configuration data given by the
 * parsing process. It encapsulates the name of the function under test,
 * a list of python nodes to be used for generating test cases, and the
 * number of random test cases to be generated.
 */
public class ConfigFile {

    /**
     * The name of the function under test.
     */
    private String funcName;

    /**
     * A list of PyNodes used to generate test cases for the function under test.
     */
    private List<APyNode<?>> nodes;

    /**
     * The number of random test cases to generate.
     */
    private int numRand;

    /**
     * Constructs a ConfigFile object containing configuration data for testing.
     *
     * @param funcName the name of the function under test
     * @param nodes a list of PyNodes used to generate test cases for the function
     * @param numRand the number of random test cases to generate
     */
    public ConfigFile(String funcName, List<APyNode<?>> nodes, int numRand) {
        this.funcName = funcName;
        this.nodes = nodes;
        this.numRand = numRand;
    }

    /**
     * Retrieves the name of the function under test.
     *
     * @return the name of the function under test
     */
    public String getFuncName() {
        return this.funcName;
    }

    /**
     * Retrieves the list of PyNodes used to generate test cases for the function.
     *
     * @return a list of PyNodes for test case generation
     */
    public List<APyNode<?>> getNodes() {
        return this.nodes;
    }

    /**
     * Retrieves the number of random test cases to be generated.
     *
     * @return the number of random test cases
     */
    public int getNumRand() {
        return this.numRand;
    }
}
