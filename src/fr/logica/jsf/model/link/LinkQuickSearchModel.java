package fr.logica.jsf.model.link;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.logica.business.Entity;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.BusinessController;
import fr.logica.jsf.components.autocomplete.AutocompleteSuggestion;
import fr.logica.jsf.controller.ViewController;
import fr.logica.jsf.webflow.View;

public class LinkQuickSearchModel extends LinkModel implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = -1435642317731148186L;

	private String encodedValue;

	public LinkQuickSearchModel(ViewController viewCtrl, Map<String, String> store, Entity entity, String entityName, String linkName,
			String filterName) {
		super(viewCtrl, store, entity, entityName, linkName, filterName);
	}

	public List<AutocompleteSuggestion> quickSearch(String criteria) {
		RequestContext context = null;
		try {
			context = new RequestContext(viewCtrl.getSessionCtrl().getContext());
			Map<String, String> values = new BusinessController()
					.getLinkQuickSearchData(sourceEntity, entityName, linkName, filterName, criteria, context);
			List<AutocompleteSuggestion> result = new ArrayList<AutocompleteSuggestion>();

			for (Map.Entry<String, String> e : values.entrySet()) {
				result.add(new AutocompleteSuggestion(e.getValue(), e.getKey()));
			}
			return result;
		} finally {
			if (context != null) {
				// Close request context potential database connection
				context.close();
			}
		}
	}

	@Override
	public void storeViewData(Map<String, String> store) {
		if (encodedValue != null) {
			store.put(ENCODED_VALUE, encodedValue);
		}
	}

	@Override
	public void validateView(View currentView) {
		if (encodedValue != null) {
			Key foreignKey = new Key(entityName, encodedValue);
			sourceEntity.setForeignKey(linkName, foreignKey);
		}
	}

	public String getEncodedValue() {
		return encodedValue;
	}

	public void setEncodedValue(String encodedValue) {
		this.encodedValue = encodedValue;
	}

	public void setDescription(String autocompleteField) {
		// Do nothing, we don't care about it.
	}
}
