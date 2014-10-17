package decisiontree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class Attribute implements Serializable {
    private int index;
    private String name;
    private Set<String> values;

    /**
     * Constructor for attribute given name and set of values
     * @param name
     * @param values
     * @param classifier 
     */
    public Attribute(String name, Set<String> values, int index){
        this.name = name;
        this.index = index;
        this.values = values;
    }
    
    /**
     * Constructor for attribute given name but no values
     * @param name
     * @param index 
     */
    public Attribute(String name, int index) {
        this.name = name;
        this.index = index;
        this.values = new HashSet<String>();
    }

    /**
     * Add all values from the given set
     * @param values 
     */
    public void add(Set<String> values){
        this.values = values;
    }
    
    /**
     * Add the given value to the set
     * @param value 
     */
    public void add(String value) {
        values.add(value);
    }

    /**
     * Getter for attribute name
     * @return name of the attribute
     */
    public String name() {
        return name;
    }

    /**
     * Getter for attribute index
     * @return index of the attribute
     */
    public int index() {
        return index;
    }

    /**
     * Getter for the size of the attribute value set
     * @return size of value set
     */
    public int size() {
        return values.size();
    }

    /**
     * Getter for the set of values
     * @return set of values
     */
    public Set<String> values() {
        return values;
    }

    /**
     * Enumerate the values for the attribute
     * @return enumeration of the values
     */
    public Enumeration enumerate() {
        return Collections.enumeration(values);
    }
}
