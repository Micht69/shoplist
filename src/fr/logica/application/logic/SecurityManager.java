package fr.logica.application.logic;

import java.util.ArrayList;
import java.util.List;

import fr.logica.business.context.RequestContext;
import fr.logica.db.DB;
import fr.logica.db.DbConnection;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.domain.models.ShopArticleModel;
import fr.logica.domain.models.ShopListLArticleModel;
import fr.logica.domain.models.ShopListModel;
import fr.logica.domain.models.ShopShelfModel;
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
		DbQuery userQuery = new DbQuery(ShopUserModel.ENTITY_NAME, "USR");
		userQuery.addCondEq(ShopUser.Var.LOGIN, "USR", login);
		userQuery.addCondEq(ShopUser.Var.PASSWORD, "USR", password);

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
		if (ShopUser.ValueList.PROFILE.USER.equals(user.profile)) {
			getUserSecurity(ret);
		} else if (ShopUser.ValueList.PROFILE.BUYER.equals(user.profile)) {
			getBuyerSecurity(ret);
		} else if (ShopUser.ValueList.PROFILE.ADMIN.equals(user.profile)) {
			getAdminSecurity(ret);
		}
		return ret;
	}

	private void getUserSecurity(List<SecurityFunction> ret) {
		ret.add(getQuerySecurityFunction(ShopListModel.ENTITY_NAME, ShopListQuery.Query.QUERY_SHOP_LIST));
		ret.add(getActionSecurityFunction(ShopListModel.ENTITY_NAME, ShopList.Action.ACTION_0));
		ret.add(getActionSecurityFunction(ShopListModel.ENTITY_NAME, ShopList.Action.ACTION_2));

		ret.add(getQuerySecurityFunction(ShopArticleModel.ENTITY_NAME, ShopArticleQuery.Query.QUERY_SHOP_ARTICLE));
		ret.add(getActionSecurityFunction(ShopArticleModel.ENTITY_NAME, ShopArticle.Action.ACTION_5));

		ret.add(getQuerySecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticleQuery.Query.QUERY_SHOP_ARTICLES2));
		ret.add(getQuerySecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticleQuery.Query.QUERY_SHOP_LIST_L_ARTICLE));
		ret.add(getActionSecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticle.Action.ACTION_0));
		ret.add(getActionSecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticle.Action.ACTION_70));
		ret.add(getActionSecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticle.Action.ACTION_2));
		ret.add(getActionSecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticle.Action.ACTION_51));
	}

	private void getBuyerSecurity(List<SecurityFunction> ret) {
		getUserSecurity(ret);

		ret.add(getActionSecurityFunction(ShopListModel.ENTITY_NAME, ShopList.Action.ACTION_50));

		ret.add(getActionSecurityFunction(ShopArticleModel.ENTITY_NAME, ShopArticle.Action.ACTION_0));
		ret.add(getActionSecurityFunction(ShopArticleModel.ENTITY_NAME, ShopArticle.Action.ACTION_2));
		ret.add(getActionSecurityFunction(ShopArticleModel.ENTITY_NAME, ShopArticle.Action.ACTION_50));

		ret.add(getQuerySecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticleQuery.Query.QUERY_SHOP_ARTICLES_SHOPPING));
		ret.add(getActionSecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticle.Action.ACTION_60));
		ret.add(getActionSecurityFunction(ShopListLArticleModel.ENTITY_NAME, ShopListLArticle.Action.ACTION_20));

		ret.add(getQuerySecurityFunction(ShopShelfModel.ENTITY_NAME, ShopShelfQuery.Query.QUERY_SHOP_SHELF));
		ret.add(getActionSecurityFunction(ShopShelfModel.ENTITY_NAME, ShopShelf.Action.ACTION_0));
		ret.add(getActionSecurityFunction(ShopShelfModel.ENTITY_NAME, ShopShelf.Action.ACTION_2));
		ret.add(getActionSecurityFunction(ShopShelfModel.ENTITY_NAME, ShopShelf.Action.ACTION_4));
		ret.add(getActionSecurityFunction(ShopShelfModel.ENTITY_NAME, ShopShelf.Action.ACTION_5));
	}

	private void getAdminSecurity(List<SecurityFunction> ret) {
		getBuyerSecurity(ret);

		ret.add(getActionSecurityFunction(ShopListModel.ENTITY_NAME, ShopList.Action.ACTION_4));

		ret.add(getActionSecurityFunction(ShopArticleModel.ENTITY_NAME, ShopArticle.Action.ACTION_4));

		ret.add(getQuerySecurityFunction(ShopUserModel.ENTITY_NAME, ShopUserQuery.Query.QUERY_SHOP_USER));
		ret.add(getActionSecurityFunction(ShopUserModel.ENTITY_NAME, ShopUser.Action.ACTION_0));
		ret.add(getActionSecurityFunction(ShopUserModel.ENTITY_NAME, ShopUser.Action.ACTION_2));
		ret.add(getActionSecurityFunction(ShopUserModel.ENTITY_NAME, ShopUser.Action.ACTION_4));
		ret.add(getActionSecurityFunction(ShopUserModel.ENTITY_NAME, ShopUser.Action.ACTION_5));
	}
}
