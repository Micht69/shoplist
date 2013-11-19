/**
 * 
 */
package fr.logica.db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.logica.application.AbstractApplicationLogic;
import fr.logica.application.ApplicationUtils;
import fr.logica.business.Constants;
import fr.logica.business.Entity;
import fr.logica.business.EntityField;
import fr.logica.business.EntityField.Memory;
import fr.logica.business.EntityModel;
import fr.logica.business.Key;
import fr.logica.business.KeyModel;
import fr.logica.business.LinkModel;
import fr.logica.business.TechnicalException;
import fr.logica.db.DbConnection;
import fr.logica.db.DbConnection.Type;
import fr.logica.db.DbQuery.Var;
import fr.logica.reflect.DomainUtils;

/**
 * @author logica
 */
public class DbQuery implements Cloneable {

	/** Join type. */
	public enum Join {
		STRICT, LOOSE, NONE;
	}

	/** Operators. */
	public enum SqlOp {
		OP_EQUAL("="), OP_N_EQUAL("<>"), OP_GREATER(">"), OP_LESS("<"), OP_GREATER_OR_EQUAL(">="), OP_LESS_OR_EQUAL("<="), OP_IN("IN"), OP_N_IN(
				"NOT IN"), OP_LIKE("LIKE"), OP_N_LIKE("NOT LIKE"), OP_ISNULL("IS NULL"), OP_N_ISNULL("IS NOT NULL"), OP_BETWEEN("BETWEEN");

		public String val;

		private SqlOp(String value) {
			val = value;
		}
	}

	/** Variable visibility. */
	public enum Visibility {
		VISIBLE, INVISIBLE, PROTECTED;
	}

	/**
	 * Représente une "variable" de la requête SQL. Une variable possède un nom (le même que le nom du champ sur l'entité), un nom affichable, un
	 * type. Une variable référence une table particulière dans laquelle on va la récupérer.
	 */
	public class Var {
		public String name = null;
		public String extern = null;
		public String tableId = null;
		public EntityField model = null;
		public Visibility visibility = Visibility.VISIBLE;
		public String expr = null;
		public boolean isGrouping = false;

		public Var(String pName, String pExternName, String pTableId, EntityField pField) {
			name = pName;
			extern = pExternName;
			if (extern == null) {
				extern = pField.getSqlName();
			}
			tableId = pTableId;
			model = pField;
			expr = pField.getSqlExpr();
		}

