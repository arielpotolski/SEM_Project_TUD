package nl.tudelft.sem.template.cluster.domain.cluster;

import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "nodes")
@NoArgsConstructor
public class Node {

	/**
	 * Identifier for the node in the cluster.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false, unique = true)
	private long id;

	@Column(name = "cpuResources", nullable = false)
	private double cpuResources;

	@Column(name = "gpuResources", nullable = false)
	private double gpuResources;

	@Column(name = "memoryResources", nullable = false)
	private double memoryResources;

	@Column(name = "node_name", nullable = false)
	private String name;

	@Column(name = "url", nullable = false, unique = true)
	private String url;

	/**
	 * Constructor of the class.
	 *
	 * @param gpuRes first value of the resources array.
	 * @param cpuRes second value of the resources array.
	 * @param memRes third value of the resources array.
	 * @param url initializes the url of the node.
	 * @param name initializes the name given to the node.
	 */
	public Node(double cpuRes, double gpuRes, double memRes, String name, String url) {
		this.cpuResources = cpuRes;
		this.gpuResources = gpuRes;
		this.memoryResources = memRes;
		this.name = name;
		this.url = url;
	}

	/**
	 * Returns the amount of GPU resources.
	 *
	 * @return GPU resources of this node.
	 */
	public double getGPU() {
		return this.gpuResources;
	}

	/**
	 * Sets the amount of GPU resources of this node to the amount given as a parameter.
	 *
	 * @param gpuRes the amount of GPU resources.
	 */
	public void setGPU(double gpuRes) {
		this.gpuResources = gpuRes;
	}

	/**
	 * Returns the amount of CPU resources.
	 *
	 * @return CPU resources of this node.
	 */
	public double getCPU() {
		return this.cpuResources;
	}

	/**
	 * Sets the amount of CPU resources of this node to the amount given as a parameter.
	 *
	 * @param cpuRes the amount of CPU resources.
	 */
	public void setCPU(double cpuRes) {
		this.cpuResources = cpuRes;
	}

	/**
	 * Returns the amount of memory resources.
	 *
	 * @return memory resources of this node.
	 */
	public double getMemory() {
		return this.memoryResources;
	}

	/**
	 * Sets the amount of memory resources of this node to the amount given as a parameter.
	 *
	 * @param memRes the amount of memory resources.
	 */
	public void setMemory(double memRes) {
		this.memoryResources = memRes;
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
	 * This method checks if the node is valid to be added. For a node to be valid,
	 * it should have as much CPU resource as it has GPU and memory resources.
	 *
	 * @return a string that either says the node has been added (in case its valid),
	 * 		   or the reason why the node was considered invalid.
	 */
	public String hasEnoughCPU() {
		if (this.cpuResources < this.gpuResources && this.cpuResources < this.memoryResources) {
			return "The amount of CPU resources should be at least as much as the amount of GPU" +
					" resources and at least as much as the amount of memory resources.";
		} else if (this.cpuResources < this.gpuResources) {
			return "The amount of CPU resources should be at least as much as the amount" +
					"of GPU resources.";
		} else if (this.cpuResources < this.memoryResources) {
			return "The amount of CPU resources should be at least as much as the amount" +
					"of memory resources.";
		} else {
			return "Your node has been successfully added.";
		}
	}

	/**
	 * Compare this node to another. Use all parameters besides ID.
	 *
	 * @param o the other to compare this node to.
	 * @return boolean indicating whether this is the same node as o.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Node node = (Node) o;
		return Double.compare(node.cpuResources, this.cpuResources) == 0
				&& Double.compare(node.gpuResources, this.gpuResources) == 0
				&& Double.compare(node.memoryResources, this.memoryResources) == 0
				&& this.name.equals(node.name) && this.url.equals(node.url);
	}

	/**
	 * Hash this node.
	 *
	 * @return the integer hash code of this node object.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.cpuResources, this.gpuResources,
			this.memoryResources, this.name, this.url);
	}
}
