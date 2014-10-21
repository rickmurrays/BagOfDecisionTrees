package decisiontree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Id3 implements Serializable {
    private static final Log log = LogFactory.getLog(Id3.class);
    private Instances testInstances;
    private List<Instance> testInstance;
    private List<String> predicted;
    private Instances instances;
    private double accuracy;
    private Id3Node root;
    
    /**
     * Constructor for id3
     */
    public Id3(Instances instances) {
        this.instances = instances;
        // create the root node
        setRoot(new Id3Node(instances));
    }
    
    /**
     * Traverse root node
     */
    public void traverse() {
        traverse(root());
    }
    
    /**
     * Traverse the node tree
     * @param instances
     * @param attribute
     * @param value
     * @return 
     */
    public void traverse(Id3Node node) {
        log.info("Traversal node contains " + node.instances().size() + " instances");
        // return if there are no instances
        if(node.instances().size() == 0) return;
        // compute purity for instance set
        node.setPurity(node.instances().classifierPurity());
        log.info("Node purity " + node.purity());
        // compute entropy for instance set
        node.setEntropy(computeEntropy(node.instances()));
        log.info("Node entropy " + node.entropy());
        // no further traversal if entropy is 0
        if(node.entropy() == 0) {
            node.setClassifier(node.instances().majorityClassifier());
            log.info("Node classifier " + node.classifier() + ", entropy is 0");
            return;
        }
        // no further traversal if all attributes tested
        if(attributesExhausted(node.instances(), node.attributesTested())) {
            node.setClassifier(node.instances().majorityClassifier());
            log.info("Node classifier " + node.classifier() + ", attributes exhausted");
            return;
        }
        // compute attribute with maximum information gain
        node.setAttribute(computeMaxInfoGain(node.instances(), node.attributesTested()));
        log.info("Node attribute with max info gain " + node.attribute());
        // update attributes tested
        List<String> attributesTested = node.attributesTested();
        attributesTested.add(node.attribute());
        // determine if this node has continuous ranged values
        if(node.isContinuous()) {
            /**
             * When the attribute selected for the node contains
             * continuous ranged values, a binary split algorithm
             * is used to split the values across left and right
             * child nodes
             */
            log.info("Node will traverse a binary split");
            // compute binary split
            node.setSplit(computeBinarySplit(node.instances(), node.attribute()));
            log.info("Node binary split value " + node.split());
            // split instances using binary split value
            Instances[] split = node.instances().split(node.attribute(), node.split());
            // create child nodes
            node.setLeft(new Id3Node(split[0], attributesTested, node));
            node.setRight(new Id3Node(split[1], attributesTested, node));
            // traverse child nodes
            log.info("Traversing left node");
            traverse((Id3Node)node.left());
            log.info("Traversing right node");
            traverse((Id3Node)node.right());
        } else {
            /**
             * Otherwise, it's assumed that the attribute selected
             * for the node contains discrete values. For this case,
             * the set of possible values for the attribute will be
             * used to split the instances across multiple child nodes
             */
            log.info("Node will traverse a discrete value split");
            // split instances using discrete values
            Instances[] split = node.instances().split(node.attribute());
            // get attribute value set
            Set<String> values = node.instances().values(node.attribute());
            // convert value set to array list for indexing
            List<String> indexed = new ArrayList<String>(values);
            // create child node array
            Id3Node[] children = new Id3Node[split.length];
            // create child nodes
            for(int i = 0; i < split.length; i++) {
                // create the child node
                children[i] = new Id3Node(split[i], attributesTested, node);
                // set the attribute split value
                children[i].setValue(indexed.get(i));
            }
            // add child nodes to parent
            node.add(children);
            // traverse child nodes
            for(int i = 0; i < split.length; i++) {
                traverse(children[i]);
            }
        }
    }
    
    /**
     * Getter method for root node
     * @return 
     */
    public Id3Node root() {
        return root;
    }
    
    /**
     * Set the root node for traversal
     * @param root 
     */
    public void setRoot(Id3Node root) {
        this.root = root;
    }

    /**
     * Getter method for accuracy
     * @return 
     */
    public double accuracy() {
        return accuracy;
    }
    
    /**
     * Return true if the number of attributes tested is equal or greater than the
     * number of attributes in the instances key set
     * @param instances
     * @param attributesTested
     * @return 
     */
    public boolean attributesExhausted(Instances instances, List<String> attributesTested) {
        return attributesTested.size() >= instances.attributesmap().size();
    }
    
    /**
     * Prune the decision tree from the root node
     */
    public void prune() {
        prune(root());
    }
    
    /**
     * Prune the decision tree recursive
     */
    public void prune(Id3Node node) {
        if(node == null) return;
        // prune left and right nodes
        prune((Id3Node)node.left());
        prune((Id3Node)node.right());
        // prune this node
        if(node.left() != null && node.right() != null) {
            double branchPurity =
                (((Id3Node)node.left()).purity() +
                ((Id3Node)node.right()).purity()) / 2;
            if(node.purity() > branchPurity) {
                log.info("Pruning purity branch " + branchPurity + " node " + node.purity());
                node.setLeft(null);
                node.setRight(null);
                node.setClassifier(node.instances().majorityClassifier());
                log.info("Pruned node classifier " + node.classifier());
            }
        }
        return;
    }

    /**
     * Test instances on trained data
     * @param instances 
     */
    public void test(Instances instances) {
        testInstances = instances;
        // get instances in list form
        testInstance = instances.instances();
        // allocate similar list for classifications
        predicted = new ArrayList(testInstance.size());
        // classify each instance
        int matches = 0;
        for(int j = 0; j < testInstance.size(); j++) {
            String classification = classify(testInstance.get(j));
            predicted.add(j, classification);
            log.info("Actual classification " + testInstance.get(j).classifier() + ", predicted " + classification);
            if(classification.equals(testInstance.get(j).classifier())) matches++;
        }
        // compute accuracy
        accuracy = (double)matches / (double)testInstance.size();
        log.info("Test completed with " + matches + " out of " + testInstance.size() + " matches, accuracy " + accuracy);
    }
    
    /**
     * Classify instance from root node
     * @param instance
     * @return 
     */
    public String classify(Instance instance) {
        return classify(root(), instance);
    }
    
    /**
     * Classify instance with given node
     * @param node
     * @param instance
     * @return 
     */
    public String classify(Id3Node node, Instance instance) {
        // return node classification if defined
        if(node.classifier() != null) return node.classifier();
        // determine if the attribute on this node is continuous
        if(node.isContinuous()) {
            // traverse binary child nodes to get classification
            if(instance.valueDouble(node.attribute()) <= node.split()) {
                return classify((Id3Node)node.left(), instance);
            } else {
                return classify((Id3Node)node.right(), instance);
            }
        } else {
            // get attribute values for the node
            Set<String> values = node.instances().values(node.attribute());
            // get current attribute value for the instance
            String value = instance.value(node.attribute());
            // determine majority attrinbute value when current is missing
            if(!values.contains(value)) {
                value = node.instances().majorityAttributeValue(value);
            }
            // traverse discrete value child nodes to get classification
            for(Node inode: node.children()) {
                if(((Id3Node)inode).value().equals(value)) {
                    return classify((Id3Node)inode, instance);
                }
            }
        }
        // this point should be unreachable, but return the majority
        // classifier as a failsafe
        return node.instances().majorityClassifier();
    }
    
    /**
     * Compute entropy for the set of instances
     * @param instances
     * @return entropy value
     */
    private double computeEntropy(Instances instances) {
        // initialize counters for classifiers
        Map<String, Integer> counts = instances.classifierCounts();
        // compute entropy across all instances
        double entropy = 0;
        for(String classifier: counts.keySet()) {
            int count = counts.get(classifier);
            if(count > 0) {
                double probability = (double)count / (double)instances.size();
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }
        return entropy;
    }
    
    /**
     * Compute the attribute with the maximum information gain
     * @param instances
     * @param exclusion
     * @return 
     */
    private String computeMaxInfoGain(Instances instances, List<String> attributesTested) {
        // get list of attributes
        Set<String> attributes = instances.attributes();
        String[] attribute = attributes.toArray(new String[attributes.size()]);
        // initialize
        int maxIndex = -1;
        double maxInfoGain = 0;
        // compute maximum information gain across all attributes
        for(int i = 0; i < attribute.length; i++) {
            if(!attributesTested.contains(attribute[i])) {
                double infoGain = computeInfoGain(instances, attribute[i]);
                if(infoGain > maxInfoGain) {
                    maxInfoGain = infoGain;
                    maxIndex = i;
                }
            }
        }
        if(maxIndex >= 0) {
            log.info("Computed max info gain " + maxInfoGain + " on " + attribute[maxIndex]);
        } else {
            log.info("Computed max info gain error");
        }
        
        // TODO: Check this logic, if the maxIndex was not identified, then split on the first attribute. 
        // This is a temp fix, the reason why this condition would occur needs to be looked at
        return maxIndex >= 0 ? attribute[maxIndex] : attribute[0];
    }
    
    /**
     * Compute information gain for the set of instances and given attribute
     * @param instances
     * @param attribute
     * @return 
     */
    private double computeInfoGain(Instances instances, String attribute) {
        // compute entropy of instance set
        double infoGain = computeEntropy(instances);
        // split instance set across attribute values
        Instances[] split = instances.split(attribute);
        // compute information gain across all attribute values
        for(int i = 0; i < instances.values(attribute).size(); i++) {
            if(split[i].size() > 0) {
                infoGain -= ((double)split[i].size() / (double)instances.size()) *
                        computeEntropy(split[i]);
            }
        }
        log.info("Computed info gain " + infoGain + " for " + attribute);
        return infoGain;
    }
    
    /**
     * Compute binary split value for the set of instances and given attribute
     * @param instances
     * @param attribute 
     */
    private double computeBinarySplit(Instances instances, String attribute) {
        // get list of sorted values
        List<Double> values = Arrays.asList(instances.valuesDouble(attribute).toArray(new Double[0]));
        Collections.sort(values);
        // initialize values
        int pass = 0;
        double purity = 0;
        double min = values.get(0);
        double max = values.get(values.size()-1);
        double midpoint = (min + max) / 2;
        log.info("Starting split evaluation min " + min + " max " + max + " split " + midpoint);
        // loop until purity is 80% or 3 passes completed
        while(purity < 80 && pass++ < 3) {
            Instances[] split = instances.split(attribute, midpoint);
            double leftPurity = split[0].classifierPurity();
            double rightPurity = split[1].classifierPurity();
            log.info("Left split purity " + leftPurity + " right purity " + rightPurity);
            if(leftPurity > rightPurity) {
                purity = leftPurity;
                max = midpoint;
            } else {
                purity = rightPurity;
                min = midpoint;
            }
            midpoint = (min + max) / 2;
            log.info("Split pass " + pass + " min " + min + " max " + max + " split " + midpoint);
        }
        log.info("Computed binary split " + midpoint + " for " + attribute);
        return midpoint;
    }
    
    /**
     * Print the confusion matrix
     */
    public void printConfusionMatrix() {
        // print header
        System.out.println("Confusion Matrix:\n");
        System.out.format("%20s", "");
        for(String classifier: testInstances.classifiers()) {
            System.out.format("%20s | ", classifier);
        }
        System.out.println("\n");
        // get set of classifiers
        Set<String> classifiers = testInstances.classifiers();
        // print each classifier group
        for(String classifierGroup: classifiers) {
            // initialize counters
            Map<String, Integer> counts = new HashMap<String, Integer>();
            for(String classifier: classifiers) {
                counts.put(classifier, 0);
            }
            // compute counts for each classification
            for(int i = 0; i < testInstance.size(); i++) {
                Instance instance = testInstance.get(i);
                if(instance.classifier().equals(classifierGroup)) {
                    counts.put(predicted.get(i), counts.get(predicted.get(i)) + 1);
                }
            }
            // print results for classifier group
            System.out.format("%20s", classifierGroup);
            for(String classifier: classifiers) {
                System.out.format("%20s | ", counts.get(classifier));
            }
            System.out.println("\n");
        }
    }
}
