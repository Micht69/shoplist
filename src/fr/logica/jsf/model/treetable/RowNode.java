package fr.logica.jsf.model.treetable;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import fr.logica.business.data.Row;

public class RowNode extends DefaultTreeNode {

	/**
	 * serial UID
	 */
	private static final long serialVersionUID = -2895051726076202621L;

	/**
	 * Row data
	 */
	private Row data;

	public RowNode(Row r, TreeNode parent) {
		super();
		setParent(parent);
		data = r;
	}

	@Override
	public Object getData() {
		return data;
	}
}
