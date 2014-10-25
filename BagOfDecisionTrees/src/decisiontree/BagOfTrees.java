package decisiontree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BagOfTrees {
	private static final Log log = LogFactory.getLog(BagOfTrees.class);

	private List<Id3> bagOfTrees;

	/**
	 * Default constructor
	 */
	public BagOfTrees() {
		bagOfTrees = new ArrayList<Id3>();
	}

	/**
	 * Add a tree to the collection
	 * 
	 * @param tree
	 */
	public void addTree(Id3 tree) {
		bagOfTrees.add(tree);
	}

	/**
	 * Add an array of trees to the collection
	 * 
	 * @param trees
	 */
	public void addTrees(Id3[] trees) {
		bagOfTrees.addAll(Arrays.asList(trees));
	}

	/**
	 * Serialize to a file output the list of trees that are currently held in
	 * the bag
	 * 
	 * @param filePath
	 *            file path that the bag of trees will be saved to
	 */
	public void serializeBagToFile(String filePath) {

		try {
			FileOutputStream fout = new FileOutputStream(filePath);
			ObjectOutputStream oos = new ObjectOutputStream(fout);

			oos.writeObject(bagOfTrees);
			oos.flush();
			fout.close();

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
	 * 
	 * @param filePath
	 *            file path that contains bag of trees
	 */
	public void readBagFromFile(String filePath) {
		try {
			FileInputStream fin = new FileInputStream(filePath);
			ObjectInputStream ois = new ObjectInputStream(fin);

			bagOfTrees = (ArrayList<Id3>) ois.readObject();

			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Returns the Id3 tree at the specified position in the list
	 * 
	 * @param index
	 *            position in the list
	 * @return Id3 tree
	 */
	public Id3 get(int index) {
		return bagOfTrees.get(index);
	}
        
        /**
         * Returns the size of the bag of trees array
         * @return size of the bag of trees array
         */
        public int count() {
            return bagOfTrees.size();
        }

	/**
	 * Vote on the most common classification for the given instance
	 * 
	 * @param instance
	 * @return
	 */
	public String classifyByVote(Instance instance) {
		HashMap<String, Integer> possibleClassifications = new HashMap<String, Integer>();

		// Use each tree in the bag to classify the instance
		for (Id3 tree : bagOfTrees) {
			String classification = tree.classify(instance);

			// If we haven't encountered this classification before, add it to
			// the map
			if (!possibleClassifications.keySet().contains(classification)) {
				possibleClassifications.put(classification, 0);
			}

			possibleClassifications.put(classification, possibleClassifications
					.get(classification).intValue() + 1);
		}

		// Return them most popular tree
		String mostPopularClassification = null;
		for (String key : possibleClassifications.keySet()) {
			if (mostPopularClassification == null) {
				mostPopularClassification = key;
			}

			mostPopularClassification = (possibleClassifications.get(key) > possibleClassifications
					.get(mostPopularClassification)) ? key
					: mostPopularClassification;
		}

		return mostPopularClassification;
	}

	/*public static void main(String[] args) {
            log.info("Starting BagOfTrees execution");
		testBagOfTrees();

	}

	//Quick test to make sure a tree can be serialized/de-serialized
	public static void testBagOfTrees() {

		String PATH_TO_FILE = "data/kddcup.data_2_percent.txt"; //kddcup.data_xsm.txt //iris.data //kddcup.data_2_percent.txt 
		
		log.info("Loading instances from file");

		// Load instances from file
		Instances instances = new Instances(new File(PATH_TO_FILE));
		
		log.info("Instantiating a new tree trainer with the loaded instances");

		// Instantiate new TreeTrainer using the loaded instances
		TreeTrainer treeTrainer = new TreeTrainer(instances);

		log.info("Instantiating new bag of trees");
		// Instantiate new BagOfTrees
		BagOfTrees bot = new BagOfTrees();
		
		log.info("Add 10 new randomly created trees to the bag");
		// Add 10 trees trained on random attributes to the bag of trees
		bot.addTrees(treeTrainer.getTreesTrainedFromRandomAttributes(1));
		
		// Serialize the bag to an out file
                log.info("Serializing bag of trees to file");
		bot.serializeBagToFile("data/test.txt");
		
		bot = null;
		instances = null;
		treeTrainer = null;
		
		log.info("Deserializing bag of trees from file");
		BagOfTrees botIn = new BagOfTrees();
		botIn.readBagFromFile("data/test.txt");
                log.info("Serialized file loaded " + botIn.count() + " trees");
		
		
		// Create some test instances that we can try and classify
		String[] names = { "#sepal-length", "#sepal-width", "#petal-length",
				"#petal-width" };

		String[] valuesSersota = { "5.5", "4.2", "1.4", "0.2" };
		String[] valuesVersicolor = { "5.7", "2.6", "3.5", "1.0" };

		Instance instanceToClasify = new Instance(names, valuesSersota, null);// "Iris-setosa"
		Instance instanceToClasify2 = new Instance(names, valuesVersicolor,
				null); // "Iris-versicolor"

		// Create a new bag of trees that will read in the previously serialized
		// tree set
		BagOfTrees botIn = new BagOfTrees();
		botIn.readBagFromFile("data/test.txt");

		// Pull out one of the trees from the bag and try to classify our test
		// records
		System.out.println(botIn.classifyByVote(instanceToClasify));
		System.out.println(botIn.classifyByVote(instanceToClasify2));
	}*/

}