		public String getColumnAlias() {
			return tableId + "_" + name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Var other = (Var) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (tableId == null) {
				if (other.tableId != null)
					return false;
			} else if (!tableId.equals(other.tableId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return tableId + '.' + name;
		}
	}

	public static final String ASC = "ASC";

	public static final String DESC = "DESC";

	/** Type SQL devant recevoir un UPPER */
	private static final List<String> STRING_SQL_TYPES = new ArrayList<String>();
	static {
		STRING_SQL_TYPES.add("VARCHAR2");
		STRING_SQL_TYPES.add("VARCHAR");
		STRING_SQL_TYPES.add("CHAR");
	}
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(DbQuery.class);

	private boolean caseInsensitiveSearch = true;
	private boolean distinct = false;
	private boolean firstAddColumn = true;
	private boolean forUpdate = false;
	private String name;
	private String whereClause = "";
	private String whereJoin = "";
	private String groupByClause = "";
	private List<Table> tables = new ArrayList<DbQuery.Table>();
	private List<Var> inVars = new ArrayList<DbQuery.Var>();
	private List<Var> outVars = new ArrayList<DbQuery.Var>();
	private List<Const> outConsts = new ArrayList<DbQuery.Const>();
	private List<SortVar> sortVars = new ArrayList<DbQuery.SortVar>();
	private List<Object> bindValues = new ArrayList<Object>();
	private int maxRownum = -1;
	private int minRownum = 0;
	private boolean count = false;

	private Map<String, Integer> indexes = new HashMap<String, Integer>();

	/**
	 * Returns the index of the alias
	 * 
	 * @param name
	 *            The alias from we want the index
	 * @return The index of alias name in the query. This should be used to get data from result set instead of accessing RS via names.
	 */
	public int getIndex(String name) {
		if (indexes.get(name) != null) {
			return indexes.get(name).intValue();
		}
		StringBuilder aliasList = new StringBuilder();
		for (String aliasName : indexes.keySet()) {
			aliasList.append(aliasName);
			aliasList.append(", ");
		}
		throw new TechnicalException("Index doesn't exist in query. Existing alias are : " + aliasList);
	}

	/**
	 * Gets the column alias created by the query to identify in a unique way the selected value. This alias can be passed to the getIndex()
	 * method in order to get the resultSet index.
	 * 
	 * @param entityName
	 *            Entity (=table) name
	 * @param name
	 *            Variable (=column) name
	 * @return the alias used by the query to identify the column selected. The alias can be hashed if it's longer than 30 characters.
	 */
	public String getColumnAlias(String entityName, String name) {
		if (name == null) {
			throw new TechnicalException("Var name is null, cannot find matching alias.");
		}

		for (Var v : outVars) {
			if (entityName == null || entityName.equals(getEntity(v.tableId))) {
				if (name.equals(v.name)) {
					return aliash(v);
				}
			}
		}
		return null;
	}

	public class Const {
		String name;
		String value;
	}

	private class SortVar {
		Var inVar = null;
		String direction = null;
		boolean categorize = false;

		protected SortVar(Var v, String d, boolean c) {
			inVar = v;
			direction = d;
			categorize = c;
		}

		@Override
		public String toString() {
			return "SortVar [inVar=" + inVar + ", direction=" + direction + ", " + Boolean.toString(categorize) + "]";
		}
	}

	private class Table {
		Entity entity = null;
		String alias = null;
		String extern = null;
		String join = "";
		String joinCond = "";
		String joinCondNull = "";
		boolean isOuterJoined = false;

		protected Table(Entity classe, String as) {
			entity = classe;
			alias = as;
			extern = classe.getModel().$_getDbName();
		}

		@Override
		public String toString() {
			return "Table [entity=" + entity + ", alias=" + alias + "]";
		}
	}

	public enum LogicOperator {
		OR("OR"), AND("AND"), EQUALS("="), NEQUALS("<>"), SUP(">"), SUPEQUALS(">="), INF("<"), INFEQUALS("<=");
		private String op;

		LogicOperator(String op) {
			this.setOp(op);
		}

		public void setOp(String op) {
			this.op = op;
		}

		public String getOp() {
			return op;
		}
	};

	public enum ValuableOperator {
		ISNULL("IS NULL"), ISNOTNULL("IS NOT NULL");

		private String op;

		ValuableOperator(String op) {
			this.setOp(op);
		}

		public void setOp(String op) {
			this.op = op;
		}

		public String getOp() {
			return op;
		}
	}

	public void setDistinct(boolean d) {
		distinct = d;
	}

	public String getOrderByClause() {
		if (sortVars.isEmpty()) {
			return null;
		}
		StringBuilder order = new StringBuilder();
		for (SortVar sVar : sortVars) {
			if (!"".equals(order.toString())) {
				order.append(", ");
			}
			if ("".equals(sVar.inVar.tableId)) {
				order.append(sVar.inVar.model.getSqlName()).append(" ").append(sVar.direction);
			} else {
				order.append(aliash(sVar.inVar)).append(" ").append(sVar.direction);
			}
		}
		return order.toString();
	}

	public static int IDENTIFIER_MAX_LEN = 30;

	public String aliash(Var v) {
		return aliash(v.tableId + "_" + v.model.getSqlName());
	}

	/**
	 * Please, notice my incredible pun on the method name. If name > 30 characters, we'll create an hashed unique alias out of it.
	 * 
	 * @param name
	 *            The maybe too long name.
	 * @return A name that makes less than 30 characters. 30 is the limit because it's the smallest maximum of all known databases.
	 */
	public String aliash(String name) {
		if (name == null || name.length() <= IDENTIFIER_MAX_LEN) {
			return name;
		}
		String hashName = String.valueOf(name.hashCode());
		if (hashName.startsWith("-")) {
			hashName = hashName.substring(1);
		}

		if (hashName.length() >= IDENTIFIER_MAX_LEN) {
			return "A" + hashName.substring(hashName.length() - IDENTIFIER_MAX_LEN + 1);
		} else {
			return name.substring(0, IDENTIFIER_MAX_LEN - hashName.length() - 1) + hashName;
		}
	}

	/**
	 * Renvoie la requête SQL
	 */
	public String toSelect() {
		indexes.clear();
		int index = 1;
		String query = "SELECT ";
		if (count) {
			query = query.concat("COUNT(1) FROM (SELECT ");
		} else if (maxRownum > 0) {
			if (DbConnection.dbType == Type.ORACLE) {
				query = query.concat("* FROM (SELECT ");
			}
		}

		if (distinct) {
			query = query.concat("DISTINCT ");
		}

		// Si on doit sélectionner toutes les variables ou qu'il n'y a aucune
		// variable de sortie définie, on sélectionne * sur toutes les tables
		// (T1.*, T2.*, etc.)
		if (forUpdate || outVars.size() == 0) {
			for (int i = 0; i < tables.size(); i++) {
				Table table = tables.get(i);
				if (i > 0) {
					query = query.concat(", ");
				}
				// all variables
				query = query.concat(table.alias + ".*");
			}
		} else {
			// Sinon, on ne sélectionne que les variables définies dans
			// outVars.
			StringBuilder selectClause = new StringBuilder();
			for (int i = 0; i < outVars.size(); i++) {
				Var outVar = outVars.get(i);
				if (!outVar.model.isFromDatabase()) {
					// Not a SQL variable, skip it
					continue;
				}
				if (selectClause.length() > 0) {
					selectClause.append(", ");
				}

				if (null != outVar.expr) {
					selectClause.append(outVar.expr);
				} else {
					selectClause.append(outVar.tableId);
					selectClause.append(".");
					selectClause.append(outVar.model.getSqlName());
				}
				indexes.put(outVar.tableId + "_" + outVar.model.getSqlName(), Integer.valueOf(index++));
				selectClause.append(" as ");
				selectClause.append(aliash(outVar));
			}
			query = query.concat(selectClause.toString());
			for (int i = 0; i < outConsts.size(); i++) {
				Const c = outConsts.get(i);

				if (i > 0 || outVars.size() > 0) {
					query = query.concat(", ");
				}
				query = query.concat("'" + c.value + "' as " + c.name);
			}
		}

		// **** from
		query = query.concat(" FROM ");
		for (int i = 0; i < tables.size(); i++) {
			if (i > 0) {
				if (DbConnection.dbType == Type.ORACLE) {
					query = query.concat(",");
				}
			}
			Table table = tables.get(i);
			query = query.concat(" " + table.join + " ");
			query = query.concat(table.extern + " " + table.alias);
			if (table.joinCond.length() > 0) {
				query = query.concat(" ON " + table.joinCond);
			}
		}
		// **** where

		String s = "";
		if (whereJoin.length() > 0) {
			// add joining clauses
			if (s.length() > 0) {
				s = s.concat(" and ");
			}
			s = s.concat("(" + whereJoin + ")");
		}
		if (whereClause.length() > 0) {
			// add where clauses
			if (s.length() > 0) {
				s = s.concat(" and ");
			}
			s = s.concat("(" + whereClause + ")");
		}
		if (s.length() > 0) {
			query = query.concat(" where " + s);
		}

		// **** group by
		if (groupByClause.length() == 0) {
			addGroupByAll();
		}
		if (groupByClause.length() > 0) {
			query = query.concat(" group by " + groupByClause);
		}

		// **** order by
		if (sortVars.size() > 0) {
			query = query.concat(" ORDER BY " + getOrderByClause());
		}

		if (count) {
			query = query.concat(")");
			if (DbConnection.dbType != Type.ORACLE) {
				query = query.concat(" AS COUNT_SUBSELECT_ALIAS");
			}
		} else if (maxRownum > 0) {
			if (DbConnection.dbType == Type.ORACLE) {
				query = query.concat(") WHERE ROWNUM <= " + maxRownum);
			} else if (DbConnection.dbType == Type.PostgreSQL) {
				query = query.concat(" LIMIT " + maxRownum + " OFFSET " + minRownum);
			} else {
				query = query.concat(" LIMIT " + minRownum + ", " + maxRownum);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("SQL Query :\n" + query);
			String params = "";
			for (Object o : bindValues) {
				params += ("".equals(params) ? "" : ", ") + o;
			}
			LOGGER.debug("SQL Params : " + params);
		}

		return query;
	}

	/**
	 * Ajout d'une classe
	 * 
	 * @param entity
	 *            Classe à ajouter
	 * @param tableAlias
	 *            Alias de table <i>null</i>=Auto (Tnnn)
	 * @param linkModel
	 *            lien pour jointure
	 */
	private Table addOneClass(Entity entity, String tableAlias, boolean addOutVars) {
		// Tester l'alias de table (si fourni)
		String tAlias = "T" + (tables.size() + 1);
		if (tableAlias != null && tableAlias.length() > 0) {
			if (getTable(tableAlias) != null) {
				throw new TechnicalException("Alias " + tableAlias + " déjà défini.");
			} else {
				tAlias = tableAlias;
			}
		}

		// Ok, ajouter l'entité
		Table table = new Table(entity, tAlias);
		tables.add(table);

		// Ajout des variables de table
		EntityModel model = entity.getModel();
		Set<String> fields = model.getFields();
		for (String fieldName : fields) {
			EntityField field = model.getField(fieldName);

			if (!field.isFromDatabase()) {
				// Not a SQL variable, skip it
				continue;
			}
			if (addOutVars) {
				Var outVar = new Var(fieldName, null, table.alias, field);
				outVars.add(outVar);
			}

			Var inVar = new Var(fieldName, null, table.alias, field);
			inVars.add(inVar);
		}
		return table;
	}

	/**
	 * Ajouter une nouvelle classe class1Name d'alias class1Alias à la requête, liée avec le lien linkName à une classe d'alias class2Alias avec
	 * une jointure de type joinType.<br>
	 * équivalence 1.2: JOIN_TYPE_STRICT si la clé source du lien de jointure est obligatoire
	 * 
	 * @param e1Name
	 *            Nom de l'entité à ajouter
	 * @param e1Alias
	 *            Alias de l'entité à ajouter, <i>null</i>=Alias automatique
	 * @param linkName
	 *            Relation d'une classe existante vers l'entité rajoutée, <i>null</i>=Lien automatique
	 * @param e2Alias
	 *            Alias de la table liée, <i>null</i>=Alias automatique
	 * @param joinType
	 *            type de jointure, cf constantes JOIN_TYPE_xxx
	 * @param addOutVars
	 *            Add all entity variables to the select clause.
	 */
	public void addEntity(String e1Name, String e1Alias, String linkName, String e2Alias, Join joinType, boolean addOutVars) {
		// FIXME Add join clauses hability
		Entity e1 = DomainUtils.newDomain(e1Name);
		String keyName = e1.getModel().getKeyModel().getName();
		// première classe, ajouter directement
		if (tables.isEmpty()) {
			addOneClass(e1, e1Alias, addOutVars);
			return;
		}
		if (getTable(e1Name, e1Alias) != null && linkName == null) {
			// classe en double ? On ne l'ajoute pas.
			return;
		}
		// pas de jointure, ajout simple
		if (joinType == Join.NONE) {
			addOneClass(e1, e1Alias, addOutVars);
			return;
		}
		/*
		 * Ce n'est pas la première classe : essayer de trouver si sa clé primaire est referencée qu'une seule fois dans l'une des entités déjà
		 * définies OU C'est la première classe d'une SubQuery avec un lien
		 */
		LinkModel uniqueLink = null;
		Entity srcEntity = null;
		String srcId = null;
		for (int i = 0; i < tables.size(); i++) {
			Table table = tables.get(i);
			for (String lnkName : table.entity.getModel().getLinkNames()) {
				LinkModel link = table.entity.getModel().getLinkModel(lnkName);
				// check links to class
				if (link.getRefEntityName().equals(e1Name)) {
					if (linkName == null && keyName.equals(new KeyModel(link.getRefEntityName()).getName())) {
						if (uniqueLink != null) {
							LOGGER.warn("ToyDbQuery.addEntity: more than one link exists from query's classes. Class " + e1Name
									+ " added with link " + uniqueLink);
						} else {
							uniqueLink = link;
							srcEntity = table.entity;
							srcId = table.alias;
						}
					} else if (linkName != null && linkName.equals(lnkName) && (e2Alias == null || e2Alias.equals(table.alias))) {
						uniqueLink = link;
						srcEntity = table.entity;
						srcId = table.alias;
					}
				}
			}
			if (uniqueLink == null) {
				for (String lnkName : table.entity.getModel().getBackRefNames()) {
					LinkModel link = table.entity.getModel().getBackRefModel(lnkName);
					// check links from class
					if (link.getEntityName().equals(e1Name)) {
						if (linkName == null) {
							if (uniqueLink != null) {
								LOGGER.warn("ToyDbQuery.addEntity: more than one link exists to query's classes. Class " + e1Name
										+ " added with link " + uniqueLink);
							} else {
								uniqueLink = link;
								srcEntity = table.entity;
								srcId = table.alias;
							}
						} else if (linkName.equals(lnkName) && (e2Alias == null || e2Alias.equals(table.alias))) {
							uniqueLink = link;
							srcEntity = table.entity;
							srcId = table.alias;
						}
					}
				}
			}
		}

		if (uniqueLink == null) {
			if (linkName == null) {
				throw new TechnicalException("ToyDbQuery.addEntity: Failed to add class " + e1Name + ", no link found to/from query's classes.");
			} else if (e2Alias == null) {
				throw new TechnicalException("ToyDbQuery.addEntity: Failed to add class " + e1Name + ", link " + linkName
						+ " not found in query's classes.");
			} else {
				throw new TechnicalException("ToyDbQuery.addEntity: Failed to add class " + e1Name + ", link " + linkName + " to/from alias "
						+ e2Alias + " not found in query's classes" + " neither in the mother query.");
			}
		}
		// OK. Ajouter l'entité si la table existe deja, on rajoute que le lien
		Table table = getTable(e1Name, e1Alias);
		if (table == null) {
			table = addOneClass(e1, e1Alias, addOutVars);
		}

		// Ajouter les liens
		KeyModel srcKey = null;
		KeyModel dstKey = null;
		boolean bExtJoin = true;
		if (uniqueLink.getRefEntityName().equals(e1Name)) {
			srcKey = srcEntity.getModel().getForeignKeyModel(uniqueLink.getKeyName());
			dstKey = e1.getModel().getKeyModel();
		} else {
			srcKey = srcEntity.getModel().getKeyModel();
			dstKey = e1.getModel().getForeignKeyModel(uniqueLink.getKeyName());
		}
		bExtJoin = (joinType == Join.LOOSE);

		String dstJoin = "";

		if (DbConnection.dbType == Type.ORACLE) {
			if (bExtJoin) {
				dstJoin = "(+)";
			} else if (getPrivateTable(srcEntity) != null && getPrivateTable(srcEntity).isOuterJoined) {
				dstJoin = "(+)";
			}
		} else {
			if (bExtJoin) {
				table.join = "LEFT JOIN";
				table.isOuterJoined = true;
			} else {
				table.join = "JOIN";
			}
		}

		if (!dstJoin.equals("")) {
			table.isOuterJoined = true;
		}

		List<String> srcKeyList = srcKey.getFields();
		List<String> dstKeyList = dstKey.getFields();
		for (int i = 0; i < srcKeyList.size(); i++) {
			if (i > 0) {
				table.joinCond = table.joinCond.concat(" and ");
				table.joinCondNull = table.joinCondNull.concat(" and ");
			}

			String srcKeyExt = srcEntity.getModel().getField(srcKeyList.get(i)).getSqlName();
			String dstKeyExt = e1.getModel().getField(dstKeyList.get(i)).getSqlName();

			table.joinCond = table.joinCond.concat(srcId + "." + srcKeyExt + "=" + table.alias + "." + dstKeyExt + dstJoin);

			table.joinCondNull = table.joinCondNull.concat(table.alias + "." + dstKeyExt + " IS NULL");
		}

		// move join conditions to where clause
		if (DbConnection.dbType == Type.ORACLE) {
			if (whereJoin.length() > 0) {
				whereJoin = whereJoin.concat(" and ");
			}
			whereJoin = whereJoin.concat("(" + table.joinCond + ")");
			table.joinCond = "";
		}
	}

	/**
	 * retourne la table dont on connait l'alias utilisé dans la recherche d'une query mère
	 * 
	 * @param tableAlias
	 *            alias de la table
	 */
	private Table getTable(String tableAlias) {
		for (Table t : tables) {
			if (t.alias.equals(tableAlias)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Méthode getPrivateTable
	 */
	private Table getPrivateTable(Entity entity) {
		for (Table t : tables) {
			if (t.entity.equals(entity)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * retourne la table dont on connait le nom et l'alias
	 * 
	 * @param entityName
	 *            nom de l'entité
	 * @param tableAlias
	 *            alias de l'entité, null si on ne le connait pas.
	 */
	private Table getTable(String className, String tableAlias) {
		for (Table t : tables) {
			if (t.entity.$_getName().equals(className) && (tableAlias == null || tableAlias.equals(t.alias))) {
				return t;
			}
		}
		return null;
	}

	/**
	 * @param entityName
	 *            première classe de la requête
	 * @param tableAlias
	 *            alias facultatif utilisé pour cette entité
	 */
	public DbQuery(String entityName, String tableAlias) {
		addEntity(entityName, tableAlias, Join.STRICT);
	}

	/**
	 * @param entityName
	 *            première classe de la requête
	 */
	public DbQuery(String entityName) {
		addEntity(entityName, null, Join.STRICT);
	}

	/**
	 * Ajouter une nouvelle table à la requête
	 * 
	 * @see #addEntity(String, String, String, String, int)
	 * @param entityName
	 * @param entityAlias
	 * @param joinType
	 */
	public void addEntity(String entityName, String entityAlias, Join joinType) {
		addEntity(entityName, entityAlias, null, null, joinType);
	}

	/**
	 * Add a new entity to the query
	 * 
	 * @param e1Name
	 *            Name of the entity to add
	 * @param e1Alias
	 *            Alias of the new table
	 * @param linkName
	 *            Link to use for join with previous tables of th query
	 * @param e2Alias
	 *            Alias of the table to use for join
	 * @param joinType
	 *            Join type
	 */
	public void addEntity(String e1Name, String e1Alias, String linkName, String e2Alias, Join joinType) {
		addEntity(e1Name, e1Alias, linkName, e2Alias, joinType, true);
	}

	/**
	 * Ajout d'une condition égal à une clé Remarque: Les variables à valeurs nulles ne sont pas ajoutées
	 * 
	 * @param key
	 *            clé de comparaison
	 * @param tableAlias
	 *            alias
	 */

	public void addCondKey(Key key, String tableAlias) {
		if (key == null) {
			// La clé est nulle, on ajoute pas de condition.
			return;
		}
		for (String fieldName : key.getModel().getFields()) {
			if (key.getValue(fieldName) != null) {
				addCondEq(fieldName, tableAlias, key.getValue(fieldName));
			}
		}
	}

	/**
	 * Ajout d'une condition d'égalité
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param valeur
	 *            valeur de comparaison
	 */

	public void addCondEq(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_EQUAL, valeur, null);
	}

	/**
	 * Ajout d'une condition de non égalité. Gére le cas des non null si valeur vaut null.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param valeur
	 *            valeur de comparaison
	 */

	public void addCondNEq(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_N_EQUAL, valeur, null);
	}

	public void addCondGT(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_GREATER, valeur, null);
	}

	public void addCondGE(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_GREATER_OR_EQUAL, valeur, null);
	}

	public void addCondLT(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_LESS, valeur, null);
	}

	public void addCondLE(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_LESS_OR_EQUAL, valeur, null);
	}

	public void addCondLike(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_LIKE, valeur, null);
	}

	public void addCondNLike(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_N_LIKE, valeur, null);
	}

