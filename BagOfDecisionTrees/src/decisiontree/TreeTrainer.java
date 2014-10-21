package decisiontree;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TreeTrainer {
	private static final Log log = LogFactory.getLog(TreeTrainer.class);

	private Instances instances;

	/**
	 * Default constructor
	 * 
	 * @param instances
	 */
	public TreeTrainer(Instances instances) {
		this.instances = instances;
	}

	/**
	 * Get a random set of sub attributes
	 * 
	 * @param attributes
	 *            String array of attribute names
	 * @return
	 */
	protected String[] randomAttributeSplit(String[] attributes) {
		int subCount = (int) Math.round(Math.sqrt(attributes.length));
		ArrayList<String> list = new ArrayList<String>(
				Arrays.asList(attributes));

		Collections.shuffle(list);

		return (String[]) list.subList(0, subCount).toArray(new String[0]);
	}

	/**
	 * Get a random set of sub attributes
	 * 
	 * @param attributes
	 *            Attribute collection
	 * @return
	 */
	protected String[] randomAttributeSplit(Collection<Attribute> attributes) {
		String[] vals = new String[attributes.size()];
		int i = 0;

		for (Attribute attr : attributes) {
			vals[i] = attr.name();
			i++;
		}

		return randomAttributeSplit(vals);
	}

	/**
	 * Split a collection of Instances into a new collection with a smaller set
	 * of attributes that are picked at random
	 * 
	 * @param instances
	 * @return
	 */
	protected Instances splitInstancesByAttributesRandomly(Instances instances) {
		Instances subInstances = new Instances();
		String[] subAttributes = randomAttributeSplit(instances.attributes
				.values());

		// Loop over all the instances in this collection and pick out the
		// attributes
		// that were randomly chosen
		for (Instance instance : instances.instances()) {
			String[] values = new String[subAttributes.length];

			for (int i = 0; i < subAttributes.length; i++) {
				// Check for discrete or continuous values
				values[i] = Instance.isContinuous(subAttributes[i]) ? instance
						.valueDouble(subAttributes[i]).toString() : instance
						.value(subAttributes[i]);
			}

			subInstances.add(new Instance(subAttributes, values, instance
					.classifier()));
		}

		subInstances.map();

		return subInstances;
	}

	/**
	 * Create a new tree from a random set of attributes
	 * 
	 * @param count
	 *            Number of trees to return
	 * @return Array of trees trained on random attributes
	 */
	public Id3[] getTreesTrainedFromRandomAttributes(int count) {
		Id3[] trees = new Id3[count];

		for (int i = 0; i < count; i++) {
			log.info("Creating tree " + i + " from random attributes");
			Instances randomInstances = splitInstancesByAttributesRandomly(this.instances);

			trees[i] = new Id3(randomInstances);
			trees[i].traverse();
		}

		return trees;
	}
}
