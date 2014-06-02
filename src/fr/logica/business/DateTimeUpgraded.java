package fr.logica.business;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

// TODO to be replaced by DateUtils
public class DateTimeUpgraded {

	String valeur;

	public DateTimeUpgraded(String valeur) {
		this.valeur = valeur;
	}

	public Date getDate()
	{
		try {
			if ("NOW".equals(valeur)) {
				return DateUtils.today();
			} else {
				return DateUtils.stringToDate(valeur);
			}
		} catch (Exception e) {
			return null;
		}
	}

	public Time getTime()
	{
		try {
			if ("NOW".equals(valeur)) {
				return DateUtils.now();
			} else {
				return DateUtils.stringToTime(valeur);
			}
		} catch (Exception e) {
			return null;
		}
	}

	public Timestamp getTimestamp()
	{
		try {
			if ("NOW".equals(valeur)) {
				return DateUtils.todayNow();
			} else {
				return DateUtils.stringToTimestamp(valeur);
			}
		} catch (Exception e) {
			return null;
		}
	}
}