	public void addCondBetween(String colAlias, String tableAlias, Object valeur1, Object valeur2) {
		addCond(colAlias, tableAlias, SqlOp.OP_BETWEEN, valeur1, valeur2);
	}

	/**
	 * Ajout d'une condition 'inclus dans' liste de valeurs
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param objs
	 *            liste de valeurs
	 */

	public void addCondInList(String colAlias, String tableAlias, List<Object> objs) {
		addCondInList(colAlias, tableAlias, objs, false);
	}

	/**
	 * Ajout d'une condition 'inclus dans' liste de valeurs
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param objs
	 *            liste de valeurs
	 * @param bNot
	 *            true=négation (NOT IN)
	 */

	public void addCondInList(String colAlias, String tableAlias, List<Object> objs, boolean bNot) {
		if (objs == null || objs.size() == 0) {
			// Liste vide. On ajoute pas de condition.
			return;
		}

		String op = "IN";
		if (bNot) {
			op = "NOT IN";
		}

		Var inVar = getInVar(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("addCondIn: unknown column " + tableAlias + "." + colAlias);
		}

		if (whereClause.length() > 0 && !whereClause.trim().endsWith("(") && !whereClause.trim().endsWith("OR")) {
			whereClause = whereClause.concat(" and ");
		}

		// add subquery in where clause
		whereClause = whereClause.concat(inVar.tableId + "." + inVar.model.getSqlName() + " " + op + " (");

		String quote = "";
		if (STRING_SQL_TYPES.contains(inVar.model.getSqlType())) {
			quote = "'";
		}
		for (int i = 0; i < objs.size(); i++) {
			String varValue = String.valueOf(objs.get(i));
			whereClause = whereClause.concat(quote + varValue + quote);
			if (i < (objs.size() - 1)) {
				whereClause = whereClause.concat(",");
			}
		}
		whereClause = whereClause.concat(")");
	}

