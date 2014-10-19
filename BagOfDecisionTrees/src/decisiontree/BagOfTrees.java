package decisiontree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BagOfTrees {

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
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		testBagOfTrees();

	}

	/**
	 * Quick test to make sure a tree can be serialized/de-serialized
	 */
	public static void testBagOfTrees() {

		String PATH_TO_FILE = "data/iris.data";

		// Load instances from file
		Instances instances = new Instances(new File(PATH_TO_FILE));

		// Instantiate new TreeTrainer using the loaded instances
		TreeTrainer treeTrainer = new TreeTrainer(instances);

		// Instantiate new BagOfTrees
		BagOfTrees bot = new BagOfTrees();

		// Add 10 trees trained on random attributes to the bag of trees
		bot.addTrees(treeTrainer.getTreesTrainedFromRandomAttributes(10));

		// Serialize the bag to an out file
		bot.serializeBagToFile("data/test.txt");

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
		Id3 tree = botIn.get(9);
		System.out.println(tree.classify(instanceToClasify));
		System.out.println(tree.classify(instanceToClasify2));
	}

}
