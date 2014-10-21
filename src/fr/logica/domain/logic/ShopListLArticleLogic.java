package fr.logica.domain.logic;

import java.util.ArrayList;
import java.util.List;

import fr.logica.application.logic.User;
import fr.logica.business.Action;
import fr.logica.business.Action.Input;
import fr.logica.business.Action.Persistence;
import fr.logica.business.Action.UserInterface;
import fr.logica.business.Constants;
import fr.logica.business.DefaultLogic;
import fr.logica.business.EntityField;
import fr.logica.business.Key;
import fr.logica.business.MessageUtils;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.Request;
import fr.logica.business.controller.Response;
import fr.logica.db.DB;
import fr.logica.db.DbManager;
import fr.logica.db.DbQuery;
import fr.logica.domain.constants.ShopArticleConstants;
import fr.logica.domain.constants.ShopListConstants;
import fr.logica.domain.constants.ShopListLArticleConstants;
import fr.logica.domain.objects.ShopArticle;
import fr.logica.domain.objects.ShopList;
import fr.logica.domain.objects.ShopListLArticle;
import fr.logica.queries.ShopArticleQuery;

/**
 * Logic class for the entity ShopListLArticle
 * 
 * @author CGI
 */
public class ShopListLArticleLogic extends DefaultLogic<ShopListLArticle> implements ShopListLArticleConstants {

	@Override
	public String doDescription(ShopListLArticle bean, RequestContext ctx) {
		if (bean == null)
			return "";
		EntityField field = bean.getModel().getField(ShopListLArticleConstants.Vars.STATUS);

		StringBuilder desc = new StringBuilder();
		desc.append(bean.getQuantity());
		desc.append(" ");
		desc.append(bean.getRef_ShopListLArticleArticleFk(ctx).getName());
		desc.append("  :  ");
		desc.append(field.getDefinedLabel(bean.getStatus(), null));

		return desc.toString();
	}
	
	@Override
	public List<Key> doCustomAction(Request<ShopListLArticle> request, ShopListLArticle entity, RequestContext ctx) {
		if (request.getAction().getCode() == ShopListLArticleConstants.Actions.ACTION_60) {
			// Mark as buyed
			markAsBuyed(entity, ctx);
		} else if (request.getAction().getCode() == ShopListLArticleConstants.Actions.ACTION_71) {
			// Get by EAN13
			ShopListLArticle lienListeArticle = request.getEntity();
			ShopArticle article = null;
			
			// Find Article
			DbQuery query = DB.createQuery(ctx, ShopArticleConstants.ENTITY_NAME, "ART");
			query.addCondEq(ShopArticleConstants.Vars.EAN13, "ART", lienListeArticle.getItemEan13());
			
			DbManager dbm = new DbManager(ctx, query);
			if (dbm.next()){
				// Found
				article = dbm.getEntity("ART", new ShopArticle(), ctx);
			} else {
				// TODO Create empty article ?
				ctx.getMessages().add(MessageUtils.addStringErrorMessage("Article non trouvé."));
			}
			
			if (article != null) {
				User user = ctx.getSessionContext().getUser();
				// Persit article line
				lienListeArticle.setListId(user.listId);
				lienListeArticle.setArticleId(article.getId());
				lienListeArticle.setQuantity(1);
				lienListeArticle.setStatus(ShopListLArticleConstants.ValueList.STATUS.BUY);
				// TODO : Check if line exists ...
				DB.persist(lienListeArticle, ctx);
			}
		}
		return super.doCustomAction(request, entity, ctx);
	}

