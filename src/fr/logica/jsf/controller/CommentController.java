package fr.logica.jsf.controller;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class CommentController implements Serializable {

	/** serialUID */
	private static final long serialVersionUID = 9220822904210415585L;

	/** Session controller. Holds user relative data and access rights management. */
	private SessionController sessionCtrl;

	/**
	 * Memory comments holder
	 */
	private Hashtable<String, List<Comment>> commentsHolder = new Hashtable<String, List<Comment>>();
	/**
	 * Current comments for entity
	 */
	private List<Comment> comments;
	/**
	 * Comment form
	 */
	private Comment comment = null;

	public boolean isCommentsEnabled() {
		// FIXME : Get property from file
		return true;
	}

	public String commentsCount(String entity, String entityKey) {
		// FIXME : Load comments from db
		comments = commentsHolder.get(entity + "#" + entityKey);
		if (comments == null) {
			comments = new ArrayList<Comment>();
		}

		getComment().entity = entity;
		getComment().entityKey = entityKey;

		int cnt = comments.size();
		if (cnt > 9) {
			return "9+";
		} else {
			return "" + cnt;
		}
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void saveComment() {
		// Complete comment data
		comment.setUser(sessionCtrl.getContext().getUser().getLogin());
		comment.setDate(DateFormat.getDateInstance().format(new Date()));
		comments.add(comment);

		// FIXME : Save in db
		commentsHolder.put(comment.entity + "#" + comment.entityKey, comments);

		// Reset
		comment = null;
	}

	public Comment getComment() {
		if (comment == null) {
			comment = new Comment();
		}
		return comment;
	}

	public void setComment(Comment c) {
		comment = c;
	}

	public class Comment implements Serializable {
		/** */
		private static final long serialVersionUID = -1799463487608270810L;

		private String entity = "";
		private String entityKey = "";
		private String user;
		private String date;
		private String comment;

		public String getEntity() {
			return entity;
		}

		public void setEntity(String entity) {
			this.entity = entity;
		}

		public String getEntityKey() {
			return entityKey;
		}

		public void setEntityKey(String entityKey) {
			this.entityKey = entityKey;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}

	public SessionController getSessionCtrl() {
		return sessionCtrl;
	}

	public void setSessionCtrl(SessionController sessionCtrl) {
		this.sessionCtrl = sessionCtrl;
	}
}
