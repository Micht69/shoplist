package fr.logica.business;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Classe regroupant des m�thodes utilitaires pour g�rer les dates. Comprend de base pas mal de choses h�rit�es du DateUtils de Apache.
 * 
 */
public final class DateUtils {

	private DateUtils() {
	}

	/**
	 * 
	 * Renvoie la date sous le format "dd/MM/yyyy hh:mm".
	 * 
	 * @param date
	 *            date � formatter.
	 * @return la date formatt�e.
	 */
	public static String formatDateHeure(final Date date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			return format.format(date);
		}
		return "";
	}

	/**
	 * 
	 * Renvoie la date sous le format "dd/MM/yyyy".
	 * 
	 * @param date
	 *            date � formatter.
	 * @return la date formatt�e.
	 */
	public static String formatDate(final Date date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			return format.format(date);
		}
		return "";
	}

	/**
	 * convertit une cha�ne au format Date.
	 * 
	 * @param sDate
	 *            date au format String.
	 * @return Date
	 * @throws Exception
	 */
	public static Date stringToDate(String sDate) throws Exception {
		SimpleDateFormat stringToDate;
		try {
			stringToDate = new SimpleDateFormat("dd/MM/yyyy");
			return stringToDate.parse(sDate);
		} catch (ParseException e) {
			stringToDate = new SimpleDateFormat("yyyy-MM-dd");
			return stringToDate.parse(sDate);
		}
	}

	/**
	 * convertit une cha�ne au format Time.
	 * 
	 * @param sTime
	 *            time au format String.
	 * @return Time
	 * @throws Exception
	 */
	public static Time stringToTime(String sTime) throws Exception {
		SimpleDateFormat stringToTime = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(stringToTime.parse(sTime).getTime());
		return new Time(cal.getTimeInMillis());
	}

	/**
	 * convertit une cha�ne au format TimeStamp.
	 * 
	 * @param sTimestamp
	 *            timestamp au format String.
	 * @return Timestamp
	 * @throws Exception
	 */
	public static Timestamp stringToTimestamp(String sTimestamp) throws Exception {
		Date d;
		SimpleDateFormat stringToDate;
		try {
			stringToDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");
			d = stringToDate.parse(sTimestamp);
		} catch (ParseException e) {
			stringToDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			d = stringToDate.parse(sTimestamp);
		}
		return new Timestamp(d.getTime());
	}

	public static Date addJours(Date d, int nbJours) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, nbJours);
		return c.getTime();
	}

	public static Date addMois(Date d, int nbMois) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, nbMois);
		return c.getTime();
	}

	/**
	 * Renvoie une date initialis�e au moment de l'appel de cette m�thode. La pr�cision est la millisecondes.
	 * 
	 * @return Timestamp correspondant au moment pr�sent.
	 */
	public static Timestamp todayNow() {
		return new Timestamp(Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * Renvoie une date initialis�e au moment de l'appel de cette m�thode. La pr�cision est la seconde.
	 * 
	 * @return Time correspondant au moment pr�sent.
	 */
	public static Time now() {
		return new Time(Calendar.getInstance().getTimeInMillis() / 1000);
	}

	/**
	 * Renvoie une date initialis�e au jour de l'appel de cette m�thode. La pr�cision est la journ�e. L'heure est fix�e � z�ro.
	 * 
	 * @return Date correspondant au jour de l'appel.
	 */
	public static Date today() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Renvoie la date courante sous le format "yyMMdd"
	 * 
	 * @return date au format YYMMDD
	 */
	public static String getDateYYMMDD() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
		Date currentTime = new Date();
		String date = formatter.format(currentTime);

		return date;
	}

	/**
	 * M�thode calculant le nombre d'heure s�parant deux dates
	 * 
	 * @param dateA
	 * @param dateB
	 * @return Nombre d'heures s�parant deux dates
	 */
	public static int getNbHours(Timestamp dateA, Timestamp dateB) {
		return (int) ((dateB.getTime() - dateA.getTime()) / 3600000);
	}

	/**
	 * Retourne la date initialis�e au moment de l'appel au format "yyyyMMddHHmmssSSS".
	 * 
	 * @return cha�ne de caract�re.
	 */
	public static String nowTimestamp() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String dateFormatee = format.format(DateUtils.todayNow());
		return dateFormatee;
	}
}
