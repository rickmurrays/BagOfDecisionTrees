package decisiontree;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Instance implements Serializable {
    private Map<String, Double> values;
    private String classifier;

    /**
     * Constructor for instance given an array of names and values
     * @param names
     * @param values
     * @param classifier 
     */
    public Instance(String[] names, Double[] values, String classifier){
        this.values = new LinkedHashMap<String, Double>();
        this.classifier = classifier;
        for(int i = 0; i < names.length; i++){
            this.values.put(names[i], values[i]);
        }
    }
    
    /**
     * Constructor for instance given a map of names and values
     * @param attributes
     * @param classifier 
     */
    public Instance(Map<String, Double> attributes, String classifier){
        this.classifier = classifier;
        this.values = attributes;
    }
    
    /**
     * Getter method for list of attribute names on this instance
     * @return set of attribute names
     */
    public Set<String> attributes(){
        return values.keySet();
    }
    
    /**
     * Getter method for a specific attribute value
     * @param name
     * @return 
     */
    public Double value(String name){
        return values.get(name);
    }

    /**
     * Getter method for the instance classifier
     * @return 
     */
    public String classifier() {
        return classifier;
    }
}
