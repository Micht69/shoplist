package fr.logica.business.data;

import java.io.Serializable;

/**
 * Column properties.
 */
public class ColumnData implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -6488987032635586234L;

	/**
	 * Column title.
	 */
	private String title;

	/**
	 * Indicates whether this column is visible.
	 */
	private boolean visible;

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