	public void addCondLikeConcat(List<String> colAliases, List<String> tableAliases, String value, boolean bNot) {
		String rel = "LIKE";
		if (bNot) {
			rel = "NOT LIKE";
		}

		ArrayList<Var> inVars = new ArrayList<Var>();
		for (int i = 0; i < colAliases.size(); i++) {
			String cAlias = colAliases.get(i);
			String tAlias = tableAliases.get(i);
			Var inVar = getInVar(cAlias, tAlias);
			if (inVar == null) {
				throw new TechnicalException("ToyDbQuery.addCond: unknown column " + tAlias + "." + cAlias);
			}
			inVars.add(inVar);
		}
		if (whereClause.length() > 0 && !whereClause.trim().endsWith("(") && !whereClause.trim().endsWith("or")) {
			whereClause = whereClause.concat(" and ");
		}

		whereClause = whereClause.concat("UPPER(");
		int nbreConcat = 2 * inVars.size() - 2;
		for (int k = 0; k < inVars.size(); k++) {
			Var inVarTmp = inVars.get(k);
			if (k < inVars.size() - 1) {
				if (inVarTmp.expr == null) {
					// Sauf sur entier
					whereClause = whereClause.concat("CONCAT(" + inVarTmp.tableId + "." + inVarTmp.model.getSqlName() + ",");
				} else {
					whereClause = whereClause.concat("CONCAT(" + inVarTmp.expr + ",");
				}
				whereClause = whereClause.concat("CONCAT(' ',");
			} else {
				if (inVarTmp.expr == null) {
					whereClause = whereClause.concat("" + inVarTmp.tableId + "." + inVarTmp.model.getSqlName() + "");
				} else {
					whereClause = whereClause.concat("" + inVarTmp.expr + "");
				}
			}
		}

		for (int p = 0; p < nbreConcat; p++) {
			whereClause = whereClause.concat(")");
		}
		whereClause = whereClause.concat(")");
		
		value = value.toUpperCase();

		if (!value.startsWith("%")) {
			value = "%" + value;
		}
		if (!value.endsWith("%")) {
			value = value.concat("%");
		}

		whereClause = whereClause.concat(" " + rel + " ?");
		EntityField varModel = new EntityField("TOYCONCATSEARCH", "VARCHAR2", 8000, 0, Memory.NO, false, false);
		Var outVar = new Var("TOYCONCATSEARCH", null, null, varModel);
		bindValues.add(parse(outVar, value));
	}

	/**
	 * Ajout d'une condition 'inclus dans' le résultat d'une autre query
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param subQuery
	 *            query retournant la liste de valeurs
	 * @param bNot
	 *            true=négation (NOT IN)
	 */

	public void addCondIn(String colAlias, String tableAlias, DbQuery subQuery, boolean bNot) {
		if (subQuery == null || "".equals(subQuery.toSelect())) {
			// Pas de subquery
			return;
		}

		String op = "IN";
		if (bNot) {
			op = "NOT IN";
		}

		Var inVar = getInVar(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("addCondIn: unknown column " + tableAlias + "." + colAlias);
		}

		if (whereClause.length() > 0 && !whereClause.trim().endsWith("(") && !whereClause.trim().endsWith("OR")) {
			whereClause = whereClause.concat(" and ");
		}

		// add subquery in where clause
		whereClause = whereClause.concat(inVar.tableId + "." + inVar.model.getSqlName() + " " + op + " (");
		whereClause = whereClause.concat(subQuery.toSelect());
		whereClause = whereClause.concat(")");
	}

	/**
	 * Ajout d'une condition 'inclus dans' le résultat d'une autre query
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param subQuery
	 *            query retournant la liste de valeurs
	 */

