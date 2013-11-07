package fr.logica.jsf.model.treetable;

import java.util.HashMap;
import java.util.Map;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class BreakNode extends DefaultTreeNode {

	private Map<String, String> data = new HashMap<String, String>();;

	/**
	 * unique serial uid
	 */
	private static final long serialVersionUID = -3210510913649535529L;

	public BreakNode(String breakLabel, TreeNode parent) {
		super();
		setParent(parent);
		data.put("breakLabel", breakLabel);
	};

	public int getLeafCount() {
		int count = 0;
		for (TreeNode n : getChildren()) {
			if (n instanceof BreakNode) {
				count += ((BreakNode) n).getLeafCount();
			} else {
				count += 1;
			}
		}
		return count;
	}

	public void addChildCountToLabel() {
		data.put("breakLabel", data.get("breakLabel") + " (" + getLeafCount() + ")");
	}

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public String getType() {
		return "treetable-break-line";
	}

	public void expandAll() {
		setExpanded(true);
		for (TreeNode n : getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).expandAll();
			}
			n.setExpanded(true);
		}
	}

	public void collapseAll() {
		setExpanded(false);
		for (TreeNode n : getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).collapseAll();
			}
			n.setExpanded(false);
		}
	}

	public void selectAll() {
		setSelected(true);
		for (TreeNode n : getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).selectAll();
			}
			n.setSelected(true);
		}
	}

	public void unselectAll() {
		setSelected(false);
		for (TreeNode n : getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).unselectAll();
			}
			n.setSelected(false);
		}
	}
}
