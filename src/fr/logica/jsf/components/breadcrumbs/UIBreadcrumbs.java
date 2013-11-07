package fr.logica.jsf.components.breadcrumbs;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

import fr.logica.ui.Page;

@FacesComponent(UIBreadcrumbs.COMPONENT_ID)
public class UIBreadcrumbs extends UICommand {

	public static final String COMPONENT_ID = "cgi.faces.breadcrumbs";

	private Page<?> page;

	private MethodExpression method;

	private String onclick;

	public Page<?> getPage() {
		return page;
	}

	public void setPage(Page<?> page) {
		this.page = page;
	}

	public MethodExpression getMethod() {
		return method;
	}

	public void setMethod(MethodExpression method) {
		this.method = method;
	}

	public String getOnclick() {
		return onclick;
	}

	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	@Override
	public void encodeBegin(FacesContext ctx) throws IOException {
		page = (Page<?>) getAttributes().get("page");
		method = (MethodExpression) getAttributes().get("method");
		onclick = (String) getAttributes().get("onclick");

		ResponseWriter writer = ctx.getResponseWriter();

		writer.write('\n');
		writer.startElement("div", null);
		writer.writeAttribute("id", "breadCrumb", null);
		writer.writeAttribute("class", "ariane", null);

		if (page != null) {
			writer.write("\n\t\t");
			writeHiddenIndex(writer);

			/* most of the fun is happening here */
			writeBreadcrumbs(writer, page, 0);
			writer.write("\n");
		}
		writer.endElement("div");
	}

	private void writeHiddenIndex(ResponseWriter writer) throws IOException {
		writer.startElement("input", this);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", getFormHiddenFieldId(), null);
		writer.writeAttribute("name", getFormHiddenFieldId(), null);
		writer.endElement("input");
	}

	private void writeBreadcrumbs(ResponseWriter writer, Page<?> currentPage, int backIndex) throws IOException {

		/* Recursion on upper pages */
		Page<?> nextPage = currentPage.getNextPage();
		if (nextPage != null) {
			writeBreadcrumbs(writer, nextPage, backIndex + 1);
			writeSeparator(writer);
		}

		/* Working on current page */
		writeCrumb(writer, currentPage, backIndex);
	}

	private void writeCrumb(ResponseWriter writer, Page<?> currentPage, int backIndex) throws IOException {
		writer.write("\n\t\t");
		writer.startElement("span", null);
		if (backIndex != 0) {
			writer.startElement("a", null);
			writer.writeAttribute("href", "#", null);
			writer.writeAttribute("onclick", getJavascript(backIndex), null);
		}
		writer.write(currentPage.getTitle());
		if (backIndex != 0) {
			writer.endElement("a");
		}
		writer.endElement("span");
	}

	private void writeSeparator(ResponseWriter writer) throws IOException {
		writer.write("\n\t\t");
		writer.startElement("span", null);
		writer.writeAttribute("class", "separator", null);
		writer.write(" > ");
		writer.endElement("span");
	}

	private String getJavascript(int backIndex) throws IOException {
		String click = "document.getElementById(\\'" + getFormHiddenFieldId() + "\\').value=" + backIndex;
		String submit = "document.getElementById(\\'" + getParent().getClientId() + "\\').submit()";

		String chain = "jsf.util.chain(this,event,'"
				+ (onclick == null ? "" : onclick + "','")
				+ click + "','" + submit + "') ; return false";
		return chain;
	}

	private String getFormHiddenFieldId() {
		return getClientId() + ".breadcrumbsForm.clicked";
	}

	@Override
	public void decode(FacesContext context) {
		Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
		String strClicked = parameters.get(getFormHiddenFieldId());
		if (strClicked != null) {

			// FIXME Use an event to trigger the method correctly, and extend UiForm instead of UiCommand
			/* Un peu crade mais j'ai pas de meilleure idée */
			Matcher matcher = Pattern.compile("\\#\\{(.*)\\}").matcher(method.getExpressionString());
			matcher.matches();
			String text = matcher.group(1);
			String newExpression = "#{" + text + "(" + strClicked + ")}";
			MethodExpression action = ExpressionFactory.newInstance().createMethodExpression(context.getELContext(), newExpression,
					String.class, null);

			setActionExpression(action);
			queueEvent(new ActionEvent(this));
		}
	}

}
