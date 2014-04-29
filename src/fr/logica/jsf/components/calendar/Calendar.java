package fr.logica.jsf.components.calendar;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;

import javax.faces.component.FacesComponent;

import fr.logica.application.ApplicationUtils;

@FacesComponent(Calendar.COMPONENT_TYPE)
public class Calendar extends org.primefaces.component.calendar.Calendar {

	protected enum PropertyKeys {

		precision;

		String toString;

		PropertyKeys(String toString) {
			this.toString = toString;
		}

		PropertyKeys() {
		}

		public String toString() {
			return ((this.toString != null) ? this.toString : super.toString());
		}
	}

	public String getPrecision() {
		return (String) getStateHelper().eval(PropertyKeys.precision, null);
	}

	public void setPrecision(String precision) {
		getStateHelper().put(PropertyKeys.precision, precision);
		handleAttribute("precision", precision);
	}

	@Override
	public String getPattern() {
		String pattern = super.getPattern();
		if (null == pattern) {
			String precision = getPrecision();
			if ("date".equals(precision)) {
				pattern = ApplicationUtils.getApplicationLogic().getDateFormat();
			} else if ("datetime".equals(precision)) {
				pattern = ApplicationUtils.getApplicationLogic().getDatetimeFormat();
			} else if ("timestamp".equals(precision)) {
				pattern = ApplicationUtils.getApplicationLogic().getTimestampFormat();
			} else if ("time".equals(precision)) {
				pattern = ApplicationUtils.getApplicationLogic().getTimeFormat();
			} else {
				throw new IllegalArgumentException(precision);
			}
		}
		return (pattern == null) ? pattern : pattern.replace('S', 'l');
	}

	@Override
	public int getMaxSecond() {
		int maxSecond = super.getMaxSecond();
		String pattern = getPattern().replace('l', 'S');

		if (pattern.indexOf('s') == -1) {
			return 0;
		}
		return maxSecond;
	}

	@Override
	public int getMaxlength() {
		int maxLength = super.getMaxlength();

		if (maxLength <= 0) {
			String pattern = getPattern().replace('l', 'S');

			SimpleDateFormat format = new SimpleDateFormat(pattern, super.calculateLocale(getFacesContext()));
			DateFormatSymbols symbols = format.getDateFormatSymbols();
			String newPattern = pattern.replace("MMMM", "");
			newPattern = newPattern.replace("MMM", "");
			newPattern = newPattern.replaceAll("[GEaSzZ]", "");
			maxLength = newPattern.length();

			if (pattern.indexOf('G') > -1) {
				maxLength += getMax(symbols.getEras());
			}
			if (pattern.indexOf("MMMM") > -1) {
				maxLength += getMax(symbols.getMonths());
			} else if (pattern.indexOf("MMM") > -1) {
				maxLength += getMax(symbols.getShortMonths());
			}
			if (pattern.indexOf("EEEE") > -1) {
				maxLength += getMax(symbols.getWeekdays());
			} else if (pattern.indexOf("EEE") > -1) {
				maxLength += getMax(symbols.getShortWeekdays());
			}
			if (pattern.indexOf('a') > -1) {
				maxLength += getMax(symbols.getAmPmStrings());
			}
			if (pattern.indexOf('S') > -1) {
				maxLength += 3;
			}
		}
		return maxLength;
	}

	private int getMax(String[] str) {
		int max = 0;
		for (int i = 0; i < str.length; i++) {
			max = Math.max(max, str[i].length());
		}
		return max;
	}

}
