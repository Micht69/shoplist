package fr.logica.jsf.model.backref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.business.data.ListCategoryData;
import fr.logica.business.data.Row;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.treetable.BreakNode;
import fr.logica.jsf.model.treetable.RowNode;

public class BackRefListCategoryModel extends BackRefListModel {

	/** serialUID */
	private static final long serialVersionUID = 6839569469845393441L;

	public BackRefListCategoryModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String queryName,
			String linkName, String filterName) {
		super(viewCtrl, store, entity, entityName, queryName, linkName, filterName);
	}

	@Override
	public void loadData(RequestContext context) {
		data = new BusinessController().getBackRefListData(entity, entityName, linkName, queryName, viewCtrl.getCurrentView().getAction(),
				context);
		if (!(data instanceof ListCategoryData)) {
			data = new ListCategoryData(data);
		}
		loadTree((ListCategoryData) data);
	}

	/**
	 * Root node of the tree table. This node is not displayed.
	 */
	private TreeNode root;

	/**
	 * Selected elements
	 */
	private TreeNode[] selectedNodes;

	/**
	 * Caption of the column with breaks
	 */
	private String breakColumnCaption;

	public void loadTree(ListCategoryData data) {
		Map<String, BreakNode> nodeMap = new HashMap<String, BreakNode>();

		root = new DefaultTreeNode("root", null);
		List<String> breakers = data.getCategoryBreak();

		if (breakers.size() > 0) {
			for (int i = 0; i < breakers.size(); i++) {
				String breaker = breakers.get(i);

				for (Row r : data.getRows()) {
					String breakValue = (String) r.get(breaker);
					StringBuilder key = new StringBuilder();
					String parentKey;

					for (int j = 0; j < i; j++) {
						key.append((String) r.get(breakers.get(j)));
					}
					parentKey = key.toString();
					key.append(breakValue);

					if (nodeMap.get(key.toString()) == null) {
						TreeNode parent = i == 0 ? root : nodeMap.get(parentKey);
						BreakNode node = new BreakNode(breakValue, parent);
						nodeMap.put(key.toString(), node);
					}
				}
			}

			for (Row r : data.getRows()) {
				StringBuilder key = new StringBuilder();
				for (int i = 0; i < breakers.size(); i++) {
					key.append((String) r.get(breakers.get(i)));
				}
				new RowNode(r, nodeMap.get(key.toString()));
			}

		} else {
			for (Row r : data.getRows()) {
				new RowNode(r, root);
			}
		}

		for (BreakNode n : nodeMap.values()) {
			n.addChildCountToLabel();
		}

		breakColumnCaption = "";
		for (String b : breakers) {
			if (breakColumnCaption.length() > 0) {
				breakColumnCaption += " / ";
			}
			breakColumnCaption += data.getColumns().get(b).getTitle();
		}
	}

	public String getBreakColumnCaption() {
		return breakColumnCaption;
	}

	public TreeNode[] getSelectedNodes() {
		return selectedNodes;
	}

	public void setSelectedNodes(TreeNode[] pSelectedNodes) {
		if (pSelectedNodes == null) {
			this.selectedNodes = null;
		} else {
			this.selectedNodes = Arrays.copyOf(pSelectedNodes, pSelectedNodes.length);
		}
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	@Override
	public List<Key> getSelected() {
		List<Key> keys = new ArrayList<Key>();
		if (selectedNodes == null) {
			return keys;
		}
		for (TreeNode n : selectedNodes) {
			if (n instanceof RowNode) {
				addOnceToList(((Row) n.getData()).getPk(), keys);
			}
			if (n instanceof BreakNode) {
				addChildren((BreakNode) n, keys);
			}
		}
		return keys;
	}

	public void addChildren(BreakNode node, List<Key> keys) {
		for (TreeNode n : node.getChildren()) {
			if (n instanceof RowNode) {
				addOnceToList(((Row) n.getData()).getPk(), keys);
			}
			if (n instanceof BreakNode) {
				addChildren((BreakNode) n, keys);
			}
		}
	}

	/**
	 * Adds a primary key to the list if it's not already present. We don't use a Set because elements order matters.
	 * 
	 * @param pk Key to add to the list
	 * @param keys Key list
	 */
	private void addOnceToList(Key pk, List<Key> keys) {
		if (pk == null || keys == null) {
			return;
		}
		for (Key k : keys) {
			if (k.getEncodedValue().equals(pk.getEncodedValue())) {
				return;
			}
		}
		keys.add(pk);
	}

	public void expandAll() {
		root.setExpanded(true);
		for (TreeNode n : root.getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).expandAll();
			}
			n.setExpanded(true);
		}
	}

	public void collapseAll() {
		root.setExpanded(false);
		for (TreeNode n : root.getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).collapseAll();
			}
			n.setExpanded(false);
		}
	}

	public void selectAll() {
		root.setSelected(true);
		for (TreeNode n : root.getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).selectAll();
			}
			n.setSelected(true);
		}
	}

	public void unselectAll() {
		root.setSelected(false);
		for (TreeNode n : root.getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).unselectAll();
			}
			n.setSelected(false);
		}
	}
	
	/** Sum cumulative columns in rows */
	public void setCumulative(String summableCols) {
		Set<String> sumColumns = new HashSet<String>(Arrays.asList(summableCols.split(",")));
		for (TreeNode n : root.getChildren()) {
			if (n instanceof BreakNode) {
				((BreakNode) n).sum(sumColumns);
			}
			n.setSelected(false);
		}
	}
}
