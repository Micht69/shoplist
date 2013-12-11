-- phpMyAdmin SQL Dump
-- version 4.0.4
-- http://www.phpmyadmin.net
--
-- Client: 127.0.0.1
-- Généré le: Jeu 05 Décembre 2013 à 17:14
-- Version du serveur: 5.5.32
-- Version de PHP: 5.4.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Base de données: `SHOPLIST`
--
CREATE DATABASE IF NOT EXISTS `SHOPLIST` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `SHOPLIST`;

-- --------------------------------------------------------

--
-- Structure de la table `ARTICLE`
--

CREATE TABLE IF NOT EXISTS `ARTICLE` (
  `ID` int(11) NOT NULL,
  `NAME` varchar(128) NOT NULL,
  `DESCR` varchar(500) DEFAULT NULL,
  `SHELF` varchar(10) DEFAULT NULL,
  `EAN13` varchar(13) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `ARTICLE_SHOP_ARTICLE_R_SHELF` (`SHELF`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Contenu de la table `ARTICLE`
--

-- --------------------------------------------------------

--
-- Structure de la table `LIST`
--

CREATE TABLE IF NOT EXISTS `LIST` (
  `ID` int(11) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `USER` varchar(10) NOT NULL,
  `CREATE_DATE` date DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `LIST_SHOP_LIST_USER_FK` (`USER`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Contenu de la table `LIST`
--

-- --------------------------------------------------------

--
-- Structure de la table `SHELF`
--

CREATE TABLE IF NOT EXISTS `SHELF` (
  `CODE` varchar(10) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `POSITION` int(11) NOT NULL,
  PRIMARY KEY (`CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Contenu de la table `SHELF`
--

INSERT INTO `SHELF` (`CODE`, `NAME`, `POSITION`) VALUES
('CONS', 'Conserves', 40),
('DEJ', 'Petit-déjeuner', 90),
('FRAI', 'Frais', 50),
('LIQ', 'Liquides', 100);

-- --------------------------------------------------------

--
-- Structure de la table `SHOP_LIST_L_ARTICLE`
--

CREATE TABLE IF NOT EXISTS `SHOP_LIST_L_ARTICLE` (
  `LIST_ID` int(11) NOT NULL,
  `ARTICLE_ID` int(11) NOT NULL,
  `QUANTITY` int(11) NOT NULL,
  `STATUS` varchar(5) NOT NULL DEFAULT 'BUY',
  PRIMARY KEY (`LIST_ID`,`ARTICLE_ID`),
  KEY `SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_LIST_FK` (`LIST_ID`),
  KEY `SHOP_LIST_L_ARTICLE_SHOP_LIST_L_ARTICLE_ARTICLE_FK` (`ARTICLE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Contenu de la table `SHOP_LIST_L_ARTICLE`
--

-- --------------------------------------------------------

--
-- Structure de la table `USER`
--

CREATE TABLE IF NOT EXISTS `USER` (
  `LOGIN` varchar(10) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `PASSWORD` varchar(100) DEFAULT NULL,
  `PROFILE` varchar(10) NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`LOGIN`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Contenu de la table `USER`
--

INSERT INTO `USER` (`LOGIN`, `NAME`, `PASSWORD`, `PROFILE`) VALUES
('micht', 'Seb', 'titia1803', 'ADMIN'),
('titia', 'Laeti', 'sebastien', 'USER');

--
-- Contraintes pour les tables exportées
--

--
-- Contraintes pour la table `ARTICLE`
--
ALTER TABLE `ARTICLE`
  ADD CONSTRAINT `SHOP_ARTICLE_L_SHELF` FOREIGN KEY (`SHELF`) REFERENCES `SHELF` (`CODE`) ON UPDATE CASCADE;

--
-- Contraintes pour la table `LIST`
--
ALTER TABLE `LIST`
  ADD CONSTRAINT `SHOP_ARTICLE_L_USER` FOREIGN KEY (`USER`) REFERENCES `USER` (`LOGIN`) ON UPDATE CASCADE;

--
-- Contraintes pour la table `SHOP_LIST_L_ARTICLE`
--
ALTER TABLE `SHOP_LIST_L_ARTICLE`
  ADD CONSTRAINT `SHOP_LIST_L_ARTICLE_L_ARTICLE` FOREIGN KEY (`ARTICLE_ID`) REFERENCES `ARTICLE` (`ID`) ON UPDATE CASCADE,
  ADD CONSTRAINT `SHOP_LIST_L_ARTICLE_L_LIST` FOREIGN KEY (`LIST_ID`) REFERENCES `LIST` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE;