	public void addCondIn(String colAlias, String tableAlias, DbQuery subQuery) {
		addCondIn(colAlias, tableAlias, subQuery, false);
	}

	/**
	 * Ajout d'une condition
	 * 
	 * @param colAlias
	 *            - nom de variable
	 * @param tableAlias
	 *            - alias
	 * @param op
	 *            - opérateur
	 * @param valeur1
	 *            - valeur de comparaison
	 * @param valeur2
	 *            - valeur 2 de comparaison
	 */
	private void addCond(String colAlias, String tableAlias, SqlOp op, Object valeur1, Object valeur2) {
		// si la valeur est null et si on peut remplacer l'opérateur...
		if (valeur1 == null) {
			if (op.val.equals(SqlOp.OP_EQUAL.val) || op.val.equals(SqlOp.OP_LIKE.val)) {
				addCondIsNull(colAlias, tableAlias, false);
				return;
			}
			if (op.val.equals(SqlOp.OP_N_EQUAL.val) || op.val.equals(SqlOp.OP_N_LIKE.val)) {
				addCondIsNull(colAlias, tableAlias, true);
				return;
			}
		}

		Var inVar = getInVar(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("ToyDbQuery.addCond: unknown column " + tableAlias + "." + colAlias);
		}

		if (whereClause.length() > 0 && !whereClause.trim().endsWith("(") && !whereClause.trim().endsWith("OR")) {
			whereClause = whereClause.concat(" and ");
		}

		boolean noCase = caseInsensitiveSearch && "VARCHAR2".equals(inVar.model.getSqlType());
		if (noCase) {
			whereClause = whereClause.concat("UPPER");
		}

		whereClause = whereClause.concat("(" + inVar.tableId + "." + inVar.model.getSqlName() + ")");

		whereClause = whereClause.concat(" " + op.val + " ");
		if (noCase) {
			whereClause = whereClause.concat("UPPER");
		}

		String function = parseDefaultValue(inVar, valeur1);
		if (null != function) {
			whereClause = whereClause.concat(function);
		} else {
			whereClause = whereClause.concat("(?)");
			bindValues.add(parse(inVar, valeur1));
		}

		if (op.val.equals(SqlOp.OP_BETWEEN.val)) {
			function = parseDefaultValue(inVar, valeur2);
			if (null != function) {
				whereClause = whereClause.concat(" AND " + function);
			} else {
				whereClause = whereClause.concat(" AND (?)");
				bindValues.add(parse(inVar, valeur2));
			}
		}
	}

	/**
	 * Add a condition between two columns.
	 * 
	 * @param colAlias1
	 *            First column alias
	 * @param tableAlias1
	 *            First column table
	 * @param operator
	 *            Sql operator between two columns (equals, greater than, etc.)
	 * @param colAlias2
	 *            Second column alias
	 * @param tableAlias2
	 *            Second column table
	 */
	public void addCond(String colAlias1, String tableAlias1, SqlOp operator, String colAlias2, String tableAlias2) {
		Var inVar1 = getInVar(colAlias1, tableAlias1);
		if (inVar1 == null) {
			throw new TechnicalException("ToyDbQuery.addCond: unknown column " + tableAlias1 + "." + colAlias1);
		}
		Var inVar2 = getInVar(colAlias2, tableAlias2);
		if (inVar2 == null) {
			throw new TechnicalException("ToyDbQuery.addCond: unknown column " + tableAlias2 + "." + colAlias2);
		}

		if (whereClause.length() > 0 && !whereClause.trim().endsWith("(") && !whereClause.trim().endsWith("OR")) {
			whereClause = whereClause.concat(" and ");
		}

		boolean noCase = caseInsensitiveSearch && "VARCHAR2".equals(inVar1.model.getSqlType()) && "VARCHAR2".equals(inVar2.model.getSqlType());
		if (noCase) {
			whereClause = whereClause.concat("UPPER");
		}
		whereClause = whereClause.concat("(" + inVar1.tableId + "." + inVar1.model.getSqlName() + ")");
		whereClause = whereClause.concat(" " + operator.val + " ");
		if (noCase) {
			whereClause = whereClause.concat("UPPER");
		}
		whereClause = whereClause.concat("(" + inVar2.tableId + "." + inVar2.model.getSqlName() + ")");

	}

	private Object parse(Var var, Object value) {
		Object outValue = value;
		if (value != null && value instanceof String) {
			String sValue = (String) value;
			if (var.model.hasDefinedValues() && var.model.isCode(sValue)) {

				if ("BOOLEAN".equals(var.model.getSqlType())) {
					outValue = var.model.getBooleanDefValue(sValue);

				} else if ("INTEGER".equals(var.model.getSqlType())) {
					outValue = Integer.parseInt(var.model.getDefValue(sValue));

				} else {
					outValue = var.model.getDefValue(sValue);
				}

			} else if ("INTEGER".equals(var.model.getSqlType())) {
				try {
					outValue = Integer.parseInt(sValue);
				} catch (NumberFormatException ex) {
					throw new TechnicalException("La valeur \"" + sValue + "\" n'est pas correcte. ");
				}

			} else if ("TIME".equals(var.model.getSqlType())) {
				AbstractApplicationLogic appLogic = ApplicationUtils.getApplicationLogic();
				SimpleDateFormat defaultDateTimeFormatter = new SimpleDateFormat(appLogic.getTimeFormat());
				try {
					outValue = new Time(defaultDateTimeFormatter.parse(sValue).getTime());
				} catch (ParseException e) {
					throw new TechnicalException("La valeur \"" + sValue + "\" n'est pas correcte. ");
				}

			} else if ("TIMESTAMP".equals(var.model.getSqlType())) {
				AbstractApplicationLogic appLogic = ApplicationUtils.getApplicationLogic();
				SimpleDateFormat defaultDateTimeFormatter = new SimpleDateFormat(appLogic.getTimestampFormat());
				try {
					outValue = new Timestamp(defaultDateTimeFormatter.parse(sValue).getTime());
				} catch (ParseException e) {
					try {
						// try ISO format
						defaultDateTimeFormatter = new SimpleDateFormat(Constants.FORMAT_TIMESTAMP_ISO);
						outValue = new Timestamp(defaultDateTimeFormatter.parse(sValue).getTime());
					} catch (ParseException ee) {
						throw new TechnicalException("La valeur \"" + sValue + "\" n'est pas correcte. ");
					}
				}
			}
		}
		return outValue;
	}

	private String parseDefaultValue(Var var, Object value) {
		String outValue = null;

		if ("*TODAY".equals(value) || "*NOW".equals(value)) {

			if ("DATE".equals(var.model.getSqlType())) {

				if (DbConnection.dbType == Type.ORACLE) {
					outValue = "SYSDATE";
				} else if (DbConnection.dbType == Type.PostgreSQL) {
					outValue = "current_date";
				} else {
					outValue = "CURRENT_DATE";
				}

			} else if ("TIME".equals(var.model.getSqlType())) {

				if (DbConnection.dbType == Type.ORACLE) {
					outValue = "SYSDATE";
				} else if (DbConnection.dbType == Type.PostgreSQL) {
					outValue = "current_time";
				} else {
					outValue = "CURRENT_TIME";
				}

			} else if ("TIMESTAMP".equals(var.model.getSqlType())) {

				if (DbConnection.dbType == Type.PostgreSQL) {
					outValue = "clock_timestamp()";
				} else {
					outValue = "CURRENT_TIMESTAMP";
				}
			}
		}
		return outValue;
	}

