package decisiontree;

public class ClassificationProgram {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		BagOfTrees botIn = new BagOfTrees();
		botIn.readBagFromFile("data/test.txt");

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

		// Pull out one of the trees from the bag and try to classify our test
		// records
		System.out.println(botIn.classifyByVote(instanceToClasify));
		System.out.println(botIn.classifyByVote(instanceToClasify2));
	}
}
