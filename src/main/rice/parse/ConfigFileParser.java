package main.rice.parse;

// TODO: implement the ConfigFileParser class here

import main.rice.node.*;
import main.rice.obj.AIterablePyObj;
import main.rice.obj.APyObj;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses a configuration file containing information about function names,
 * input types, and exhaustive/random input domains. The configuration is read
 * as a JSON file and converted into a ConfigFile object that can be used to
 * create test inputs for a function.
 */
public class ConfigFileParser {

    /**
     * Reads the contents of a file at the given file path.
     *
     * @param filepath The path to the file.
     * @return The contents of the file as a single string.
     * @throws IOException If an I/O error occurs reading from the file.
     */
    public static String readFile(String filepath) throws IOException {
        return Files.readString(Paths.get(filepath));
    }

    /**
     * Parses the contents of the configuration file into a ConfigFile object.
     *
     * @param contents The contents of the configuration file as a string.
     * @return A ConfigFile object with the parsed information.
     * @throws InvalidConfigException If the contents are invalid or cannot be parsed.
     */
    public static ConfigFile parse(String contents) throws InvalidConfigException {

        // check json obj
        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(contents);
        }
        catch (Exception e) {
            throw new InvalidConfigException("file contents is not a JSON object");
        }

        String fName;
        List<APyNode<?>> nodes = new ArrayList<>();
        int numRand;

        try {

            //fname
            if (!jsonObj.has("fname")) {
                throw new InvalidConfigException("fname key is missing in config file");
            }

            if (jsonObj.get("fname") instanceof String s) {
                fName = s;
            }
            else {
                throw new InvalidConfigException("value of fname is not a string");
            }

            //types
            if (!jsonObj.has("types")) {
                throw new InvalidConfigException("types key is missing in config file");
            }

            if (jsonObj.get("types") instanceof JSONArray a) {

                for (int i = 0; i < a.length(); i++) {

                    // parse each type
                    if (a.get(i) instanceof String type) {
                        nodes.add(parseType(type));
                    }
                    else {
                        throw new InvalidConfigException("value of type in types is not a string");
                    }
                }
            }
            else {
                throw new InvalidConfigException("types is not a JSON array");
            }

            //exdomain and randomain
            if (!jsonObj.has("exhaustive domain")) {
                throw new InvalidConfigException("exhaustive domain key is missing in config file");
            }

            if (!jsonObj.has("random domain")) {
                throw new InvalidConfigException("exhaustive domain key is missing in config file");
            }

            if (jsonObj.get("exhaustive domain") instanceof JSONArray exArray) {
                if (jsonObj.get("random domain") instanceof JSONArray ranArray) {

                    // iterate through nodes
                    for (int i = 0; i < nodes.size(); i++) {
                        APyNode<?> node = nodes.get(i);

                        if (exArray.get(i) instanceof String exDomain) {
                            parseDomain(node, exDomain, true);
                        }
                        else {
                            throw new InvalidConfigException("value of element in exhaustive domain array is not a string");
                        }

                        if (ranArray.get(i) instanceof String ranDomain) {
                            parseDomain(node, ranDomain, false);
                        }
                        else {
                            throw new InvalidConfigException("value of element in random domain array is not a string");
                        }

                    }
                }
                else {
                    throw new InvalidConfigException("random domain value is not a JSON array");
                }
            }
            else {
                throw new InvalidConfigException("exhaustive domain value is not a JSON array");
            }

            //numrand
            if (!jsonObj.has("num random")) {
                throw new InvalidConfigException("num random key is missing in config file");
            }
            if (jsonObj.get("num random") instanceof Integer num) {
                numRand = num;
            }
            else {
                throw new InvalidConfigException("value of num random is not a integer");
            }

            if (numRand < 0) {
                throw new InvalidConfigException("Invalid num random: not a non-negative integer");
            }

        }
        catch (Exception e) {
            throw new InvalidConfigException(e.getMessage());
        }