	/**
	 * Ajout d'une condition IS NULL
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param notNull
	 *            vrai pour tester IS NOT NULL, faux pour tester IS NULL
	 */
	private void addCondIsNull(String colAlias, String tableAlias, boolean notNull) {
		String value = SqlOp.OP_ISNULL.val;
		if (notNull) {
			value = SqlOp.OP_N_ISNULL.val;
		}

		Var inVar = getInVar(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("addCond: unknown column " + tableAlias + "." + colAlias);
		}
		if (whereClause.length() > 0 && !whereClause.trim().endsWith("(") && !whereClause.trim().endsWith("OR")) {
			whereClause = whereClause.concat(" AND ");
		}

		whereClause = whereClause.concat(inVar.tableId + "." + inVar.model.getSqlName() + " " + value);
	}

	private void addSort(String colAlias, String tableAlias, String direction, boolean firstInPos, boolean categorize) {
		Var inVar = getInVar(colAlias, tableAlias);
		if (inVar != null) {
			SortVar sVar = new SortVar(inVar, direction, categorize);
			if (firstInPos) {
				sortVars.add(0, sVar);
			} else {
				sortVars.add(sVar);
			}
		}
	}

	public void addSortBy(String column) {
		addSort(column, null, ASC, false, false);
	}

	public void addSortBy(String column, String tableAlias) {
		addSort(column, tableAlias, ASC, false, false);
	}

	public void addSortByDesc(String column) {
		addSort(column, null, DESC, false, false);
	}

	public void addSortByDesc(String column, String tableAlias) {
		addSort(column, tableAlias, DESC, false, false);
	}

	public void addSortBy(String column, String tableAlias, String direction) {
		addSortBy(column, tableAlias, direction, false);
	}

	public void addCategorizedSortBy(String column, String tableAlias, String direction) {
		addSort(column, tableAlias, direction, false, true);
	}

	/**
	 * Sort the DbQuery with the given column
	 * 
	 * @param column
	 *            the order column
	 * @param tableAlias
	 *            the order table alias
	 * @param direction
	 *            the direction for sort
	 * @param firstInPos
	 *            true if the sort clause should be applyed before any existing sort clauses
	 */

	public void addSortBy(String column, String tableAlias, String direction, boolean firstInPos) {
		if (!(ASC.equals(direction) || DESC.equals(direction))) {
			direction = ASC;
		}
		addSort(column, tableAlias, direction, firstInPos, false);
	}

	/**
	 * check for variable and return Var instance if found
	 * 
	 * @param colAlias
	 *            - nom de la variable
	 * @param tableAlias
	 *            - alias de l'entité
	 * @param mdVar
	 *            - mdVar ou null
	 * @return Var instance if found
	 */
	private Var getInVar(String colAlias, String tableAlias) {
		for (Var inVar : inVars) {
			if ((colAlias.equals(inVar.name) || inVar.name.equals(DomainUtils.createDbName(colAlias)))
					&& (tableAlias == null || inVar.tableId.equals(tableAlias))) {
				return inVar;
			}
		}
		return null;
	}

	public List<Object> getBindValues() {
		return bindValues;
	}

	/**
	 * Ajouter une colonne
	 * 
	 * @param varName
	 *            variable
	 * @param tableAlias
	 *            alias
	 */
	public void addColumn(String varName, String tableAlias) {
		addColumn(varName, tableAlias, null, Visibility.VISIBLE);
	}

	/**
	 * Ajouter une colonne à valeur constante.
	 * 
	 * @param name
	 *            Le nom de la colonne
	 * @param value
	 *            La valeur de la colonne
	 */
	public void addColumnConst(String name, String value) {
		Const c = new Const();
		c.name = name;
		c.value = value;
		outConsts.add(c);
	}

	/**
	 * Ajouter une colonne
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param as
	 *            alias de sortie
	 */
	public void addColumn(String varName, String tableAlias, String as) {
		addColumn(varName, tableAlias, as, Visibility.VISIBLE);
	}

	/**
	 * Ajouter une colonne
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param v
	 *            Visibility
	 */
	public void addColumn(String varName, String tableAlias, Visibility v) {
		addColumn(varName, tableAlias, null, Visibility.VISIBLE);
	}

	/**
	 * Ajouter une colonne<br>
	 * FIXME select column: 'as name' not managed
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param as
	 *            alias de sortie
	 * @param v
	 *            Visibility
	 */
	public Var addColumn(String varName, String tableAlias, String as, Visibility v) {
		Var outVar = findOutVar(varName, tableAlias);
		if (outVar == null) {
			throw new TechnicalException("addColumn: variable " + varName + " not found.");
		}

		if (firstAddColumn) {
			removeAllColumns();
			firstAddColumn = false;
		}

		if (v != null && v != Visibility.VISIBLE) {
			outVar.visibility = v;
		}

		if (!outVars.contains(outVar)) {
			outVars.add(outVar);
		}
		return outVar;
	}

	/**
	 * Ajouter une colonne
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param as
	 *            alias de sortie
	 * @param v
	 *            Visibility
	 * @param expr
	 *            expression sql
	 */
	public Var addColumn(String varName, String tableAlias, String as, Visibility v, String expr) {
		Var outVar = addColumn(varName, tableAlias, as, v);

		if (expr != null && !expr.isEmpty()) {
			if (outVar.expr != null && outVar.expr.length() > 0) {
				outVar.expr = MessageFormat.format(expr, new Object[] { outVar.expr });
			} else {
				outVar.expr = MessageFormat.format(expr, new Object[] { outVar.tableId + "." + outVar.extern });
			}
		}
		return outVar;
	}

	/**
	 * Supprimer une colonne
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param as
	 *            alias de sortie
	 */
	public void removeColumn(String varName, String tableAlias, String as) {
		List<Var> newOutVars = new ArrayList<DbQuery.Var>();
		for (Var v : outVars) {
			if (!v.name.equals(varName) || !v.tableId.equals(tableAlias)) {
				newOutVars.add(v);
			}
		}
		outVars = newOutVars;
	}

	/**
	 * Enléve toutes les colonnes en sortie
	 */

	public void removeAllColumns() {
		outVars.clear();
	}

	/**
	 * Ajoute toutes colonnes d'une table
	 * 
	 * @param tableAlias
	 *            alias
	 */
	public void addAllColumns(String tableAlias) {
		Table t = getTable(tableAlias);
		// Ajout des variables de table
		Set<String> fields = t.entity.getModel().getFields();
		for (String fieldName : fields) {
			Var outVar = new Var(fieldName, null, tableAlias, t.entity.getModel().getField(fieldName));
			if (!outVars.contains(outVar)) {
				outVars.add(outVar);
			}

			Var inVar = new Var(fieldName, null, tableAlias, t.entity.getModel().getField(fieldName));
			if (!inVars.contains(inVar)) {
				inVars.add(inVar);
			}
		}
	}

	/**
	 * Enlève toutes les colonnes en sortie de la table
	 * 
	 * @param tableAlias
	 *            alias
	 */
	public void removeOutVars(String tableAlias) {
		for (int i = outVars.size() - 1; i >= 0; i--) {
			Var oV = outVars.get(i);
			if (oV.tableId.equals(tableAlias)) {
				outVars.remove(oV);
			}
		}
	}

