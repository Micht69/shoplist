package fr.logica.jsf.renderer;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.api.UITree;
import org.primefaces.component.column.Column;
import org.primefaces.component.treetable.TreeTable;
import org.primefaces.model.TreeNode;
import org.primefaces.renderkit.RendererUtils;

public class TreeTableRenderer extends org.primefaces.component.treetable.TreeTableRenderer {

	@Override
	protected void encodeNode(FacesContext context, TreeTable tt, TreeNode treeNode, String rowKey, String parentRowKey) throws IOException {
		if ("true".equals(tt.getAttributes().get("vanilla"))) {
			super.encodeNode(context, tt, treeNode, rowKey, parentRowKey);
		} else {
			if (rowKey != null) {
				ResponseWriter writer = context.getResponseWriter();
				tt.setRowKey(rowKey);
				String icon = treeNode.isExpanded() ? TreeTable.COLLAPSE_ICON : TreeTable.EXPAND_ICON;
				int depth = rowKey.split(UITree.SEPARATOR).length - 1;
				String selectionMode = tt.getSelectionMode();
				boolean selectionEnabled = selectionMode != null;
				boolean selectable = treeNode.isSelectable() && selectionEnabled;
				boolean checkboxSelection = selectionEnabled && selectionMode.equals("checkbox");

				if (checkboxSelection && treeNode.getParent().isSelected()) {
					treeNode.setSelected(true);
				}

				boolean selected = treeNode.isSelected();

				String rowStyleClass = selected ? TreeTable.SELECTED_ROW_CLASS : TreeTable.ROW_CLASS;
				rowStyleClass = selectable ? rowStyleClass + " " + TreeTable.SELECTABLE_NODE_CLASS : rowStyleClass;
				rowStyleClass = rowStyleClass + " " + treeNode.getType();

				String userRowStyleClass = tt.getRowStyleClass();
				if (userRowStyleClass != null) {
					rowStyleClass = rowStyleClass + " " + userRowStyleClass;
				}

				if (selected) {
					tt.getSelectedRowKeys().add(rowKey);
				}

				writer.startElement("tr", null);
				writer.writeAttribute("id", tt.getClientId(context) + "_node_" + rowKey, null);
				writer.writeAttribute("class", rowStyleClass, null);
				writer.writeAttribute("role", "row", null);
				writer.writeAttribute("aria-expanded", String.valueOf(treeNode.isExpanded()), null);
				writer.writeAttribute("data-rk", rowKey, null);

				if (selectionEnabled) {
					writer.writeAttribute("aria-selected", String.valueOf(selected), null);
				}

				if (parentRowKey != null) {
					writer.writeAttribute("data-prk", parentRowKey, null);
				}

				for (int i = 0; i < tt.getChildren().size(); i++) {
					UIComponent kid = (UIComponent) tt.getChildren().get(i);

					if (kid instanceof Column && kid.isRendered()) {
						Column column = (Column) kid;
						String columnStyleClass = column.getStyleClass();
						String columnStyle = column.getStyle();

						writer.startElement("td", null);
						if (treeNode.getChildCount() != 0) {
							writer.writeAttribute("colspan", tt.getColumnsCount(), null);
							if (columnStyleClass == null) {
								columnStyleClass = "treetable-break-line-col";
							} else {
								columnStyleClass += " treetable-break-line-col";
							}
						}
						writer.writeAttribute("role", "gridcell", null);
						if (columnStyle != null)
							writer.writeAttribute("style", columnStyle, null);
						if (columnStyleClass != null)
							writer.writeAttribute("class", columnStyleClass, null);

						if (i == 0) {
							for (int j = 0; j < depth; j++) {
								writer.startElement("span", null);
								writer.writeAttribute("class", TreeTable.INDENT_CLASS, null);
								writer.endElement("span");
							}

							writer.startElement("span", null);
							writer.writeAttribute("class", icon, null);
							if (treeNode.getChildCount() == 0) {
								writer.writeAttribute("style", "visibility:hidden", null);
							}
							writer.endElement("span");

							if (selectable && checkboxSelection) {
								RendererUtils.encodeCheckbox(context, selected);
							}
						}

						column.encodeAll(context);

						writer.endElement("td");
						if (treeNode.getChildCount() != 0) {
							// When we build a breaking column, we display only one cell with colspan.
							break;
						}
					}
				}

				writer.endElement("tr");
			}

			// render child nodes if node is expanded or node itself is the root
			if (treeNode.isExpanded() || treeNode.getParent() == null) {
				int childIndex = 0;
				for (Iterator<TreeNode> iterator = treeNode.getChildren().iterator(); iterator.hasNext();) {
					String childRowKey = rowKey == null ? String.valueOf(childIndex) : rowKey + UITree.SEPARATOR + childIndex;

					encodeNode(context, tt, iterator.next(), childRowKey, rowKey);

					childIndex++;
				}
			}
		}
	}
}
