package fr.logica.domain.logic;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.parser.DateParser;
import org.apache.xmlrpc.parser.TypeParser;
import org.apache.xmlrpc.serializer.DateSerializer;
import org.apache.xmlrpc.serializer.TypeSerializer;
import org.xml.sax.SAXException;

import fr.logica.application.logic.User;
import fr.logica.business.Action;
import fr.logica.business.Constants;
import fr.logica.business.DefaultLogic;
import fr.logica.business.Key;
import fr.logica.business.context.RequestContext;
import fr.logica.business.controller.Request;
import fr.logica.domain.models.ShopShelfModel;
import fr.logica.domain.objects.ShopArticle;
import fr.logica.domain.objects.ShopList;
import fr.logica.domain.objects.ShopListLArticle;
import fr.logica.queries.ShopArticleQuery;
import fr.logica.ui.Message;

/**
 * Logic class for the entity ShopArticle
 * 
 * @author CGI
 */
public class ShopArticleLogic extends DefaultLogic<ShopArticle> {
	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void dbOnSave(ShopArticle bean, Action action, RequestContext ctx) {
		if (action.getCode() == ShopArticle.Action.ACTION_50) {
			// Import from EAN database
			logger.debug("EAN import.");
			searchUPCdatabase(bean, ctx);
		}
		super.dbOnSave(bean, action, ctx);
	}

	@Override
	public void dbPostLoad(ShopArticle bean, Action action, RequestContext ctx) {
		if (action.getCode() == ShopArticle.Action.ACTION_50) {
			User user = ctx.getSessionContext().getUser();
			if (user.eanCode != null) {
				bean.setEan13(user.eanCode);
				user.eanCode = null;
			}
			if (user.eanShelf != null) {
				bean.setForeignKey("shopArticleRShelf", new Key(ShopShelfModel.ENTITY_NAME, user.eanShelf));
				user.eanShelf = null;
			}
		}
		super.dbPostLoad(bean, action, ctx);
	}

	@Override
	public List<Key> doCustomAction(Request<ShopArticle> request, ShopArticle entity, List<Key> keys, RequestContext ctx) {
		if (request.getAction().is(Constants.SELECT) && ShopArticleQuery.Query.QUERY_SHOP_ARTICLE.equals(request.getQueryName())) {
			ShopList liste = (ShopList) request.getLinkedEntity();
			for (Key articlePk : keys) {
				ShopListLArticle lienListeArticle = new ShopListLArticle();
				lienListeArticle.setListId(liste.getId());
				lienListeArticle.setArticleId((Integer) articlePk.getValue(ShopArticle.Var.ID));
				lienListeArticle.setQuantity(1);
				lienListeArticle.setStatus(ShopListLArticle.ValueList.STATUS.BUY);
				lienListeArticle.persist(ctx);
			}
		}
		return super.doCustomAction(request, entity, keys, ctx);
	}

	private void searchUPCdatabase(ShopArticle bean, RequestContext ctx) {
		String ean = bean.getEan13();
		if (ean == null) {
			ctx.getMessages().add(new Message("Veuillez entrer un code EAN.", Message.Severity.ERROR));
			return;
		}

		String key = "";
		// Validate EAN
		switch (ean.length()) {
//		case 8:
//			ean = "00000" + ean;
		case 12:
			key = "upc";
			break;
		case 13:
			key = "ean";
			// Valid length
			if (!ean.matches("^[0-9]*$")) {
				ctx.getMessages().add(new Message("Le code ne doit contenir que des chiffres.", Message.Severity.ERROR));
				return;
			}

			int total = 0;
			for (int i = 0; i < (ean.length() - 1); i++) {
				int c = Integer.parseInt(ean.substring(i, i + 1));
				total += c * (1 + (i % 2) * 2);
			}
			total = (10 - (total % 10)) % 10;

			int c = Integer.parseInt(ean.substring(ean.length() - 1));
			if (total != c) {
				ctx.getMessages().add(new Message("Le code est invalide.", Message.Severity.ERROR));
				return;
			}
			break;
		default:
			ctx.getMessages().add(new Message("Longueur invalide.", Message.Severity.ERROR));
			return;
		}

		try {
			// System.setProperty("http.proxyHost", "fr-proxy.groupinfra.com");
			// System.setProperty("http.proxyPort", "3128");

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL("http://www.upcdatabase.com/xmlrpc"));
			final XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
			client.setTypeFactory(new MyTypeFactory(client));
//			final XmlRpcTransportFactory transportFactory = new XmlRpcTransportFactory() {
//				public XmlRpcTransport getTransport() {
//					return new MessageLoggingTransport(client);
//				}
//			};
//			client.setTransportFactory(transportFactory);

			Map<String, String> params = new HashMap<String, String>();
			params.put("rpc_key", "3a9a2dd5f3334db7af7532b5ae4ed570dc5399a9");
			params.put(key, ean);
			HashMap<?, ?> result = (HashMap<?, ?>) client.execute("lookup", new Object[] { params });
			logger.debug(result);

			String status = (String) result.get("status");
			if (status != null && "success".equalsIgnoreCase(status)) {
				// Found
				bean.setName((String) result.get("description"));
				bean.setDescr((String) result.get("size"));
			} else {
				ctx.getMessages().add(new Message("Le code est introuvable.", Message.Severity.ERROR));
			}

			return;
		} catch (Exception e) {
			ctx.getMessages().add(new Message(e.getMessage(), Message.Severity.ERROR));
			logger.error(e);
		}
		return;
	}

	class MyTypeFactory extends TypeFactoryImpl {
		public MyTypeFactory(XmlRpcController pController) {
			super(pController);
		}

		private DateFormat newFormat() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		}

		public TypeParser getParser(XmlRpcStreamConfig pConfig, NamespaceContextImpl pContext, String pURI, String pLocalName) {
			if (DateSerializer.DATE_TAG.equals(pLocalName)) {
				return new DateParser(newFormat());
			} else {
				return super.getParser(pConfig, pContext, pURI, pLocalName);
			}
		}

		public TypeSerializer getSerializer(XmlRpcStreamConfig pConfig, Object pObject) throws SAXException {
			if (pObject instanceof Date) {
				return new DateSerializer(newFormat());
			} else {
				return super.getSerializer(pConfig, pObject);
			}
		}
	}
}
