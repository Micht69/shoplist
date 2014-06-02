package fr.logica.jsf.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.model.backref.BackRefInnerModel;
import fr.logica.jsf.model.backref.BackRefListCategoryModel;
import fr.logica.jsf.model.backref.BackRefListModel;
import fr.logica.jsf.model.backref.BackRefListTabeditModel;
import fr.logica.jsf.model.backref.BackRefModel;
import fr.logica.jsf.model.backref.BackRefScheduleModel;
import fr.logica.jsf.model.link.LinkComboModel;
import fr.logica.jsf.model.link.LinkInnerModel;
import fr.logica.jsf.model.link.LinkModel;
import fr.logica.jsf.model.link.LinkMultiComboModel;
import fr.logica.jsf.model.link.LinkQuickSearchModel;
import fr.logica.jsf.model.list.ListCategoryModel;
import fr.logica.jsf.model.list.ListModel;
import fr.logica.jsf.model.list.TabeditModel;
import fr.logica.jsf.webflow.View;

public abstract class DataModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -6865509601652494302L;

	private static final Logger LOGGER = Logger.getLogger(DataModel.class);

	protected ViewController viewCtrl;

	protected boolean readonly;
	protected boolean isProtected;

	public DataModel(ViewController viewCtrl) {
		this.viewCtrl = viewCtrl;
	}

	/**
	 * Returns a list of selected elements primary keys
	 * 
	 * @return Currently selected elements. Implementations must return an empty list if no element is selected. Return value must not be null.
	 */
	public List<Key> getSelected() {
		return null;
	}

	/**
	 * Loads model data from business controller. This method may be called for model reloading via ajax requests.
	 * 
	 * @param context
	 *            A request context to call business
	 */
	public void loadData(RequestContext context) {

	}

	public void reload() {
		RequestContext context = null;
		try {
			context = new RequestContext(viewCtrl.getSessionCtrl().getContext());
			loadData(context);
		} finally {
			if (context != null) {
				// Close request context potential database connection
				context.close();
			}
		}
		viewCtrl.displayMessages(context);
	}

	/**
	 * Current view will be validated server side, model may update the view before validation.
	 * 
	 * @param currentView
	 *            Current view.
	 */
	public void validateView(View currentView) {
		// Default data model does nothing on validation.
	}

	/**
	 * Allows model to store information, data, metadata or anything else inside the view so this can be retrieved when the user displays this
	 * again. For instance, javascript filter in lists.
	 * 
	 * @param store
	 *            A map that will be stored in the view and passed as parameter to the datamodel on next construction
	 */
	public void storeViewData(Map<String, String> store) {
		// Default data model does not store anything
	}

	public void downloadFile(File f) {
		OutputStream out = null;
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

			response.setContentLength((int) f.length());
			String fileName = f.getName();
			response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			response.addHeader("Cache-Control", "public");

			FileInputStream stream = new FileInputStream(f);
			out = response.getOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = stream.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			stream.close();
			out.flush();
			out.close();
			fc.responseComplete();
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}

		}
	}

	public enum UiTemplate {
		/** Image link */
		VAR_IMAGE_LINK,
		/** File upload */
		VAR_FILE_UPLOAD,
		/** CLOB */
		VAR_CLOB,
		/** Classic list template */
		LIST,
		/** Editable list */
		LIST_TAB_EDIT,
		/** Categorized list */
		LIST_CATEGORY,
		/** Link simple reference */
		LINK,
		/** Link simple reference and quick search */
		LINK_QUICK_SEARCH,
		/** Link in a combobox */
		LINK_COMBO,
		/** Link in a multi-combobox */
		LINK_MULTI_COMBO,
		/** Link in an inner template */
		LINK_INNER,
		/** Backref simple reference */
		BACK_REF,
		/** Backref list */
		BACK_REF_LIST,
		/** Backref list with input */
		BACK_REF_LIST_TAB_EDIT,
		/** Backref list displayed as a schedule */
		BACK_REF_LIST_SCHEDULE,
		/** Backref list with categories */
		BACK_REF_LIST_CATEGORY,
		/** Backref in an inner template */
		BACK_REF_INNER,
		/** Group */
		GROUP,
		/** Tabs */
		GROUP_TABS,
		/** Wizard */
		GROUP_WIZARD,
		/** Custom template */
		CUSTOM,
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	/**
	 * Builds a DataModel for UI
	 * 
	 * @param clazz
	 *            Custom DataModel class to use. Null for default templates.
	 * @param ui
	 *            UI template to display
	 * @param entityName
	 *            Main entity of the template
	 * @param linkName
	 *            LinkName between template and its container template, if any
	 * @param queryName
	 *            QueryName used by template, if any
	 * @param filterName
	 *            Filter Query used by template, if any
	 * @param e
	 *            Main Usually target entity for backref templates, and source entity for link templates.
	 * @param modelStore
	 *            Stored parameters of this templates. (Stateful components)
	 * @param viewController
	 *            Main view controller
	 * @return new instance of asked DataModel
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static DataModel build(Class<?> clazz, UiTemplate ui, String entityName, String linkName, String queryName,
			String filterName, Entity e, Map<String, String> modelStore, ViewController viewController) throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (ui == UiTemplate.LIST) {
			return new ListModel(viewController, modelStore, e, entityName, queryName);
		} else if (ui == UiTemplate.BACK_REF_LIST) {
			return new BackRefListModel(viewController, modelStore, e, entityName, queryName, linkName, filterName);
		} else if (ui == UiTemplate.BACK_REF_LIST_SCHEDULE) {
			return new BackRefScheduleModel(viewController, modelStore, e, entityName, queryName, linkName);
		} else if (ui == UiTemplate.LINK) {
			return new LinkModel(viewController, modelStore, e, entityName, linkName, filterName);
		} else if (ui == UiTemplate.BACK_REF) {
			return new BackRefModel(viewController, modelStore, e, entityName, linkName, filterName);
		} else if (ui == UiTemplate.LINK_QUICK_SEARCH) {
			return new LinkQuickSearchModel(viewController, modelStore, e, entityName, linkName, filterName);
		} else if (ui == UiTemplate.LINK_COMBO) {
			return new LinkComboModel(viewController, modelStore, e, entityName, linkName, filterName);
		} else if (ui == UiTemplate.LINK_MULTI_COMBO) {
			return new LinkMultiComboModel(viewController, modelStore, e, entityName, linkName);
		} else if (ui == UiTemplate.LINK_INNER) {
			return new LinkInnerModel(viewController, modelStore, e, entityName, linkName);
		} else if (ui == UiTemplate.BACK_REF_INNER) {
			return new BackRefInnerModel(viewController, modelStore, e, entityName, linkName);
		} else if (ui == UiTemplate.LIST_TAB_EDIT) {
			return new TabeditModel(viewController, modelStore, e, entityName, queryName, linkName, filterName);
		} else if (ui == UiTemplate.LIST_CATEGORY) {
			return new ListCategoryModel(viewController, modelStore, e, entityName, queryName);
		} else if (ui == UiTemplate.BACK_REF_LIST_TAB_EDIT) {
			return new BackRefListTabeditModel(viewController, modelStore, e, entityName, queryName, linkName,
					filterName);
		} else if (ui == UiTemplate.BACK_REF_LIST_CATEGORY) {
			return new BackRefListCategoryModel(viewController, modelStore, e, entityName, queryName, linkName,
					filterName);
		} else if (ui == UiTemplate.CUSTOM && clazz != null) {
			// Refactor this at the same time we refactor this whole method
			return (DataModel) clazz.getConstructors()[0].newInstance(viewController, modelStore, e, entityName, queryName, linkName,
					filterName);
		}
		LOGGER.error("DataModel " + (clazz != null ? clazz.getSimpleName() : ui) + " not supported. ");
		return null;
	}
}
