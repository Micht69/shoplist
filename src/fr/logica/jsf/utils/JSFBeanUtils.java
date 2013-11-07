package fr.logica.jsf.utils;


import javax.el.ELResolver;
import javax.faces.context.FacesContext;

public class JSFBeanUtils {

	/**
	 * Constructeur priv� : la classe n'est pas instanciable.
	 */
	private JSFBeanUtils() {
		/* rien */
	}

	/**
	 * Renvoie le bean manag� portant le nom pass� en param�tre dans le contexte ctx.
	 * 
	 * @param ctx
	 *            Contexte JSF o� chercher le bean.
	 * @param name
	 *            Nom du bean recherch�.
	 * @return Le bean manag� recherch�.
	 */
	public static Object getManagedBean(final FacesContext ctx, final String name) {
		try {
			ELResolver resolver = ctx.getELContext().getELResolver();
			Object bean = resolver.getValue(ctx.getELContext(), null, name);
			return bean;
		} catch (Exception e) {
			// In case JSF2 is not initialized yet
			return null;
		}
	}
}
