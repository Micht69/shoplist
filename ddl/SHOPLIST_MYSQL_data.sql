-- Target Database : MYSQL
---------------------
SET FOREIGN_KEY_CHECKS=0;

-- Delete tables content
DELETE FROM ARTICLE;
DELETE FROM LIST;
DELETE FROM SHOP_LIST_L_ARTICLE;
DELETE FROM SHELF;
DELETE FROM USER;


-- Insert new data
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (0, 'Cuisse de Lapin', NULL, 'VIA', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (1, 'Bavette', NULL, 'VIA', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (2, 'Filet de poulet', NULL, 'VIA', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (3, 'Viande de Boeuf hach�e', NULL, 'VIA', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (4, 'Riz', NULL, 'CONS', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (5, 'Potimarron', NULL, 'LEG', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (6, 'Galettes de riz', NULL, 'DEJ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (7, 'Lunettes Petit D�jeuner', NULL, 'DEJ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (8, 'Petit beurre', NULL, 'DEJ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (9, 'Dosettes Voluptuoso', NULL, 'DEJ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (10, 'Flocons d''avoine', NULL, 'DEJ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (11, 'Fromage blanc', NULL, 'FRAI', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (12, 'Yahourt', NULL, 'FRAI', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (13, 'Desserts Seb', NULL, 'FRAI', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (14, 'Fromage rap�', NULL, 'FRAI', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (15, 'Parmesan', NULL, 'FRAI', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (16, 'Banane', NULL, 'LEG', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (17, 'Oignons', NULL, 'LEG', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (18, 'Pain de seigle', NULL, 'DIV', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (19, 'Champignons de Paris', NULL, 'CONS', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (20, 'Pur�e de tomate 400g', NULL, 'CONS', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (21, 'Sirop p�che', NULL, 'LIQ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (22, 'Sirop grenadine', NULL, 'LIQ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (23, 'Sirop stevia', NULL, 'LIQ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (24, 'Vinaigre de vin', NULL, 'CONS', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (25, 'Stevia', NULL, 'DEJ', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (26, 'Cachou', NULL, 'CONF', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (27, 'Carottes (kg)', NULL, 'LEG', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (28, 'Orange', NULL, 'LEG', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (29, 'Huile', NULL, 'CONS', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (30, 'Jambon', NULL, 'FRAI', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (31, 'Emmental', NULL, 'FRAI', NULL);
INSERT INTO ARTICLE (ID, NAME, DESCR, SHELF, EAN13) 
  VALUES (32, 'Fromage raclette merzer', NULL, 'FRAI', NULL);
INSERT INTO LIST (ID, NAME, `USER`, CREATE_DATE) 
  VALUES (0, 'Carrefour', 'micht', '2013-12-09');
INSERT INTO LIST (ID, NAME, `USER`, CREATE_DATE) 
  VALUES (1, 'Avenel', 'micht', '2013-12-09');
INSERT INTO LIST (ID, NAME, `USER`, CREATE_DATE) 
  VALUES (3, 'Satoriz', 'micht', '2014-01-13');
INSERT INTO LIST (ID, NAME, `USER`, CREATE_DATE) 
  VALUES (4, 'Courses WE', 'micht', '2014-03-18');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (0, 4, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (0, 19, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (0, 20, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (0, 24, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (0, 26, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (0, 29, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (1, 4, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (1, 19, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (1, 20, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (1, 26, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (1, 29, 1, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (4, 30, 8, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (4, 31, 8, 'BUY');
INSERT INTO SHOP_LIST_L_ARTICLE (LIST_ID, ARTICLE_ID, QUANTITY, STATUS) 
  VALUES (4, 32, 1, 'BUY');
INSERT INTO SHELF (CODE, NAME, `POSITION`) 
  VALUES ('CONF', 'Confiserie', 60);
INSERT INTO SHELF (CODE, NAME, `POSITION`) 
  VALUES ('CONS', 'Conserves', 40);
INSERT INTO SHELF (CODE, NAME, `POSITION`) 
  VALUES ('DEJ', 'Petit-D�jeuner', 50);
INSERT INTO SHELF (CODE, NAME, `POSITION`) 
  VALUES ('DIV', 'Divers', 999);
INSERT INTO SHELF (CODE, NAME, `POSITION`) 
  VALUES ('FRAI', 'Frais', 80);
INSERT INTO SHELF (CODE, NAME, `POSITION`) 
  VALUES ('LEG', 'L�gumes', 70);
INSERT INTO SHELF (CODE, NAME, `POSITION`) 
  VALUES ('LIQ', 'Liquides', 10);
INSERT INTO SHELF (CODE, NAME, `POSITION`) 
  VALUES ('VIA', 'Viande', 200);
INSERT INTO USER (LOGIN, NAME, `PASSWORD`, PROFILE) 
  VALUES ('micht', 'Seb', 'titia1803', 'ADMIN');
INSERT INTO USER (LOGIN, NAME, `PASSWORD`, PROFILE) 
  VALUES ('titia', 'Laeti', 'sebastien', 'ADMIN');

SET FOREIGN_KEY_CHECKS=1;

