package fr.logica.domain.logic;

import java.util.Map;

import fr.logica.business.Action;
import fr.logica.business.DefaultLogic;
import fr.logica.business.context.RequestContext;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.domain.constants.ShopListConstants;
import fr.logica.domain.constants.ShopListLArticleConstants;
import fr.logica.domain.objects.ShopList;

/**
 * Logic class for the entity ShopList
 *
 * @author CGI
 */
public class ShopListLogic extends DefaultLogic<ShopList> implements ShopListConstants {
	@Override
	public Object uiListVarValue(Map<String, Object> vars, String queryName, String domainName, String varName, RequestContext ctx) {
		if (ShopListConstants.Vars.ARTICLE_COUNT.equals(varName)) {
			// Get article count
			DbQuery artCount = new DbQuery(ShopListLArticleConstants.ENTITY_NAME, "ART");
			artCount.addCondEq(ShopListLArticleConstants.Vars.LIST_ID, "ART", vars.get("T1_id"));
			artCount.addCount(ShopListLArticleConstants.Vars.ARTICLE_ID, "ART", "artCount", false);

			DbManager dbm = null;
			try {
				dbm = new DbManager(ctx, artCount);
				if (dbm.next()) {
					return dbm.getInt(1);
				}
			} catch (Exception e) {
				return 0;
			} finally {
				if (dbm != null)
					dbm.close();
			}
		}
		return super.uiListVarValue(vars, queryName, domainName, varName, ctx);
	}
	
	@Override
	public void dbPostLoad(ShopList bean, Action action, RequestContext ctx) {
		if (action.is(ShopListConstants.Actions.ACTION_0)) {
			bean.setUser(ctx.getSessionContext().getUser().getLogin());
		}
		super.dbPostLoad(bean, action, ctx);
	}
}