	/**
	 * Retourner une variable en sortie
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 */
	private Var findOutVar(String varName, String tableAlias) {
		if (varName == null || varName.length() == 0) {
			throw new TechnicalException("findOutVar: invalid column");
		}

		Table table = null;
		// rechercher l'alias de table
		if (tableAlias != null && tableAlias.length() > 0) {
			table = getTable(tableAlias);
		}

		if (table == null) {
			return null;
		}

		if (table.entity.getModel().getField(varName) != null) {
			// find existing outvar
			Var o = new Var(varName, null, table.alias, table.entity.getModel().getField(varName));
			if (outVars.contains(o)) {
				return outVars.get(outVars.indexOf(o));
			} else if (table.entity.getModel().getField(varName) != null) {
				return o;
			}
		}
		return null;
	}

	/**
	 * @return entité de la première table de la query
	 */

	public Entity getMainEntity() {
		if (tables.isEmpty()) {
			return null;
		}
		return (tables.get(0)).entity;
	}

	/**
	 * @return alias de la première table de la query
	 */
	public String getMainEntityAlias() {
		if (tables.isEmpty()) {
			return null;
		}
		return (tables.get(0)).alias;
	}

	/**
	 * @param alias
	 *            alias de la table
	 * @return nom de l'entité de la table
	 */
	public String getEntity(String alias) {
		if (alias == null || tables.isEmpty()) {
			return null;
		}
		for (Table t : tables) {
			if (alias.equals(t.alias)) {
				return t.entity.$_getName();
			}
		}
		return null;
	}

	/**
	 * Récupère la liste des alias des tables
	 */
	public List<String> getAliases() {
		List<String> aliases = new ArrayList<String>();
		for (Table t : tables) {
			aliases.add(t.alias);
		}
		return aliases;
	}

	/**
	 * @return récupère l'alias d'une entité.
	 */
	public String getAlias(String entityName) {
		if (entityName == null || tables.isEmpty()) {
			return null;
		}
		for (Table t : tables) {
			if (entityName.equals(t.entity.$_getName())) {
				return t.alias;
			}
		}
		return null;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		DbQuery query = (DbQuery) super.clone();
		query.tables = new ArrayList<DbQuery.Table>();
		for (DbQuery.Table t : tables) {
			query.tables.add(t);
		}
		query.inVars = new ArrayList<DbQuery.Var>();
		for (DbQuery.Var v : inVars) {
			query.inVars.add(v);
		}
		query.outVars = new ArrayList<DbQuery.Var>();
		for (DbQuery.Var v : outVars) {
			query.outVars.add(v);
		}
		query.sortVars = new ArrayList<DbQuery.SortVar>();
		for (DbQuery.SortVar v : sortVars) {
			query.sortVars.add(v);
		}
		query.bindValues = new ArrayList<Object>();
		for (Object o : bindValues) {
			query.bindValues.add(o);
		}
		query.outConsts = new ArrayList<DbQuery.Const>();
		for (DbQuery.Const c : outConsts) {
			query.outConsts.add(c);
		}
		return query;
	}

	/*************************************************************************/

	public static String createPkWhereClause(KeyModel pkModel, Key key) {
		return createKeyWhereClause(pkModel, key, null);
	}

	public static String createKeyWhereClause(KeyModel pkModel, Key key, String tableAlias) {
		StringBuilder builder = new StringBuilder();
		Object result;
		boolean first = true;
		for (int i = 0; i < pkModel.getFields().size(); i++) {
			String fieldName = pkModel.getFields().get(i);
			result = key.getValue(key.getModel().getFields().get(i));

			// La première fois, on ne met pas de AND.
			if (first) {
				first = false;
			} else {
				builder.append(" AND ");
			}

			if (tableAlias != null) {
				builder.append(tableAlias + "." + DomainUtils.createDbName(fieldName));
			} else {
				builder.append(DomainUtils.createDbName(fieldName));
			}
			builder.append(" = ");

			if (result instanceof Integer || result instanceof BigDecimal || result instanceof BigInteger) {
				builder.append(result);
			} else if (result instanceof String) {
				String value = (String) result;
				builder.append("'" + value + "'");
			} else if (result instanceof Date) {
				Date value = (Date) result;
				AbstractApplicationLogic appLogic = ApplicationUtils.getApplicationLogic();
				SimpleDateFormat sdf = new SimpleDateFormat(appLogic.getDateFormat());
				builder.append("'" + sdf.format(value) + "'");
			} else {
				// result is null
				// FIXME should create IsNull condition
				builder.append("''");
			}
		}
		return builder.toString();
	}

	public static String createSelectByIdQuery(String entityName, KeyModel pkModel, Key key) {
		try {
			String dbName = DomainUtils.createDbName(entityName);
			StringBuffer buffer = new StringBuffer("select " + dbName + ".* from ");
			buffer.append(dbName);
			buffer.append(" where ");
			buffer.append(createPkWhereClause(pkModel, key));
			return buffer.toString();
		} catch (SecurityException e) {
			throw new RuntimeException("Impossible de réaliser une clause SELECT sur l'objet " + entityName, e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Impossible de réaliser une clause SELECT sur l'objet " + entityName, e);
		}

	}

	/**
	 * Création de la requête SQL de select à partir de l'identifiant de l'entité passée en paramètre
	 * 
	 * @param entity
	 * @return
	 */
	public static String createSelectByIdQuery(Entity entity) {
		return createSelectByIdQuery(entity.getClass().getSimpleName(), entity.getModel().getKeyModel(), entity.getPrimaryKey());
	}

	/**
	 * Génération de la requête permettant de récupérer le prochain ID d'une séquence
	 * 
	 * @param entity
	 * @return
	 */
	public static String getNextSequenceIdQuery(Entity entity) {
		String dbName = DomainUtils.createDbName(entity.getClass().getSimpleName());
		return "select " + Constants.EXTENSION_SEQUENCE + dbName + ".nextVal from dual";
	}

	/**
	 * Génération de la requête permettant de récupérer le prochain ID d'une séquence
	 * 
	 * @param entity
	 * @return
	 */
	public static String getNextSequenceIdQuery(String sequenceName) {
		return "select " + Constants.EXTENSION_SEQUENCE + sequenceName + ".nextVal from dual";
	}

	/**
	 * Returns a list a expected variables from this query. They are the variable models, not the values. They do not always match with variables
	 * in an entity.
	 */
	public List<DbQuery.Var> getOutVars() {
		return outVars;
	}

	/**
	 * Returns the expected variables from this query reduced to the one linked to the main entity.
	 */
	public List<DbQuery.Var> getMainEntityOutVars() {
		List<Var> results = new ArrayList<DbQuery.Var>();
		for (DbQuery.Var var : getOutVars()) {
			if (var.tableId.equals(getMainEntityAlias())) {
				results.add(var);
			}
		}

		return results;
	}

	public void setMaxRownum(int maxRownum) {
		this.maxRownum = maxRownum;
	}

	public int getMaxRownum() {
		return maxRownum;
	}

	/**
	 * Le nom de la requête. Attention, pour les requêtes non nommées, ce nom peut être null.
	 * 
	 * @return Le nom de la requête s'il est renseigné, null sinon.
	 */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCount() {
		return count;
	}

	public void setCount(boolean count) {
		this.count = count;
	}

	/**
	 * Vérifie si la requete est NON sensible à la casse.
	 * 
	 * @return the caseInsensitiveSearch
	 */

	public boolean isCaseInsensitiveSearch() {
		return caseInsensitiveSearch;
	}

	/**
	 * Détermine si la requete est NON sensible à la casse.<br>
	 * - true : <b>NON</b> sensible<br>
	 * - false : sensible.
	 * 
	 * @param caseInsensitiveSearch
	 *            the caseInsensitiveSearch to set
	 */

	public void setCaseInsensitiveSearch(boolean caseInsensitiveSearch) {
		this.caseInsensitiveSearch = caseInsensitiveSearch;
	}

	/**
	 * Ajout d'une fonction de regroupement (GROUP BY)
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 */

	public void addGroupBy(String colAlias, String tableAlias) {
		Var outVar = findOutVar(colAlias, tableAlias);
		if (outVar != null && !outVar.model.isTransient()) { // FIXME Replace !isTransient() by isFromDatabase() ?
			if (groupByClause.length() > 0) {
				groupByClause = groupByClause.concat(", ");
			}
			groupByClause = groupByClause.concat(outVar.tableId + "." + outVar.model.getSqlName());
		}
	}

	/**
	 * Used when building the final SQL statement.<br>
	 * If select clause has 'group by' functions like avg() then add standard columns as 'group by' clauses.
	 */
	private void addGroupByAll() {
		boolean hasGrouping = false;
		for (int i = 0; i < outVars.size() && !hasGrouping; i++) {
			if (outVars.get(i).isGrouping) {
				hasGrouping = true;
			}
		}
		if (hasGrouping) {
			for (int i = 0; i < outVars.size(); i++) {
				Var outVar = outVars.get(i);
				if (!outVar.isGrouping) {
					addGroupBy(outVar.name, outVar.tableId);
				}
			}
		}
	}

	/**
	 * Ajouter une expression regroupante
	 */
	private Var addGroupColumn(String colAlias, String tableAlias, String expr, String asName) {
		Var outVar = addColumn(colAlias, tableAlias, asName, Visibility.VISIBLE, expr);
		outVar.isGrouping = true;
		return outVar;
	}

	/**
	 * Ajouter une colonne utilisant la fonction AVG(colAlias)
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de classe (facultatif)
	 */
	public void addAvg(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "avg({0})", null);
	}

