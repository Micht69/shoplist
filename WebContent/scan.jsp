<%@page import="fr.logica.ui.Page"%>
<%@page import="fr.logica.security.User"%>
<%@page import="fr.logica.business.Constants"%>
<%@page import="fr.logica.domain.models.ShopArticleModel"%>
<%@page import="fr.logica.domain.objects.ShopArticle"%>
<%@page import="javax.faces.context.FacesContext"%>
<%@page import="fr.logica.jsf.utils.JSFBeanUtils"%>
<%@page import="fr.logica.jsf.controller.JSFController"%>
<%
	String code = request.getParameter("code");
	String shelf = request.getParameter("shelf");
	
	JSFController jsfCtrl = (JSFController) request.getSession().getAttribute("jsfCtrl");
	String url = "";
	
	if (jsfCtrl != null) {
		if (code != null && !"".equals(code)) {
			// Store in user
			User user = (User) jsfCtrl.getSessionCtrl().getUser();
			user.eanCode = code;
			user.eanShelf = shelf;
		}
	
		// Get action URL for redirect
		Page p = jsfCtrl.getSessionCtrl().getPage().getNextPage();
		jsfCtrl.setPage(p);
		url = jsfCtrl.prepareSingleAction(ShopArticleModel.ENTITY_NAME, ShopArticle.Action.ACTION_50, Constants.CREATE, null, null);
		url += ".jsf";
	}
%>
<html>
<head>
	<meta http-equiv="refresh" content="0; url=<%= request.getContextPath() + url %>">
</head>
<body>
</body>
</html>