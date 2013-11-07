package fr.logica.db;

import java.util.Set;

public abstract class AbstractEntityQuery {

	public abstract DbQuery getQuery(String queryName);

	public abstract Set<String> getQueryNames();
}