	@Override
	public List<Key> doCustomAction(Request<ShopListLArticle> request, ShopListLArticle entity, List<Key> keys, RequestContext ctx) {
		if (request.getAction().getCode() == ShopListLArticleConstants.Actions.ACTION_60) {
			// Mark as buyed
			for (Key k : keys) {
				ShopListLArticle e = DB.get(ShopListLArticleConstants.ENTITY_NAME, k, ctx);
				markAsBuyed(e, ctx);
			}
		} else if (request.getAction().getCode() == ShopListLArticleConstants.Actions.ACTION_20) {
			// Custom delete
			for (Key k : keys) {
				ShopListLArticle e = DB.get(ShopListLArticleConstants.ENTITY_NAME, k, ctx);
				if (e != null) {
					DB.remove(e, ctx);
				}
			}
		}
		return super.doCustomAction(request, entity, keys, ctx);
	}
	
	private void markAsBuyed(ShopListLArticle e, RequestContext ctx) {
		e.setStatus(ShopListLArticleConstants.ValueList.STATUS.DONE);
		DB.persist(e, ctx);
	}

	@Override
	public void uiActionOnLoad(Response<ShopListLArticle> response, RequestContext ctx) {
		if (response.getAction().getCode() == ShopListLArticleConstants.Actions.ACTION_20) {
			List<Key> keys = response.getKeys();
			String entityToRemove = "Les lignes d'article suivantes seront supprimées :<br>";
			entityToRemove += "<ul style=\"list-style: initial; padding-left: 40px;\">";
			for (Key k : keys) {
				ShopListLArticle e = DB.get(ShopListLArticleConstants.ENTITY_NAME, k, ctx);
				if (e != null) {
					entityToRemove += "<li>";
					entityToRemove += doDescription(e, ctx);
					entityToRemove += "</li>";
				}
			}
			entityToRemove += "</ul>";
			response.getEntity().setDeleteInfos(entityToRemove);
		} else if (response.getAction().getCode() == ShopListLArticleConstants.Actions.ACTION_71) {
			User user = ctx.getSessionContext().getUser();
			if (user.eanCode != null) {
				// Scan return
				response.getEntity().setItemEan13(user.eanCode);
				user.eanCode = null;
			} else if (response.getLinkedEntity() != null) {
				// First display
				user.listId = ((ShopList)response.getLinkedEntity()).getId();
			}
		}
		super.uiActionOnLoad(response, ctx);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Response uiCtrlOverrideAction(Response response, RequestContext ctx) {
		if (response.getAction().is(ShopListLArticleConstants.Actions.ACTION_70)) {

			Response r = new Response<ShopArticle>();
			String queryName = ShopArticleQuery.Query.QUERY_SHOP_ARTICLE;

			Action customAttachmentAction = new Action(Constants.SELECT, queryName, null, null, Input.QUERY,
					Persistence.UPDATE, UserInterface.INPUT,
					fr.logica.business.Action.Process.CUSTOM);

			r.setAction(customAttachmentAction);
			r.setEntityName(ShopArticleConstants.ENTITY_NAME);
			r.setLinkedEntity(response.getLinkedEntity());
			r.setLinkName(response.getLinkName());
			r.setQueryName(queryName);

			return r;
		}

		return super.uiCtrlOverrideAction(response, ctx);
	}
	
	@Override
	public Request<?> uiCtrlNextAction(Request<ShopListLArticle> request, RequestContext ctx) {
		if (request.getAction().getCode() == ShopListLArticleConstants.Actions.ACTION_71) {
			User user = ctx.getSessionContext().getUser();
			Key listKey = ShopList.buildPrimaryKey(user.listId);
			ShopList list = DB.get(ShopListConstants.ENTITY_NAME, listKey, ctx);
			
			Request<ShopList> req = new Request<ShopList>();
			req.setContext(ctx);
			req.setEntityName(ShopListConstants.ENTITY_NAME);
			req.setEntity(list);
			req.setAction(list.getModel().getAction(ShopListConstants.Actions.ACTION_2));
			List<Key> keysList = new ArrayList<Key>(1);
			keysList.add(listKey);
			req.setKeys(keysList);
			
			return req;
		}
		return super.uiCtrlNextAction(request, ctx);
	}
}
