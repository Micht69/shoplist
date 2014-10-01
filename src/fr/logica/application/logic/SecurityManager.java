package fr.logica.application.logic;

import java.util.ArrayList;
import java.util.List;

import fr.logica.business.context.RequestContext;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.domain.constants.ShopArticleConstants;
import fr.logica.domain.constants.ShopListConstants;
import fr.logica.domain.constants.ShopListLArticleConstants;
import fr.logica.domain.constants.ShopShelfConstants;
import fr.logica.domain.constants.ShopUserConstants;
import fr.logica.domain.objects.ShopUser;
import fr.logica.queries.ShopArticleQuery;
import fr.logica.queries.ShopListLArticleQuery;
import fr.logica.queries.ShopListQuery;
import fr.logica.queries.ShopShelfQuery;
import fr.logica.queries.ShopUserQuery;
import fr.logica.security.DefaultSecurityManager;
import fr.logica.security.SecurityFunction;

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
	public User getUser(String login, String password, RequestContext ctx) {
		// Auth through DB
		User user = null;
		DbQuery userQuery = new DbQuery(ShopUserConstants.ENTITY_NAME, "USR");
		userQuery.addCondEq(ShopUserConstants.Vars.LOGIN, "USR", login);
		userQuery.addCondEq(ShopUserConstants.Vars.PASSWORD, "USR", password);

		DbManager dbm = null;
		try {
			dbm = new DbManager(ctx, userQuery);
			if (dbm.next()) {
				ShopUser dbUser = new ShopUser();
				dbm.getEntity("USR", dbUser, ctx);

				user = new User(dbUser);
			}
		} finally {
			if (dbm != null) {
				dbm.close();
			}
		}

		return user;
	}

	@Override
	public List<SecurityFunction> getSecurity(User user, RequestContext ctx) {
		List<SecurityFunction> ret = new ArrayList<SecurityFunction>();
		if (ShopUserConstants.ValueList.PROFILE.USER.equals(user.profile)) {
			getUserSecurity(ret);
		} else if (ShopUserConstants.ValueList.PROFILE.BUYER.equals(user.profile)) {
			getBuyerSecurity(ret);
		} else if (ShopUserConstants.ValueList.PROFILE.ADMIN.equals(user.profile)) {
			getAdminSecurity(ret);
		}
		return ret;
	}

	private void getUserSecurity(List<SecurityFunction> ret) {
		ret.add(getQuerySecurityFunction(ShopListConstants.ENTITY_NAME, ShopListQuery.Query.QUERY_SHOP_LIST));
		ret.add(getActionSecurityFunction(ShopListConstants.ENTITY_NAME, ShopListConstants.Actions.ACTION_0));
		ret.add(getActionSecurityFunction(ShopListConstants.ENTITY_NAME, ShopListConstants.Actions.ACTION_2));

		ret.add(getQuerySecurityFunction(ShopArticleConstants.ENTITY_NAME, ShopArticleQuery.Query.QUERY_SHOP_ARTICLE));
		ret.add(getActionSecurityFunction(ShopArticleConstants.ENTITY_NAME, ShopArticleConstants.Actions.ACTION_5));

		ret.add(getQuerySecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleQuery.Query.QUERY_SHOP_ARTICLES2));
		ret.add(getQuerySecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleQuery.Query.QUERY_SHOP_LIST_L_ARTICLE));
		ret.add(getActionSecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleConstants.Actions.ACTION_0));
		ret.add(getActionSecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleConstants.Actions.ACTION_70));
		ret.add(getActionSecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleConstants.Actions.ACTION_2));
		ret.add(getActionSecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleConstants.Actions.ACTION_51));
	}

	private void getBuyerSecurity(List<SecurityFunction> ret) {
		getUserSecurity(ret);

		ret.add(getActionSecurityFunction(ShopListConstants.ENTITY_NAME, ShopListConstants.Actions.ACTION_50));

		ret.add(getActionSecurityFunction(ShopArticleConstants.ENTITY_NAME, ShopArticleConstants.Actions.ACTION_0));
		ret.add(getActionSecurityFunction(ShopArticleConstants.ENTITY_NAME, ShopArticleConstants.Actions.ACTION_2));
		ret.add(getActionSecurityFunction(ShopArticleConstants.ENTITY_NAME, ShopArticleConstants.Actions.ACTION_50));

		ret.add(getQuerySecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleQuery.Query.QUERY_SHOP_ARTICLES_SHOPPING));
		ret.add(getActionSecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleConstants.Actions.ACTION_60));
		ret.add(getActionSecurityFunction(ShopListLArticleConstants.ENTITY_NAME, ShopListLArticleConstants.Actions.ACTION_20));

		ret.add(getQuerySecurityFunction(ShopShelfConstants.ENTITY_NAME, ShopShelfQuery.Query.QUERY_SHOP_SHELF));
		ret.add(getActionSecurityFunction(ShopShelfConstants.ENTITY_NAME, ShopShelfConstants.Actions.ACTION_0));
		ret.add(getActionSecurityFunction(ShopShelfConstants.ENTITY_NAME, ShopShelfConstants.Actions.ACTION_2));
		ret.add(getActionSecurityFunction(ShopShelfConstants.ENTITY_NAME, ShopShelfConstants.Actions.ACTION_4));
		ret.add(getActionSecurityFunction(ShopShelfConstants.ENTITY_NAME, ShopShelfConstants.Actions.ACTION_5));
	}

	private void getAdminSecurity(List<SecurityFunction> ret) {
		getBuyerSecurity(ret);

		ret.add(getActionSecurityFunction(ShopListConstants.ENTITY_NAME, ShopListConstants.Actions.ACTION_4));

		ret.add(getActionSecurityFunction(ShopArticleConstants.ENTITY_NAME, ShopArticleConstants.Actions.ACTION_4));

		ret.add(getQuerySecurityFunction(ShopUserConstants.ENTITY_NAME, ShopUserQuery.Query.QUERY_SHOP_USER));
		ret.add(getActionSecurityFunction(ShopUserConstants.ENTITY_NAME, ShopUserConstants.Actions.ACTION_0));
		ret.add(getActionSecurityFunction(ShopUserConstants.ENTITY_NAME, ShopUserConstants.Actions.ACTION_2));
		ret.add(getActionSecurityFunction(ShopUserConstants.ENTITY_NAME, ShopUserConstants.Actions.ACTION_4));
		ret.add(getActionSecurityFunction(ShopUserConstants.ENTITY_NAME, ShopUserConstants.Actions.ACTION_5));
	}
}
