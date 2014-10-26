package decisiontree;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TrainingProgram {
	private static final Log log = LogFactory.getLog(BagOfTrees.class);

	private String[] attributeNames;
	private String classifier;
	private ArrayList<String> rawTrainingData;

	public int totalClassifications;
	public int totalMisClassifications;

	private BagOfTrees bagOfTrees;

	/**
	 * Default constructor
	 */
	public TrainingProgram() {
		totalClassifications = 0;
		totalMisClassifications = 0;
	}

	/**
	 * Get number of trees in bag
	 */
	public int getBagOfTreesSize() {
		return bagOfTrees.count();
	}

	/**
	 * Load data, randomize it, and train trees
	 */
	public void Run(String path_to_file) {
		bagOfTrees = new BagOfTrees();

		loadData(path_to_file);

		trainTreesOnDataSplits();
	}

	/**
	 * Serialize bag of tree
	 */
	public void Save(String path_to_file) {

		log.info("Saving the trees (bag of trees) to file: " + path_to_file);
		bagOfTrees.serializeBagToFile(path_to_file);
	}

	/**
	 * Break up raw training data into small chunks and train trees off of those
	 * chunks
	 */
	private void trainTreesOnDataSplits() {
		// Break up the raw training data into small pieces that trees will be
		// trained from
		int dataSplitFactor = 100;
		int dataSplit = rawTrainingData.size() / dataSplitFactor;

		for (int i = 0; i < dataSplitFactor; i++) {
			if ((i % 10) == 0) {
				log.info("Creating trees for data split " + i);
			}
			int fromItem = i * dataSplit;
			// In case of odd numbers, make sure we catch the last record
			int toItem = (i == dataSplitFactor - 1) ? rawTrainingData.size()
					: fromItem + dataSplit;

			List<Instance> tempInstances = parseStringToInstance(rawTrainingData
					.subList(fromItem, toItem));

			// Train trees
			trainTrees(tempInstances, 10);

			// TODO: remove artificial break
			// if (i == 10) {
			// return;
			// }
		}
	}

	private List<Instance> parseStringToInstance(List<String> strings) {
		List<Instance> tempData = new ArrayList<Instance>(strings.size());

		RecordParser parser;

		// Create a list of Instance objects
		for (String s : strings) {
			// parse data record
			parser = new RecordParser(s);

			// add the instance attribute names and values
			tempData.add(new Instance(attributeNames, parser.values(), parser
					.classifier()));
		}
		return tempData;
	}

	/**
	 * Train trees from random attributes
	 */
	private void trainTrees(List<Instance> instanceList, int treeCount) {
		Id3[] trees = new Id3[treeCount];

		for (int i = 0; i < treeCount; i++) {
			// Take 66% of the instances at random and train a tree from them
			int trainingSize = instanceList.size() / 66;
			Instances instances = new Instances(instanceList.subList(0,
					trainingSize - 1));

			// Instantiate new TreeTrainer using the loaded instances
			TreeTrainer treeTrainer = new TreeTrainer(instances);

			// Add the tree to the trees array
			trees[i] = treeTrainer.getTreeTrainedFromRandomAttributes();

			// Test the tree's mis-classification rate across the unused 33% of
			// instances
			testTree(trees[i],
					instanceList.subList(trainingSize, instanceList.size()));
		}

		// Add to the bag the randomly trained trees
		bagOfTrees.addTrees(trees);
	}

	/**
	 * Test a tree given a list of Instance objects, and keep track of the
	 * missclassification counts
	 */
	private void testTree(Id3 tree, List<Instance> instanceList) {
		for (Instance instance : instanceList) {
			if (!instance.classifier().equals(tree.classify(instance))) {
				totalMisClassifications++;
			}
			totalClassifications++;
		}

	}

	/**
	 * Given a file name, load the data in the rawTrainingData list, then call
	 * randomizeData() to split it up into training and testing lists
	 */
	public void loadData(String path_to_file) {

		rawTrainingData = new ArrayList<String>();

		log.info("Loading data set");
		String fileRow;
		RecordParser p;
		FileInputStream fis = null;
		BufferedReader reader = null;
		try {
			fis = new FileInputStream(path_to_file);
			reader = new BufferedReader(new InputStreamReader(fis));
			// parse header record
			p = new RecordParser(reader.readLine());
			// get attribute names from header
			attributeNames = p.values();
			log.info("Attribute names " + Arrays.toString(attributeNames));
			// get classifier name from header
			classifier = p.classifier();
			log.info("Classifier name " + classifier);
			// parse remaining records
			while ((fileRow = reader.readLine()) != null) {
				rawTrainingData.add(fileRow);
			}
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException issued");
		} catch (IOException e) {
			System.out.println("IOException issued");
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				System.out.println("IOException when closing file");
			}
		}

		log.debug("Loaded " + rawTrainingData.size() + " records");

		randomizeData();
	}

	/**
	 * Randomize all data that was loaded into the training program and add 33%
	 * of it to the testing data set and retain 66% in the training data set
	 */
	public void randomizeData() {
		Collections.shuffle(rawTrainingData);

		log.debug("Training data contains " + rawTrainingData.size()
				+ " records");
	}

	public static void main(String[] args) {
		long t = System.currentTimeMillis();

		// testBagOfTrees();
		String PATH_TO_FILE = "data/kddcup.data_10_percent.txt"; // kddcup.data_10_percent.txt
		String PATH_TO_SERIALIZED_BOT = "data/kddcup.trees";
		TrainingProgram trainingProgram = new TrainingProgram();
		trainingProgram.Run(PATH_TO_FILE);
		trainingProgram.Save(PATH_TO_SERIALIZED_BOT);

		int count = trainingProgram.getBagOfTreesSize();

		System.out.println("TreeBagCount: " + count);
		System.out.println("Out of bag error rate: "
				+ trainingProgram.totalMisClassifications + " / "
				+ trainingProgram.totalClassifications);

		System.out.println("Runtime: "
				+ (((System.currentTimeMillis() - t) / 1000) / 60) + "minutes");
	}

	/**
	 * Quick test to make sure a tree can be serialized/de-serialized
	 */
	public static void testBagOfTrees() {

		String PATH_TO_FILE = "data/kddcup.data.txt"; // kddcup.data_xsm.txt
		// //iris.data
		// //kddcup.data_2_percent.txt

		// Create some test instances that we can try and classify
		String[] names = { "#duration", "@protocol_type", "@service", "@flag",
				"#src_bytes", "#dst_bytes", "@land", "#wrong_fragment",
				"#urgent", "#hot", "#num_failed_logins", "@logged_in",
				"#num_compromised", "#root_shell", "#su_attempted",
				"#num_root", "#num_file_creations", "#num_shells",
				"#num_access_files", "#num_outbound_cmds", "@is_host_login",
				"@is_guest_login", "#count", "#srv_count", "#serror_rate",
				"#srv_serror_rate", "#rerror_rate", "#srv_rerror_rate",
				"#same_srv_rate", "#diff_srv_rate", "#srv_diff_host_rate",
				"#dst_host_count", "#dst_host_srv_count",
				"#dst_host_same_srv_rate", "#dst_host_diff_srv_rate",
				"#dst_host_same_src_port_rate", "#dst_host_srv_diff_host_rate",
				"#dst_host_serror_rate", "#dst_host_srv_serror_rate",
				"#dst_host_rerror_rate", "#dst_host_srv_rerror_rate" };

		String[] valuesSmurf = { "0", "icmp", "ecr_i", "SF", "1032", "0", "0",
				"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0",
				"0", "0", "0", "511", "511", "0.00", "0.00", "0.00", "0.00",
				"1.00", "0.00", "0.00", "255", "255", "1.00", "0.00", "1.00",
				"0.00", "0.00", "0.00", "0.00", "0.00" };
		String[] valuesNormal = { "0", "tcp", "http", "SF", "336", "3841", "0",
				"0", "0", "0", "0", "1", "0", "0", "0", "0", "0", "0", "0",
				"0", "0", "0", "7", "11", "0.00", "0.00", "0.00", "0.00",
				"1.00", "0.00", "0.27", "33", "255", "1.00", "0.00", "0.03",
				"0.07", "0.00", "0.00", "0.00", "0.00" };

		Instance instanceToClasify = new Instance(names, valuesSmurf, null);
		Instance instanceToClasify2 = new Instance(names, valuesNormal, null);
	}
}
