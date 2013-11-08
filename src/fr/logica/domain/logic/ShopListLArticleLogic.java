package fr.logica.domain.logic;

import java.util.List;

import fr.logica.business.Context;
import fr.logica.business.DefaultLogic;
import fr.logica.business.Key;
import fr.logica.domain.objects.ShopListLArticle;
import fr.logica.ui.ActionPage;

/**
 * Logic class for the entity ShopListLArticle
 * 
 * @author CGI
 */
public class ShopListLArticleLogic extends DefaultLogic<ShopListLArticle> {
	@Override
	public List<Key> doCustomAction(ActionPage<ShopListLArticle> page, List<Key> keys, Context ctx) {
		if (page != null && page.getAction().code == ShopListLArticle.Action.ACTION_50) {
			for (Key k : keys) {
				int listId = Integer.parseInt((String) k.getValue(ShopListLArticle.Var.LIST_ID));
				int articleId = Integer.parseInt((String) k.getValue(ShopListLArticle.Var.ARTICLE_ID));

				ShopListLArticle e = new ShopListLArticle(listId, articleId);
				e.setStatus(ShopListLArticle.ValueList.STATUS.DONE);
				e.persist(ctx);
			}
		}
		return super.doCustomAction(page, keys, ctx);
	}

}