        return new ConfigFile(fName, nodes, numRand);

    }

    /**
     * Parses a given type string and returns an appropriate object based on the specified type.
     *
     * @return an APyNode of the parsed type
     * @throws InvalidConfigException if the domain format is incorrect, contains invalid characters,
     *               non-numeric elements, negative numbers, or if the bounds in a range are invalid
     */
    private static APyNode<?> parseType(String type) throws InvalidConfigException {
        // finds first bracket
        int brackIdx = type.indexOf('(');
        if (brackIdx == -1) {
            type = type.strip();
            if (type.equals("int")) {
                return new PyIntNode();
            }

            else if (type.equals("float")) {
                return new PyFloatNode();
            }

            else if (type.equals("bool")) {
                return new PyBoolNode();
            }

            else {
                throw new InvalidConfigException("inputted simple type is not a valid type");
            }
        }

        String inner = type.substring(brackIdx + 1);
        inner = inner.strip();

        if (inner.isEmpty()) {
            throw new InvalidConfigException("collection type has no token(s) after opening bracket");
        }

        String outer = type.substring(0, brackIdx);
        outer = outer.strip();

        // outer type is a list
        if (outer.equals("list")) {
            return new PyListNode<>(parseType(inner));
        }

        // outer type is a tuple
        else if (outer.equals("tuple")) {
            return new PyTupleNode<>(parseType(inner));
        }

        // outer type is a set
        else if (outer.equals("set")) {
            return new PySetNode<>(parseType(inner));
        }

        // outer type is a string
        else if (outer.equals("str")) {
            Set<Character> charDomain = new HashSet<>();

            // add each character in token
            for (int charIdx = 0; charIdx < inner.length(); charIdx++) {
                charDomain.add(inner.charAt(charIdx));
            }
            return new PyStringNode(charDomain);
        }

        else if (outer.equals("dict")) {
            int colIdx = inner.indexOf(':');
            if (colIdx == -1) {
                throw new InvalidConfigException("dictionary type does not have a colon separating key and value types");
            }

            String keyType = inner.substring(0, colIdx);
            keyType = keyType.strip();
            if (keyType.isEmpty()) {
                throw new InvalidConfigException("dictionary key type is empty");
            }

            String valueType = inner.substring(colIdx + 1);
            valueType = valueType.strip();
            if (valueType.isEmpty()) {
                throw new InvalidConfigException("dictionary value type is empty");
            }

            return new PyDictNode<>(parseType(keyType), parseType(valueType));
        }

        // outer type is not one of the allowed types
        else {
            throw new InvalidConfigException("inputted outer type is not a valid type");
        }

    }

    /**
     * Parses the given domain string and assigns a domain to the specified node.
     *
     * @param node The node for which the domain is being parsed.
     * @param domain The domain string to be parsed.
     * @param isExDomain A boolean indicating whether the domain is exhaustive or random.
     * @throws InvalidConfigException If the domain string is not formatted correctly for the node type.
     */
    private static void parseDomain(APyNode<?> node, String domain, boolean isExDomain) throws InvalidConfigException {
        // strips the domain to remove whitespace - this is an operation that will be conducted very often
        domain = domain.strip();

        if (node instanceof PyIntNode intNode) {
            // initialize domain list that will be assigned
            List<Integer> intList = new ArrayList<>();

            int tilIdx = domain.indexOf('~');

            if (tilIdx == -1) {

                Set<Integer> seen = new HashSet<>();

                if (!(domain.charAt(0) == '[' && domain.charAt(domain.length() - 1) == ']')) {
                    throw new InvalidConfigException("integer array has incorrect bracket format");
                }

                String data = domain.substring(1, domain.length() - 1);

                for (String stringNum : data.split(",")) {
                    stringNum = stringNum.strip();

                    Integer num;
                    try {
                        num = Integer.parseInt(stringNum);
                    } catch (Exception e) {
                        throw new InvalidConfigException("integer array domain has illegal domain element(s)");
                    }

                    if (seen.contains(num)) {
                        continue;
                    }
                    seen.add(num);
                    intList.add(num);
                }

            }
            else {

                int lowerVal;
                int upperVal;

                String lower = domain.substring(0, tilIdx);
                lower = lower.strip();

                String upper = domain.substring(tilIdx + 1);
                upper = upper.strip();

                try {
                    lowerVal = Integer.parseInt(lower);
                    upperVal = Integer.parseInt(upper);
                } catch (Exception e) {
                    throw new InvalidConfigException("integer domain range has illegal domain bounds");
                }

                if (lowerVal > upperVal) {
                    throw new InvalidConfigException("lower bound must be less than or equal to upper bound");
                }

                for (Integer currVal = lowerVal; currVal <= upperVal; currVal++) {
                    intList.add(currVal);
                }

            }

            // set exhaustive or random domain
            if (isExDomain) {
                node.setExDomain(intList);
            }
            else {
                node.setRanDomain(intList);
            }

        }

        else if (node instanceof PyFloatNode floatNode) {
            // initialize domain list that will be assigned
            List<Double> floatList = new ArrayList<>();

            int tilIdx = domain.indexOf('~');

            if (tilIdx == -1) {

                Set<Double> seen = new HashSet<>();

                if (!(domain.charAt(0) == '[' && domain.charAt(domain.length() - 1) == ']')) {
                    throw new InvalidConfigException("float array has incorrect bracket format");
                }

                String data = domain.substring(1, domain.length() - 1);

                for (String stringNum : data.split(",")) {
                    stringNum = stringNum.strip();

                    Double num;
                    try {
                        num = Double.parseDouble(stringNum);
                    } catch (Exception e) {
                        throw new InvalidConfigException("float array domain has illegal domain element(s)");
                    }

                    if (seen.contains(num)) {
                        continue;
                    }
                    seen.add(num);
                    floatList.add(num);
                }

            }
            else {

                int lowerVal;
                int upperVal;

                String lower = domain.substring(0, tilIdx);
                lower = lower.strip();

                String upper = domain.substring(tilIdx + 1);
                upper = upper.strip();

                try {
                    lowerVal = Integer.parseInt(lower);
                    upperVal = Integer.parseInt(upper);
                } catch (Exception e) {
                    throw new InvalidConfigException("float domain range has illegal domain bounds: must be integers");
                }

                if (lowerVal > upperVal) {
                    throw new InvalidConfigException("lower bound must be less than or equal to upper bound");
                }

                for (Double currVal = (double) lowerVal; currVal <= upperVal; currVal++) {
                    floatList.add(currVal);
                }

            }

            // set exhaustive or random domain
            if (isExDomain) {
                node.setExDomain(floatList);
            }
            else {
                node.setRanDomain(floatList);
            }

        }

        else if (node instanceof PyBoolNode boolNode) {
            // initialize domain list that will be assigned
            List<Integer> boolList = new ArrayList<>();

            int tilIdx = domain.indexOf('~');

            if (tilIdx == -1) {

                Set<Integer> seen = new HashSet<>();

                if (!(domain.charAt(0) == '[' && domain.charAt(domain.length() - 1) == ']')) {
                    throw new InvalidConfigException("boolean array has incorrect bracket format");
                }

                String data = domain.substring(1, domain.length() - 1);

                for (String stringNum : data.split(",")) {
                    stringNum = stringNum.strip();

                    Integer num;
                    try {
                        num = Integer.parseInt(stringNum);
                    } catch (Exception e) {
                        throw new InvalidConfigException("boolean array domain has illegal domain element(s)");
                    }

                    Set<Integer> permissible = new HashSet<>();
                    permissible.add(0);
                    permissible.add(1);
                    if (!permissible.contains(num)) {
                        throw new InvalidConfigException("boolean array has values outside accepted boolean value domain (0 and 1)");
                    }

                    if (seen.contains(num)) {
                        continue;
                    }
                    seen.add(num);
                    boolList.add(num);
                }

            }
            else {

                int lowerVal;
                int upperVal;

                String lower = domain.substring(0, tilIdx);
                lower = lower.strip();

                String upper = domain.substring(tilIdx + 1);
                upper = upper.strip();

                try {
                    lowerVal = Integer.parseInt(lower);
                    upperVal = Integer.parseInt(upper);
                } catch (Exception e) {
                    throw new InvalidConfigException("boolean domain range has illegal domain bounds: must be integers");
                }

                if (lowerVal > upperVal) {
                    throw new InvalidConfigException("lower bound must be less than or equal to upper bound");
                }

                for (Integer currVal = lowerVal; currVal <= upperVal; currVal++) {
                    boolList.add(currVal);
                }

            }

            // set exhaustive or random domain
            if (isExDomain) {
                node.setExDomain(boolList);
            }
            else {
                node.setRanDomain(boolList);
            }

        }

        else if (node instanceof PyListNode || node instanceof PyTupleNode || node instanceof PySetNode) {

            // we know that this cast will function because the types have been parsed and are already verified
            // therefore this ast is permissible
            AIterablePyNode<AIterablePyObj<?>, ?> iterableNode = (AIterablePyNode) node;

            int brackIdx = domain.indexOf('(');
            if (brackIdx == -1) {
                throw new InvalidConfigException("iterable domain has no bracket following length domain");
            }

            String outer = domain.substring(0, brackIdx).strip();

            // handle recursive parsing of outer iterable domain
            List<Integer> iterableList = parseIterableDom(outer);
            if (isExDomain) {
                iterableNode.setExDomain(iterableList);
            } else {
                iterableNode.setRanDomain(iterableList);
            }

            String inner = domain.substring(brackIdx + 1).strip();

            parseDomain(node.getLeftChild(), inner, isExDomain);
        }

        else if (node instanceof PyStringNode stringNode) {
            // call helper
            List<Integer> iterableList = parseIterableDom(domain);

            if (isExDomain) {
                node.setExDomain(iterableList);
            }
            else {
                node.setRanDomain(iterableList);
            }

        }

        else if (node instanceof PyDictNode dictNode) {
            int brackIdx = domain.indexOf('(');
            if (brackIdx == -1) {
                throw new InvalidConfigException("dictionary domain has no bracket following length domain");
            }

            String outer = domain.substring(0, brackIdx);
            outer = outer.strip();

            // handle recursive parsing of outer iterable domain
            List<Integer> iterableList = parseIterableDom(outer);
            if (isExDomain) {
                node.setExDomain(iterableList);
            }
            else {
                node.setRanDomain(iterableList);
            }

            String inner = domain.substring(brackIdx + 1);
            inner = inner.strip();

            int colIdx = inner.indexOf(':');
            if (colIdx == -1) {
                throw new InvalidConfigException("dictionary key and values domain has no colon");
            }

            String key = inner.substring(0, colIdx);
            key = key.strip();

            String value = inner.substring(colIdx + 1);
            value = value.strip();

            parseDomain(node.getLeftChild(), key, isExDomain);
            parseDomain(node.getRightChild(), value, isExDomain);

        }

        else {
            throw new InvalidConfigException("node is not of a PyNodeObj - this error should not happen");
        }
    }

    /**
     * Parses a domain string into a list of integers.
     *
     * @param domain the domain string to parse, which must be in the format of either "[a, b, c]" or "a~b"
     *               where a, b, and c are non-negative integers.
     * @return a list of integers extracted from the domain string.
     * @throws InvalidConfigException if the domain format is incorrect, contains invalid characters,
     *                                non-numeric elements, negative numbers, or if the bounds in a range are invalid.
     */
    private static List<Integer> parseIterableDom(String domain) throws InvalidConfigException {
        // Initialize an empty list to store the resulting integers from the domain.
        List<Integer> iterableList = new ArrayList<>();

        // Identify if the domain string contains a tilde ('~'), which denotes a range (e.g., "1~5").
        int tilIdx = domain.indexOf('~');

        // If no tilde is found, treat the domain as a list of individual numbers (e.g., "[1, 2, 3]").
        if (tilIdx == -1) {

            // Set to track seen numbers and avoid duplicates.
            Set<Integer> seen = new HashSet<>();

            // Validate that the domain starts with '[' and ends with ']' to ensure proper list format.
            if (!(domain.charAt(0) == '[' && domain.charAt(domain.length() - 1) == ']')) {
                throw new InvalidConfigException("iterable array has incorrect bracket format");
            }

            // Extract the content between the brackets.
            String data = domain.substring(1, domain.length() - 1);

            // Split the string by commas to separate individual numbers.
            for (String stringNum : data.split(",")) {
                // Remove any leading or trailing whitespace from the number.
                stringNum = stringNum.strip();

                Integer num;
                try {
                    // Attempt to convert the string to an integer.
                    num = Integer.parseInt(stringNum);
                } catch (Exception e) {
                    throw new InvalidConfigException("iterable array domain has illegal domain element(s)");
                }

                // Ensure the number is non-negative.
                if (num < 0) {
                    throw new InvalidConfigException("iterable array domain must have non-negative integers only");
                }

                // If the number has already been added to the list, skip it to avoid duplicates.
                if (seen.contains(num)) {
                    continue;
                }

                // Mark the number as seen and add it to the result list.
                seen.add(num);
                iterableList.add(num);
            }

        } else {
            // If a tilde ('~') is present, treat the domain as a range (e.g., "1~5").

            int lowerVal; // Lower bound of the range.
            int upperVal; // Upper bound of the range.

            // Extract and clean the lower and upper bounds from the domain string.
            String lower = domain.substring(0, tilIdx).strip();
            String upper = domain.substring(tilIdx + 1).strip();

            try {
                // Parse the lower and upper bounds as integers.
                lowerVal = Integer.parseInt(lower);
                upperVal = Integer.parseInt(upper);
            } catch (Exception e) {
                throw new InvalidConfigException("iterable domain range has illegal domain bounds");
            }

            // Check if the lower bound is greater than the upper bound.
            if (lowerVal > upperVal) {
                throw new InvalidConfigException("lower bound must be less than or equal to upper bound");
            }

            // Check if the bounds are non-negative.
            if (lowerVal < 0) {
                throw new InvalidConfigException("bounds must be non-negative integers only");
            }

            // Populate the list with integers from lowerVal to upperVal (inclusive).
            for (Integer currVal = lowerVal; currVal <= upperVal; currVal++) {
                iterableList.add(currVal);
            }
        }

        // Return the fully populated list of integers.
        return iterableList;
    }
}