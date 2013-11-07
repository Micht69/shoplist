package fr.logica.jsf.components.checkbox;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.convert.Converter;
import javax.faces.view.facelets.ConverterConfig;
import javax.faces.view.facelets.ConverterHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.TagAttribute;

public class ConvertBooleanHandler extends ConverterHandler {

	private static final String JAVA_LANG_PACKAGE = "java.lang.";
	private final TagAttribute type;
	private final TagAttribute checkedValue;
	private final TagAttribute uncheckedValue;

	public ConvertBooleanHandler(ConverterConfig config) {
		super(config);
		type = this.getAttribute("type");
		checkedValue = this.getAttribute("checkedValue");
		uncheckedValue = this.getAttribute("uncheckedValue");
	}

	protected Converter createConverter(FaceletContext ctx) throws FacesException, ELException, FaceletException {
		return ctx.getFacesContext().getApplication().createConverter(BooleanConverter.CONVERTER_ID);
	}

	@Override
	public void setAttributes(FaceletContext ctx, Object obj) {
		BooleanConverter c = (BooleanConverter) obj;
		Class<?> clazz = null;

		if (null != type) {
			String classString = type.getValue();

			if (classString.indexOf('.') < 0) {
				classString = JAVA_LANG_PACKAGE + classString;
			}

			try {
				clazz = Class.forName(classString);
			} catch (ClassNotFoundException e) {
				clazz = String.class;
			}
		}

		if (null == clazz) {
			clazz = String.class;
		}

		if (null != checkedValue) {
			c.setCheckedValue(checkedValue.getObject(ctx, clazz));
		}

		if (null != uncheckedValue) {
			c.setUncheckedValue(uncheckedValue.getObject(ctx, clazz));
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public MetaRuleset createMetaRuleset(Class type) {
		return super.createMetaRuleset(type).ignoreAll();
	}

}
