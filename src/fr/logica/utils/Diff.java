package fr.logica.utils;

import java.io.Serializable;

/** Difference between two element. Used to compare a user's modification with modifications already persisted. */
public class Diff implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String label;
	private final Serializable mine;
	private final Serializable theirs;
	private final boolean hasValues;

	/** Difference noted, but not storing the values (they are BLOBs or otherwise too heavy) */
	public Diff(String label) {
		this.label = label;
		this.mine = null;
		this.theirs = null;
		this.hasValues = false;
	}

	/** Difference with the associated values */
	public Diff(String label, Serializable mine, Serializable theirs) {
		this.label = label;
		this.mine = mine;
		this.theirs = theirs;
		this.hasValues = true;
	}

	public Diff(String label, Object mine, Object theirs) {
		this(label, toSerializable(mine), toSerializable(theirs));
	}

	public Serializable getLabel() {
		return label;
	}

	public Serializable getMine() {
		return mine;
	}

	public Serializable getTheirs() {
		return theirs;
	}

	public boolean hasValues() {
		return hasValues;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(label);
		builder.append(" : ").append(mine).append(" <=> ").append(theirs);
		return builder.toString();
	}

	private static Serializable toSerializable(Object value) {
		if (value == null) {
			return null;
		} else if (value instanceof Serializable) {
			return (Serializable) value;
		} else {
			return value.toString();
		}
	}

}
