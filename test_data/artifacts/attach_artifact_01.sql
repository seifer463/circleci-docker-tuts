/*
SQLyog Ultimate v11.33 (64 bit)
MySQL - 5.5.39 : Database - bluebilling_selenium
*********************************************************************
*/
 SET NAMES utf8 ;
 SET SQL_MODE='';
 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 ;
 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 ;
 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' ;
 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 ;

 /* Let's get the Account ID of 200010004998 (Bonds) to update in the bbeng_bills_generation table */
 SET @acc_id1 = NULL;
 SELECT id INTO @acc_id1 FROM bbeng_account_details WHERE `account_number`='200010004998';
 
  /* Let's get the Account ID of 200010004899 (Lonsdale) to update in the bbeng_bills_generation table */
 SET @acc_id2 = NULL;
 SELECT id INTO @acc_id2 FROM bbeng_account_details WHERE `account_number`='200010004899';
 

/* Update a specific record in the bbeng_bills_generation */

UPDATE `bbeng_bills_generation` SET `snp_last_stage`='POSTED', bill_delivery ='POST_ONLY' WHERE (`account_id`=@acc_id1);

UPDATE `bbeng_bills_generation` SET `snp_last_stage`='TO_BE_BILLED' WHERE (`account_id`=@acc_id2);


 SET SQL_MODE=@OLD_SQL_MODE ;
 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS ;
 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS ;
 SET SQL_NOTES=@OLD_SQL_NOTES ;