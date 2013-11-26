package fr.logica.security;

import java.util.ArrayList;
import java.util.List;

import fr.logica.business.Context;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.domain.models.ShopUserModel;
import fr.logica.domain.objects.ShopArticle;
import fr.logica.domain.objects.ShopList;
import fr.logica.domain.objects.ShopListLArticle;
import fr.logica.domain.objects.ShopShelf;
import fr.logica.domain.objects.ShopUser;
import fr.logica.queries.ShopArticleQuery;
import fr.logica.queries.ShopListLArticleQuery;
import fr.logica.queries.ShopListQuery;
import fr.logica.queries.ShopShelfQuery;
import fr.logica.queries.ShopUserQuery;


/**
 * Extensible class to store Security specific behavior. It must implement AbstractSecurityManager. 
 * 
 * @author bellangerf
 * 
 */
public class SecurityManager extends DefaultSecurityManager {
	/**
	 * serial UID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean disableSecurity() {
		return false;
	}

	@Override
	public ApplicationUser getUser(String login, String password) {
		// Auth through DB
		User user = null;
		DbQuery userQuery = new DbQuery(ShopUserModel.ENTITY_NAME, "USR");
		userQuery.addCondEq(ShopUser.Var.LOGIN, "USR", login);
		userQuery.addCondEq(ShopUser.Var.PASSWORD, "USR", password);
		
		Context ctx = new Context(null);
		DbManager dbm = new DbManager(ctx, userQuery);
		if (dbm.next()) {
			ShopUser dbUser = new ShopUser();
			dbm.getEntity("USR", dbUser, ctx);
			
			user = new User(dbUser);
		}
		ctx.close();
		
		return user;
	}

	@Override
	public List<SecurityFunction> getSecurity(ApplicationUser pUser) {
		if (pUser instanceof User) {
			User user = (User) pUser;

			List<SecurityFunction> ret = new ArrayList<SecurityFunction>();
			if (ShopUser.ValueList.PROFILE.USER.equals(user.profile)) {
				getUserSecurity(ret);
			} else if (ShopUser.ValueList.PROFILE.BUYER.equals(user.profile)) {
				getUserSecurity(ret);
				getBuyerSecurity(ret);
			} else if (ShopUser.ValueList.PROFILE.ADMIN.equals(user.profile)) {
				getUserSecurity(ret);
				getBuyerSecurity(ret);
				getAdminSecurity(ret);
			}
			return ret;
		}
		return super.getSecurity(pUser);
	}

	private void getUserSecurity(List<SecurityFunction> ret) {
		ret.add(getQuerySecurityFunction("SHOP_LIST", ShopListQuery.Query.QUERY_SHOP_LIST));
		ret.add(getActionSecurityFunction("SHOP_LIST", ShopList.Action.ACTION_0));
		ret.add(getActionSecurityFunction("SHOP_LIST", ShopList.Action.ACTION_2));
		// ret.add(getActionSecurityFunction("SHOP_LIST", ShopList.Action.ACTION_5));

		ret.add(getQuerySecurityFunction("SHOP_ARTICLE", ShopArticleQuery.Query.QUERY_SHOP_ARTICLE));
		ret.add(getActionSecurityFunction("SHOP_ARTICLE", ShopArticle.Action.ACTION_5));

		ret.add(getQuerySecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticleQuery.Query.QUERY_SHOP_ARTICLES2));
		ret.add(getQuerySecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticleQuery.Query.QUERY_SHOP_LIST_L_ARTICLE));
		ret.add(getActionSecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticle.Action.ACTION_0));
		ret.add(getActionSecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticle.Action.ACTION_2));
		ret.add(getActionSecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticle.Action.ACTION_4));
		ret.add(getActionSecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticle.Action.ACTION_5));
		ret.add(getActionSecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticle.Action.ACTION_51));
	}

	private void getBuyerSecurity(List<SecurityFunction> ret) {
		ret.add(getActionSecurityFunction("SHOP_LIST", ShopList.Action.ACTION_50));

		ret.add(getActionSecurityFunction("SHOP_ARTICLE", ShopArticle.Action.ACTION_0));
		ret.add(getActionSecurityFunction("SHOP_ARTICLE", ShopArticle.Action.ACTION_2));
		ret.add(getActionSecurityFunction("SHOP_ARTICLE", ShopArticle.Action.ACTION_50));

		ret.add(getQuerySecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticleQuery.Query.QUERY_SHOP_ARTICLES_SHOPPING));
		ret.add(getActionSecurityFunction("SHOP_LIST_L_ARTICLE", ShopListLArticle.Action.ACTION_60));

		ret.add(getQuerySecurityFunction("SHOP_SHELF", ShopShelfQuery.Query.QUERY_SHOP_SHELF));
		ret.add(getActionSecurityFunction("SHOP_SHELF", ShopShelf.Action.ACTION_0));
		ret.add(getActionSecurityFunction("SHOP_SHELF", ShopShelf.Action.ACTION_2));
		ret.add(getActionSecurityFunction("SHOP_SHELF", ShopShelf.Action.ACTION_4));
		ret.add(getActionSecurityFunction("SHOP_SHELF", ShopShelf.Action.ACTION_5));
	}

	private void getAdminSecurity(List<SecurityFunction> ret) {
		ret.add(getActionSecurityFunction("SHOP_LIST", ShopList.Action.ACTION_4));

		ret.add(getActionSecurityFunction("SHOP_ARTICLE", ShopArticle.Action.ACTION_4));

		ret.add(getQuerySecurityFunction("SHOP_USER", ShopUserQuery.Query.QUERY_SHOP_USER));
		ret.add(getActionSecurityFunction("SHOP_USER", ShopUser.Action.ACTION_0));
		ret.add(getActionSecurityFunction("SHOP_USER", ShopUser.Action.ACTION_2));
		ret.add(getActionSecurityFunction("SHOP_USER", ShopUser.Action.ACTION_4));
		ret.add(getActionSecurityFunction("SHOP_USER", ShopUser.Action.ACTION_5));
	}
}
