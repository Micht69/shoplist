-- Target Database : MYSQL

-- Tables
CREATE TABLE ARTICLE (
	ID integer NOT NULL COMMENT 'ID',
	NAME varchar(128) NOT NULL COMMENT 'Nom',
	DESCR TEXT NULL COMMENT 'Description',
	SHELF varchar(10) NOT NULL COMMENT 'Rayon',
	EAN13 varchar(13) NULL COMMENT 'Ean13'
) ENGINE = InnoDB;
ALTER TABLE ARTICLE COMMENT='Article';
ALTER TABLE ARTICLE ADD CONSTRAINT ARTICLE_SHOP_ARTICLE_PK PRIMARY KEY (ID);
CREATE INDEX ARTICLE_SHOP_ARTICLE_R_SHELF ON ARTICLE (SHELF ASC) ;
CREATE TABLE LIST (
	ID integer NOT NULL COMMENT 'ID',
	NAME varchar(100) NOT NULL COMMENT 'Titre',
	`USER` varchar(10) NOT NULL COMMENT 'Cr�ateur',
	CREATE_DATE date NULL COMMENT 'Date de cr�ation'
) ENGINE = InnoDB;
ALTER TABLE LIST COMMENT='Liste de courses';
ALTER TABLE LIST ADD CONSTRAINT LIST_SHOP_LIST_PK PRIMARY KEY (ID);
CREATE INDEX LIST_SHOP_LIST_USER_FK ON LIST (`USER` ASC) ;
CREATE TABLE SHOP_LIST_L_ARTICLE (
	LIST_ID integer NOT NULL COMMENT 'Liste',
	ARTICLE_ID integer NOT NULL COMMENT 'Article',
	QUANTITY integer NOT NULL COMMENT 'Quantit�',
	STATUS varchar(5) DEFAULT 'BUY' NOT NULL COMMENT 'Statut'
) ENGINE = InnoDB;
ALTER TABLE SHOP_LIST_L_ARTICLE COMMENT='Ligne d''article';
ALTER TABLE SHOP_LIST_L_ARTICLE ADD CONSTRAINT SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_PK PRIMARY KEY (LIST_ID,ARTICLE_ID);
CREATE INDEX SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_LIST_FK ON SHOP_LIST_L_ARTICLE (LIST_ID ASC) ;
CREATE INDEX SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_ARTICLE_FK ON SHOP_LIST_L_ARTICLE (ARTICLE_ID ASC) ;
CREATE TABLE SHELF (
	CODE varchar(10) NOT NULL COMMENT 'Code',
	NAME varchar(100) NOT NULL COMMENT 'Nom',
	`POSITION` integer NOT NULL COMMENT 'Position'
) ENGINE = InnoDB;
ALTER TABLE SHELF COMMENT='Rayon';
ALTER TABLE SHELF ADD CONSTRAINT SHELF_SHOP_SHELF_PK PRIMARY KEY (CODE);
CREATE TABLE USER (
	LOGIN varchar(10) NOT NULL COMMENT 'Login',
	NAME varchar(100) NOT NULL COMMENT 'Nom',
	`PASSWORD` varchar(100) NULL COMMENT 'Mot de passe',
	PROFILE varchar(10) DEFAULT 'USER' NOT NULL COMMENT 'Profil'
) ENGINE = InnoDB;
ALTER TABLE USER COMMENT='Utilisateur';
ALTER TABLE USER ADD CONSTRAINT USER_SHOP_USER_PK PRIMARY KEY (LOGIN);

-- Foreign key constraints 
ALTER TABLE ARTICLE ADD CONSTRAINT SHOP_ARTICLE_L_SHELF FOREIGN KEY (SHELF) REFERENCES SHELF (CODE) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE LIST ADD CONSTRAINT SHOP_ARTICLE_L_USER FOREIGN KEY (`USER`) REFERENCES USER (LOGIN) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE SHOP_LIST_L_ARTICLE ADD CONSTRAINT SHOP_LIST_L_ARTICLE_L_LIST FOREIGN KEY (LIST_ID) REFERENCES LIST (ID) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE SHOP_LIST_L_ARTICLE ADD CONSTRAINT SHOP_LIST_L_ARTICLE_L_ARTICLE FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE (ID) ON DELETE RESTRICT ON UPDATE CASCADE;

