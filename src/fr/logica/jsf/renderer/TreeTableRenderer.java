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

/**
 * Tree Table Renderer.
 */
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

				if (treeNode.getChildCount() != 0) {
					// add a line with break label, the "normal" line will contain potential sums
					addBreakCaptionLine(context, tt, treeNode, rowKey, parentRowKey, writer);
				}

				writer.startElement("tr", null);
				if (treeNode.getChildCount() == 0) {
					Boolean readOnly = (Boolean) tt.getAttributes().get("readOnly");
					Object onRowClick = tt.getAttributes().get("onRowClick");
					if (onRowClick != null && (null == readOnly || !readOnly)) {
						writer.writeAttribute("onclick", onRowClick, null);
					}
				}
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
							if (columnStyleClass == null) {
								columnStyleClass = "treetable-break-line-col";
							} else {
								columnStyleClass += " treetable-break-line-col";
							}
							if (i < tt.getChildren().size() - 1) {
								if (columnStyle == null) {
									columnStyle = "";
								}
							}
						} else if (i == 0) {
							if (columnStyleClass == null) {
								columnStyleClass = "first-gridcell";
							} else {
								columnStyleClass += " first-gridcell";
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

						if (treeNode.getChildCount() == 0 || i > 0) {
							// we do not encode break label of first column here
							column.encodeAll(context);
						}

						writer.endElement("td");
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

	private void addBreakCaptionLine(FacesContext context, TreeTable tt, TreeNode treeNode, String rowKey, String parentRowKey,
			ResponseWriter writer) throws IOException {
		String rowStyleClass = TreeTable.ROW_CLASS + " " + treeNode.getType();

		String userRowStyleClass = tt.getRowStyleClass();
		if (userRowStyleClass != null) {
			rowStyleClass = rowStyleClass + " " + userRowStyleClass;
		}
		rowStyleClass += " treetable-break-line-header";

		writer.startElement("tr", null);
		writer.writeAttribute("class", rowStyleClass, null);
		writer.writeAttribute("role", "row", null);
		writer.writeAttribute("aria-expanded", String.valueOf(treeNode.isExpanded()), null);
		writer.writeAttribute("data-rk", rowKey + "-header", null);

		if (parentRowKey != null) {
			writer.writeAttribute("data-prk", parentRowKey, null);
		}

		writer.startElement("td", null);
		writer.writeAttribute("colspan", tt.getColumnsCount(), null);
		Column column = (Column) tt.getChildren().get(0);
		String columnStyleClass = column.getStyleClass();
		String columnStyle = column.getStyle();
		if (columnStyleClass == null) {
			columnStyleClass = "";
		}
		columnStyleClass += " treetable-break-line-col";
		if (columnStyle == null) {
			columnStyle = "";
		}
		if (columnStyle != null)
			writer.writeAttribute("style", columnStyle, null);

		if (columnStyleClass != null)
			writer.writeAttribute("class", columnStyleClass, null);

		writer.writeAttribute("role", "gridcell", null);
		column.encodeAll(context);
		writer.endElement("td");
		writer.endElement("tr");
	}

}
