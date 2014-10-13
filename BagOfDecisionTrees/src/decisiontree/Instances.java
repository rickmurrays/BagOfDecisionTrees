package decisiontree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Instances {
    private static final Log log = LogFactory.getLog(Instances.class);
    // attributes derived from instances
    public Map<String, Attribute> attributes;
    // instances for the set of data
    private List<Instance> instances;
    
    /**
     * Default constructor for instances
     */
    public Instances(){
        instances = new LinkedList<Instance>();
    }
    
    /**
     * Constructor for instances given an input file
     * @param f 
     */
    public Instances(File f) {
        this();
        // check for null file
        if(f == null) return;
        // load file containing data set
        log.info("Instances constructor");
        loadDataset(f);
        // map attributes from set of instances
        log.info("Mapping attributes");
        map();
    }
    
    /**
     * Add instance to the linked list given an array of names and values
     * @param names
     * @param values
     * @param classifier 
     */
    private void add(String[] names, Double[] values, String classifier){
        instances.add(new Instance(names, values, classifier));
    }
    
    /**
     * Add instance to the linked list given a map of names and values
     * @param attributes
     * @param classifier 
     */
    private void add(Map<String, Double> attributes, String classifier){
        instances.add(new Instance(attributes, classifier));
    }
    
    /**
     * Add instance to the linked list
     * @param instance 
     */
    private void add(Instance instance) {
        // add instance
        instances.add(instance);
    }

    /**
     * Add all instances to the linked list
     * @param instances 
     */
    public void addAll(Instances instances) {
        // add all instances to this set
        for(Instance instance: instances.instances()) {
            this.add(instance);
        }
        // map attributes
        map();
    }

    /**
     * Getter method for the map of attributes to values
     * @return map of attributes
     */
    public Map<String, Attribute> attributesmap() {
        return attributes;
    }
    
    /**
     * Getter method for the set of attributes on all instances
     * @return 
     */
    public Set<String> attributes(){
        Set<String> attributes = new LinkedHashSet<String>();
        for(Instance instance: instances){
            attributes.addAll(instance.attributes());
        }
        return attributes;
    }
    
    /**
     * Getter method for the set of values of a specific attribute
     * on all instances
     * @param name
     * @return 
     */
    public Set<Double> values(String name){
        Set<Double> values = new LinkedHashSet<Double>();
        for(Instance instance: instances){
            values.add(instance.value(name));
        }
        return values;
    }
    
    /**
     * Getter method for size of instance set
     * @return size of instance set
     */
    public int size() {
        return instances.size();
    }
    
    /**
     * Getter method for list of instances
     * @return 
     */
    public List<Instance> instances() {
        return instances;
    }
    
    /**
     * Getter for enumeration of instances
     * @return enumeration of instances
     */
    public Enumeration enumerate() {
        return Collections.enumeration(instances);
    }
    
    /**
     * Getter method for the set of all classifiers
     * @return 
     */
    public Set<String> classifiers() {
        Set<String> classifiers = new LinkedHashSet<String>();
        for(Instance instance: instances){
            classifiers.add(instance.classifier());
        }
        return classifiers;
    }
    
    /**
     * Compute classifier counts for the set of instances
     * @param instances
     * @return map of classifiers and counts
     */
    public Map<String, Integer> classifierCounts() {
        // get the list of classifiers
        Set<String> classifiers = classifiers();
        // initialize counters for classifiers
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for(String classifier: classifiers) {
            counts.put(classifier, 0);
        }
        // count classifiers for all instances
        for(Instance instance: instances()) {
            String classifier = instance.classifier();
            counts.put(classifier, counts.get(classifier) + 1);
        }
        return counts;
    }
    
    /**
     * Compute the majority classifier for the set of instances
     * @param instances
     * @return majority classifier
     */
    public String majorityClassifier() {
        // initialize counters for classifiers
        Map<String, Integer> counts = classifierCounts();
        // initialize
        int maxCount = 0;
        String maxClassifier = "";
        // compute majority count
        for(String classifier: counts.keySet()) {
            if(counts.get(classifier) > maxCount) {
                maxCount = counts.get(classifier);
                maxClassifier = classifier;
            }
        }
        return maxClassifier;
    }
    
    /**
     * Compute the percent purity of the majority classifier
     * @param instances
     * @return majority classifier count
     */
    public double classifierPurity() {
        // initialize counters for classifiers
        Map<String, Integer> counts = classifierCounts();
        // initialize
        int maxCount = 0;
        // compute majority count
        for(String classifier: counts.keySet()) {
            if(counts.get(classifier) > maxCount) {
                maxCount = counts.get(classifier);
            }
        }
        return ((double)maxCount / (double)size()) * 100;
    }
    
    /**
     * Split instances given a number of subsets
     * @param k
     * @return array of instances
     */
    public Instances[] split(int k) {
        // check if k is sufficiently large
        if(k <= 1) {
            log.info("Unable to perform split subsets, number of subsets too small");
            return null;
        }
        // allocate array of instances for size of value set
        Instances[] split = new Instances[k];
        // instantiate instances for each value
        for(int i = 0; i < k; i++) {
            split[i] = new Instances();
        };
        // split instances using round robin approach
        int group = 0;
        for(Instance instance: instances) {
            split[group++].add(instance);
            if(group == k) group = 0;
        }
        // map attributes for each split
        for(int i = 0; i < k; i++) {
            split[i].map();
        }
        return split;
    }
    
    /**
     * Split instances given an attribute name using discrete values
     * @param attribute
     * @return array of instances
     */
    public Instances[] split(String attribute) {
        // check if attribute name exists
        if(!attributes.containsKey(attribute)) {
            log.info("Unable to split instances, attribute " + attribute + " not found");
            return null;
        }
        // get attribute value set
        Set<Double> values = attributes.get(attribute).values();
        // allocate array of instances for size of value set
        Instances[] split = new Instances[values.size()];
        // instantiate instances for each value
        for(int i = 0; i < values.size(); i++) {
            split[i] = new Instances();
        };
        // convert value set to array list for indexing
        List<Double> indexed = new ArrayList<Double>(values);
        // split instances using indexed attribute value
        for(Instance instance: instances) {
            split[indexed.indexOf(instance.value(attribute))].add(instance);
        }
        // map attributes for each split
        for(int i = 0; i < values.size(); i++) {
            split[i].map();
        }
        return split;
    }
    
    /**
     * Split instances given an attribute and value using continuous values
     * @param attribute
     * @param value
     * @return 
     */
    public Instances[] split(String attribute, Double value) {
        // check if attribute name exists
        if(!attributes.containsKey(attribute)) {
            log.info("Unable to split instances, attribute " + attribute + " not found");
            return null;
        }
        // binary split
        int k = 2;
        // get attribute value set
        Set<Double> values = attributes.get(attribute).values();
        // allocate array of instances for size of value set
        Instances[] split = new Instances[k];
        // instantiate instances for each value
        for(int i = 0; i < k; i++) {
            // instantiate with existing attributes map
            split[i] = new Instances();
        };
        // split instances using indexed attribute value
        for(Instance instance: instances) {
            if((Double)instance.value(attribute) <= value) {
                split[0].add(instance);
            } else {
                split[1].add(instance);
            }
        }
        // map attributes for each split
        for(int i = 0; i < k; i++) {
            split[i].map();
        }
        return split;
    }

    /**
     * Merge all instances within the array, excluding the given index
     * @param instances
     * @param index
     * @return 
     */
    public static Instances merge(Instances[] instances, int index) {
        Instances merged = new Instances();
        for(int i = 0; i < instances.length; i++) {
            if(i != index) merged.addAll(instances[i]);
        }
        // map attributes
        merged.map();
        return merged;
    }
    
    /**
     * Load the data set records and create all instances
     * @param f 
     */
    public void loadDataset(File f){
        log.info("Loading data set");
        String r;
        RecordParser p;
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(f);
            reader = new BufferedReader(new InputStreamReader(fis));
            // parse header record
            p = new RecordParser(reader.readLine());
            // get attribute names from header
            String[] names = p.values();
            log.info("Attribute names " + Arrays.toString(names));
            // get classifier name from header
            String classifier = p.classifier();
            log.info("Classifier name " + classifier);
            // parse remaining records
            while(( r = reader.readLine()) != null) {
                // parse data record
                p = new RecordParser(r);
                // add the instance attribute names and values
                add(names, p.doubles(), p.classifier());
            }
        } catch (FileNotFoundException e){
            System.out.println("FileNotFoundException issued");
        } catch (IOException e) {
            System.out.println("IOException issued");
        } finally {
            try {
                if(reader != null) reader.close();
                if(fis != null) fis.close();
            } catch (IOException e) {
                System.out.println("IOException when closing file");
            }
        }
    }
    
    /**
     * Map attributes from set of instances
     */
    private void map() {
        int index = 0;
        attributes = new HashMap<String, Attribute>();
        for(String name: attributes()) {
            attributes.put(name, new Attribute(name, values(name), index++));
        }
    }
}
