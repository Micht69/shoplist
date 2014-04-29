/**
 * 
 */
package fr.logica.business;

/**
 * @author logica
 * 
 */
public final class Constants {

	/**
	 * Private constructor, Utility class
	 */
	private Constants() {
	}

	public static final String NOT_YET_IMPLEMENTED = "NOT YET IMPLEMENTED";

	/* variables des classes custom */
	public static final String DOMAIN_MODELS_PACKAGE = "fr.logica.domain.models";
	public static final String DOMAIN_OBJECT_PACKAGE = "fr.logica.domain.objects";
	public static final String DOMAIN_LOGIC_PACKAGE = "fr.logica.domain.logic";
	public static final String QUERIES_PACKAGE = "fr.logica.queries";
	public static final String DB_PACKAGE = "fr.logica.db";

	/* extensions */
	public static final String EXTENSION_SEQUENCE = "S_";
	public static final String EXTENSION_SELECT = "_SELECT";
	public static final String EXTENSION_CONTROLLER = "Ctrl";
	public static final String EXTENSION_MODEL = "Model";
	public static final String EXTENSION_QUERY = "Query";
	public static final String EXTENSION_LOGIC = "Logic";

	/* formats */
	public static final String FORMAT_DATE = "dd/MM/yyyy";
	public static final String FORMAT_TIME = "HH:mm:ss";
	public static final String FORMAT_HOUR = "HH:mm";
	public static final String FORMAT_MS = "S";
	public static final String FORMAT_TIMESTAMP = FORMAT_DATE + " " + FORMAT_TIME + "." + FORMAT_MS;

	public static final String FORMAT_DATE_ISO = "yyyy-MM-dd";
	public static final String FORMAT_TIMESTAMP_ISO = FORMAT_DATE_ISO + " " + FORMAT_TIME + "." + FORMAT_MS;;

	/* type action */
	public static final int DUMMY = -42;
	public static final int DISPLAY_FILE = -3;
	public static final int DELETE_FILE = -2;
	public static final int DETACH = -1;
	public static final int DETACH_BR = -111;
	public static final int CREATE = 0;
	public static final int SELECT = 1;
	public static final int SELECT_BR = 111;
	public static final int MODIFY = 2;
	public static final int COPY = 3;
	public static final int DELETE = 4;
	public static final int DISPLAY = 5;
	public static final int SINGLE_ELT_PRINT = 6;
	public static final int NO_ELT_PRINT = 7;
	public static final int NO_ELT_CUSTOM_ACTION = 8;
	public static final int NO_ELT_CUSTOM_ACTION_DISPLAY = 14;
	public static final int CUSTOM_ACTION = 10;
	public static final int CUSTOM_ACTION_DISPLAY = 11;
	public static final int SINGLE_ELT_CUSTOM_ACTION = 16;
	public static final int SINGLE_ELT_CUSTOM_ACTION_DISPLAY = 13;
	public static final int SEARCH = -4;
	public static final int RENAME = -5;

	/* code action par défaut */
	public static final int ACTION_CREATE = 0;
	public static final int ACTION_MODIFY = 2;
	public static final int ACTION_COPY = 3;
	public static final int ACTION_DELETE = 4;
	public static final int ACTION_DISPLAY = 5;

	/* Chaines utilisÃ©es pour identifier des trucs dans le code */
    public static final String RESULT_PK = "primaryKey";
    public static final String RESULT_ROWNUM = "$rownum";
	public static final String CUSTOM_DATA = "cData_key_";
	public static final String PERMALINK_LOGIN_KEY = "permalinkKey"; 

	public static final String EVENT = "event";
	public static final String EVENT_TITLE = "title";
	public static final String EVENT_ALL_DAY = "allDay";
	public static final String EVENT_READ_ONLY = "readOnly";
	public static final String EVENT_DATE_START = "dateStart";
	public static final String EVENT_DATE_END = "dateEnd";
	public static final String EVENT_CSS_CLASSNAME = "className";
	public static final String EVENT_CSS_COLOR = "color";
	public static final String EVENT_CSS_BACKGROUND_COLOR = "backgroundColor";
	public static final String EVENT_CSS_BORDER_COLOR = "borderColor";
	public static final String EVENT_CSS_TEXT_COLOR = "textColor";
	public static final String EVENT_CREATE_DATE_START = "createStartTime";
	public static final String EVENT_CREATE_DATE_END = "createEndTime";

	/** Préfixe de la variable contenant l'adresse (associée à une variable de type Geometry). */
	public final static String GEOMETRY_ADDRESS = "geoadr";

	/* Ressources binaires gérées manuellement */
	public static final String CUSTOM_RESSOURCE = "customRessource";

	/** Maximum number of rows initially defined on the criteria page */
	public static final int MAX_ROW = 200;
	
	/** Absolute maximum for the number of row, impossible for the user to bypass. Not applicable to custom code. */
	public static final int MAX_ROW_ABSOLUTE = 9999;
	
	public static final int AUTOCOMPLETE_MAX_ROW = 20;
	public static final int EVENT_CREATE_DEFAULT_DURATION = 2;

}
