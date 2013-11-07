package fr.logica.jsf.utils;


import javax.el.ELResolver;
import javax.faces.context.FacesContext;

public class JSFBeanUtils {

	/**
	 * Constructeur privé : la classe n'est pas instanciable.
	 */
	private JSFBeanUtils() {
		/* rien */
	}

	/**
	 * Renvoie le bean managé portant le nom passé en paramètre dans le contexte ctx.
	 * 
	 * @param ctx
	 *            Contexte JSF où chercher le bean.
	 * @param name
	 *            Nom du bean recherché.
	 * @return Le bean managé recherché.
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
