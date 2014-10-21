package fr.logica.jsf.model.treetable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class BreakNode extends DefaultTreeNode {

	private Map<String, Object> data = new HashMap<String, Object>();

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

	public void sum(Set<String> sumColumns) {
		Map<String, Number> sums = new HashMap<String, Number>();
		for (TreeNode n : getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).sum(sumColumns);
			}
		}
		for (TreeNode n : getChildren()) {
			for (String col : sumColumns) {
				if (!"".equals(data.get(col))) {
					Object o = ((Map<String, Object>) n.getData()).get(col);
					if (o == null || "".equals(o)) {
						// we skip empty values
						continue;
					}
					if (!(o instanceof Number)) {
						// We can't sum non numeric values
						data.put(col, "");
					} else {
						if (sums.get(col) == null) {
							sums.put(col, 0);
						}
						if (o instanceof Integer) {
							sums.put(col, sums.get(col).intValue() + ((Number) o).intValue());
						} else {
							sums.put(col, sums.get(col).floatValue() + ((Number) o).floatValue());
						}
					}
				}
			}
		}
		for (String col : sumColumns) {
			data.put(col, sums.get(col));
		}
	}
}
