package main.rice.node;

import main.rice.obj.APyObj;
import main.rice.obj.PySetObj;
import java.util.*;

/**
 * A representation of a node for generating PySetObjs.
 *
 * @param <InnerType> the type of object stored within the sets generated by this node
 */
public class PySetNode<InnerType extends APyObj<?>> extends AIterablePyNode<
        PySetObj<InnerType>, InnerType> {

    /**
     * Constructor fot a PySetNode; stores a reference to its child node (used to
     * generate its contained elements).
     *
     * @param child a node representing the valid elements for this set
     */
    public PySetNode(APyNode<InnerType> child) {
        this.leftChild = child;
    }

    /**
     * Generates a single valid PySetObj within the random domain; begins by randomly
     * generating a valid length (as constrained by the random domain), and then randomly
     * generates valid elements. Overridden to disallow duplicates.
     *
     * @return a single PySetObj selected from the random domain
     */
    public PySetObj<InnerType> genRandVal() {
        // Randomly select the size, n
        int length = this.ranDomainChoice().intValue();

        // Randomly select n elements; make sure to continuously monitor the size, as we
        // could generate the same element twice resulting in the size not increasing
        // from one iteration to the next
        Set<InnerType> set = new HashSet<>();
        while (set.size() < length) {
            InnerType childVal = this.leftChild.genRandVal();
            set.add(childVal);
        }

        return new PySetObj<>(set);
    }

    /**
     * Helper function for generating a PyListObj.
     *
     * @param innerVals the elements to be contained by the generated PyListObj
     * @return a PyListObj object encapsulating the innerVals
     */
    protected PySetObj<InnerType> genObj(List<InnerType> innerVals) {
        return new PySetObj<>(new HashSet<>(innerVals));
    }
}