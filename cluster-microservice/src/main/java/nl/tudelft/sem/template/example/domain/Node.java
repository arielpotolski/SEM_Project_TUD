package nl.tudelft.sem.template.example.domain;

public class Node {

	private double[] resources;  // GPU, CPU, Memory
	private String name;
	private String url;
	private String token;

	/**
	 * Constructor of the class.
	 *
	 * @param gpuRes first value of the resources array.
	 * @param cpuRes second value of the resources array.
	 * @param memRes third value of the resources array.
	 * @param token initializes the token of the node.
	 * @param url initializes the url of the node.
	 * @param name initializes the name given to the node.
	 */
	public Node(double gpuRes, double cpuRes, double memRes, String name,
				String url, String token) {
		this.resources = new double[] {gpuRes, cpuRes, memRes};
		this.name = name;
		this.url = url;
		this.token = token;
	}

	/**
	 * Returns the amount of GPU resources.
	 *
	 * @return GPU resources of this node.
	 */
	public double getGPU() {
		return this.resources[0];
	}

	/**
	 * Sets the amount of GPU resources of this node to the amount given as a parameter.
	 *
	 * @param gpuRes the amount of GPU resources.
	 */
	public void setGPU(double gpuRes) {
		this.resources[0] = gpuRes;
	}

	/**
	 * Returns the amount of CPU resources.
	 *
	 * @return CPU resources of this node.
	 */
	public double getCPU() {
		return this.resources[1];
	}

	/**
	 * Sets the amount of CPU resources of this node to the amount given as a parameter.
	 *
	 * @param cpuRes the amount of CPU resources.
	 */
	public void setCPU(double cpuRes) {
		this.resources[1] = cpuRes;
	}

	/**
	 * Returns the amount of memory resources.
	 *
	 * @return memory resources of this node.
	 */
	public double getMemory() {
		return this.resources[2];
	}

	/**
	 * Sets the amount of memory resources of this node to the amount given as a parameter.
	 *
	 * @param memRes the amount of memory resources.
	 */
	public void setMemory(double memRes) {
		this.resources[2] = memRes;
	}

	/**
	 * Gets and returns the name of this node.
	 *
	 * @return the name of this node.
 	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this node to the given string.
	 *
	 * @param name the name that this node should receive.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets and returns the url of this node.
	 *
	 * @return the url of this node.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url of this node to the one passed as a parameter.
	 *
	 * @param url the url that will be set to this node.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets and returns the token of this node.
	 *
	 * @return the token of this node.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the token of this node to the token passed as a parameter.
	 *
	 * @param token the token that this node will have.
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * This method checks if the node is valid to be added. For a node to be valid,
	 * it should have as much CPU resource as it has GPU and memory resources.
	 *
	 * @return a string that either says the node has been added (in case its valid),
	 * 		   or the reason why the node was considered invalid.
	 */
	public String enoughCPU() {
		if (this.resources[1] < this.resources[0] && this.resources[1] < this.resources[2]) {
			return "The amount of CPU resources should be at least as much as the amount of GPU" +
					" resources and at least as much as the amount of memory resources.";
		} else if (this.resources[1] < this.resources[0]) {
			return "The amount of CPU resources should be at least as much as the amount" +
					"of GPU resources.";
		} else if (this.resources[1] < this.resources[3]) {
			return "The amount of CPU resources should be at least as much as the amount" +
					"of memory resources.";
		} else {
			return "Your node has been successfully added.";
		}
	}
}
