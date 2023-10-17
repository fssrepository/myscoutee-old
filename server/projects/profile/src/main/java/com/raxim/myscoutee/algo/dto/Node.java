package com.raxim.myscoutee.algo.dto;

public class Node implements Comparable<Node> {

	private final String id;
	private final String type;

	public Node() {
		this(null, null);
	}

	public Node(String id) {
		this(id, null);
	}

	public Node(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Node [id=" + id + ", type=" + type + "]";
	}

	@Override
	public int compareTo(Node arg0) {
		return getId().compareTo(arg0.getId());
	}

}
