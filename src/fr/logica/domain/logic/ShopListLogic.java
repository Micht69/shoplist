package fr.logica.domain.logic;

import java.util.Map;

import fr.logica.business.Context;
import fr.logica.business.DefaultLogic;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.domain.models.ShopListLArticleModel;
import fr.logica.domain.objects.ShopList;
import fr.logica.domain.objects.ShopListLArticle;

/**
 * Logic class for the entity ShopList
 *
 * @author CGI
 */
public class ShopListLogic extends DefaultLogic<ShopList> {
	@Override
	public Object uiListVarValue(Map<String, Object> vars, String queryName, String domainName, String varName, Context ctx) {
		if (ShopList.Var.ARTICLE_COUNT.equals(varName)) {
			// Get article count
			DbQuery artCount = new DbQuery(ShopListLArticleModel.ENTITY_NAME, "ART");
			artCount.addCondEq(ShopListLArticle.Var.LIST_ID, "ART", vars.get("T1_id"));
			artCount.addCount(ShopListLArticle.Var.ARTICLE_ID, "ART", "artCount", false);

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
}