	/**
	 * Ajouter une colonne utilisant la fonction SUM(colAlias)
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de classe (facultatif)
	 */
	public void addSum(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "sum({0})", null);
	}

	/**
	 * Ajouter une colonne utilisant la fonction DISTINCT(colAlias)
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de classe (facultatif)
	 */
	public void addDistinct(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "distinct({0})", null);
	}

	/**
	 * Ajouter une fonction maximum
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 */
	public void addMax(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "max({0})", null);
	}

	/**
	 * Ajouter une fonction minimum
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 */
	public void addMin(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "min({0})", null);
	}

	/**
	 * Ajout d'une colonne de comptage
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de table, <code>null</code>=facultatif
	 * @param bDistinct
	 *            <code>true</code>=count(distinct COLONNE) <code>false</code>=count(COLONNE)
	 * @param asName
	 * <br>
	 */
	public void addCount(String colAlias, String tableAlias, String asName, boolean bDistinct) {
		Var outVar = null;
		if (bDistinct) {
			outVar = addGroupColumn(colAlias, tableAlias, "count(distinct {0})", asName);
		} else {
			outVar = addGroupColumn(colAlias, tableAlias, "count({0})", asName);
		}
		// override column type
		outVar.model = new EntityField(outVar.model.getSqlName(), "INTEGER", 10, 0, Memory.NO, true, false);
	}

	/**
	 * Ajout d'une expression decode : decode (<b>tableAlias.colAlias</b>, <b>args[0]</b> {args[1], args[2], ...}) as <b>asName</b>
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de table, <code>null</code>=facultatif
	 * @param asName
	 *            alias de colonne
	 * @param args
	 *            Valeurs de decode
	 */
	public void addDecode(String colAlias, String tableAlias, String asName, Object[] args) {
		if (args == null || args.length == 0) {
			throw new TechnicalException("addDecode() : missing decode values");
		}
		Var outVar = findOutVar(colAlias, tableAlias);
		boolean isValue = true;
		StringBuffer expr = new StringBuffer("decode({0}");
		for (int i = 0; i < args.length; i++) {
			if (isValue) {
				String function = parseDefaultValue(outVar, args[i]);
				if (null != function) {
					expr.append(", " + function);
				} else {
					expr.append(", ?");
					bindValues.add(parse(outVar, args[i]));
				}
			} else {
				expr.append(", " + args[i].toString());
			}
			isValue = !isValue;
		}
		expr.append(")");
		addColumn(colAlias, tableAlias, asName, Visibility.VISIBLE, expr.toString());
	}

	/**
	 * Ajout d'une expression ifnull : ifnull (<b>tableAlias.colAlias</b>, <b>valueIfNull</b>) as </b>asName</b>
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de table, <code>null</code>=facultatif
	 * @param asName
	 *            alias de colonne
	 * @param valueIfNull
	 *            valeur si null
	 */
	public void addIfNull(String colAlias, String tableAlias, String asName, Object valueIfNull) {
		if (valueIfNull == null) {
			throw new TechnicalException("addIfNull() : missing value");
		}
		Var outVar = findOutVar(colAlias, tableAlias);
		String function = parseDefaultValue(outVar, valueIfNull);
		String expr;
		if (null != function) {
			if (DbConnection.dbType == Type.ORACLE) {
				expr = "nvl({0}, " + function + ")";
			} else {
				expr = "ifnull({0}, " + function + ")";
			}
		} else {
			if (DbConnection.dbType == Type.ORACLE) {
				expr = "nvl({0}, ?)";
			} else {
				expr = "ifnull({0}, ?)";
			}
			bindValues.add(parse(outVar, valueIfNull));
		}
		addColumn(colAlias, tableAlias, asName, Visibility.VISIBLE, expr.toString());
	}

	/**
	 * Ajoute AND à la fin de clause WHERE
	 */

	public void and() {
		if (whereClause.length() > 0 && !whereClause.endsWith("(")) {
			whereClause = whereClause.concat(" AND ");
		}
	}

	/**
	 * Ajoute OR à la fin de clause WHERE
	 */

	public void or() {
		if (whereClause.length() > 0 && !whereClause.endsWith("(")) {
			whereClause = whereClause.concat(" OR ");
		}
	}

	/**
	 * Ajoute une parenthése ouvrante en fin de clause WHERE
	 */

	public void startGroupCondition() {
		whereClause = whereClause.concat("(");
	}

	/**
	 * Ajoute une parenthése en fin de clause WHERE
	 */

	public void endGroupCondition() {
		whereClause = whereClause.concat(")");
	}

	public boolean isForUpdate() {
		return forUpdate;
	}

	public void setForUpdate(boolean forUpdate) {
		this.forUpdate = forUpdate;
	}

	public void resetSort() {
		this.sortVars = new ArrayList<DbQuery.SortVar>();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Finds an out var from the query with
	 * 
	 * @param columnAlias
	 *            Column alias which is (tableAlias_varName)
	 * @return Var corresponding to the alias, null if there's no such Var
	 */
	public Var getOutVar(String columnAlias) {
		if (columnAlias != null) {
			for (Var v : outVars) {
				if (columnAlias.equals(v.getColumnAlias())) {
					return v;
				}
			}
		}
		return null;
	}

	/**
	 * Lists all category break (order is important) for the query
	 * @return Columns on which we need to break the results for display
	 */
	public List<String> getCategoryBreak() {
		List<String> categoryBreak = new ArrayList<String>();
		for (SortVar s : sortVars) {
			if (s.categorize) {
				categoryBreak.add(s.inVar.getColumnAlias());
			}
		}
		return categoryBreak;
	}
	
	public int getMinRownum() {
		return minRownum;
	}

	public void setMinRownum(int minRownum) {
		this.minRownum = minRownum;
	}
}
