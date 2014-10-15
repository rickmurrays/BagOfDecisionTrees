package decisiontree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BagOfTrees {
	
	private List<Id3> bagOfTrees;
	
	public BagOfTrees(){
		bagOfTrees = new ArrayList<Id3>();
	}
	
	public void addTree(Id3 tree){
		bagOfTrees.add(tree);
	}

	
	/**
	 * Serialize to a file output the list of trees that are currently held in the bag
	 * @param filePath file path that the bag of trees will be saved to
	 */
	public void serializeBagToFile(String filePath){
				
		try {
			FileOutputStream fout =  new FileOutputStream(filePath);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			
			oos.writeObject(bagOfTrees);
			oos.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * De-serialize from a file a list of trees
	 * @param filePath file path that contains bag of trees
	 */
	public void readBagFromFile(String filePath){
		try{
			FileInputStream fin = new FileInputStream(filePath);
			ObjectInputStream ois = new ObjectInputStream(fin);
			
			bagOfTrees  = (ArrayList<Id3>) ois.readObject();
			
			ois.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns the Id3 tree at the specified position in the list
	 * @param index position in the list
	 * @return Id3 tree
	 */
	public Id3 get(int index){
		return bagOfTrees.get(index);
	}
	
	
	
	 /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	testBagOfTrees();

    }
    
    public static void testBagOfTrees(){

        /**
         * Instantiate decision tree with static data set
         */
        DecisionTree dt = new DecisionTree("data/iris.data");
        dt.crossValidation();
        
        BagOfTrees bot = new BagOfTrees();
        
        bot.addTree(dt.getBestTrainter());
        bot.serializeBagToFile("data/test.txt");
        
        Map<String, Double> attributes = new HashMap<String, Double>();
        attributes.put("sepal-length", 5.5);
        attributes.put("sepal-width", 4.2);
        attributes.put("petal-length", 1.4);
        attributes.put("petal-width", 0.2);
		String classifier = "Iris-setosa";
		
		Instance instanceToClasify = new Instance(attributes, classifier);
		
		BagOfTrees botIn = new BagOfTrees();
		botIn.readBagFromFile("data/test.txt");
		
		Id3 tree = botIn.get(0);
		System.out.println(tree.classify(instanceToClasify));
    }

}
