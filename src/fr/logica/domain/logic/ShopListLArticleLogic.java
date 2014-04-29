package fr.logica.domain.logic;

import java.util.List;

import fr.logica.business.Action;
import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Constants;
import fr.logica.business.DefaultLogic;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.Request;
import fr.logica.business.controller.Response;
import fr.logica.domain.objects.ShopArticle;
import fr.logica.domain.objects.ShopListLArticle;
import fr.logica.queries.ShopArticleQuery;

/**
 * Logic class for the entity ShopListLArticle
 * 
 * @author CGI
 */
public class ShopListLArticleLogic extends DefaultLogic<ShopListLArticle> {

	@Override
	public String doDescription(ShopListLArticle bean, RequestContext ctx) {
		if (bean == null)
			return "";

		StringBuilder desc = new StringBuilder();
		desc.append(bean.getQuantity());
		desc.append(" ");
		desc.append(bean.getRef_ShopListLArticleArticleFk(ctx).getName());
		desc.append(" ");
		desc.append(bean.getStatus());

		return desc.toString();
	}

	@Override
	public List<Key> doCustomAction(Request<ShopListLArticle> request, ShopListLArticle entity, List<Key> keys, RequestContext ctx) {
		if (request.getAction().getCode() == ShopListLArticle.Action.ACTION_60) {
			// Mark as buyed
			for (Key k : keys) {
				int listId = Integer.parseInt((String) k.getValue(ShopListLArticle.Var.LIST_ID));
				int articleId = Integer.parseInt((String) k.getValue(ShopListLArticle.Var.ARTICLE_ID));

				ShopListLArticle e = new ShopListLArticle(listId, articleId);
				e.setStatus(ShopListLArticle.ValueList.STATUS.DONE);
				e.persist(ctx);
			}
		} else if (request.getAction().getCode() == ShopListLArticle.Action.ACTION_20) {
			// Custom delete
			for (Key k : keys) {
				ShopListLArticle e = new ShopListLArticle();
				e.setPrimaryKey(k);
				if (e.find(ctx)) {
					e.remove(ctx);
				}
			}
		}
		return super.doCustomAction(request, entity, keys, ctx);
	}

	@Override
	public void uiActionOnLoad(Response<ShopListLArticle> response, RequestContext ctx) {
		if (response.getAction().getCode() == ShopListLArticle.Action.ACTION_20) {
			List<Key> keys = response.getKeys();
			String entityToRemove = "Les lignes d'article suivantes seront supprimées :<br>";
			entityToRemove += "<ul style=\"list-style: initial; padding-left: 40px;\">";
			for (Key k : keys) {
				ShopListLArticle e = new ShopListLArticle();
				e.setPrimaryKey(k);
				if (e.find(ctx)) {
					entityToRemove += "<li>";
					entityToRemove += doDescription(e, ctx);
					entityToRemove += "</li>";
				}
			}
			entityToRemove += "</ul>";
			response.getEntity().setDeleteInfos(entityToRemove);
		}
		super.uiActionOnLoad(response, ctx);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response uiCtrlOverrideAction(Response response, RequestContext ctx) {
		if (response.getAction().is(ShopListLArticle.Action.ACTION_70)) {

			Response r = new Response<ShopArticle>();
			String queryName = ShopArticleQuery.Query.QUERY_SHOP_ARTICLE;

			Action customAttachmentAction = new Action(Constants.SELECT, queryName, null, null, Input.QUERY,
					Persistence.UPDATE, UserInterface.INPUT,
					fr.logica.business.Action.Process.CUSTOM);

			r.setAction(customAttachmentAction);
			r.setEntityName(ShopArticle.NAME);
			r.setLinkedEntity(response.getLinkedEntity());
			r.setLinkName(response.getLinkName());
			r.setQueryName(queryName);

			return r;
		}

		return super.uiCtrlOverrideAction(response, ctx);
	}
}
