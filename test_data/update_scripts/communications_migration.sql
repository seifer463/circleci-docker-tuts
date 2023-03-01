/*!40101 SET NAMES utf8 */;
/*!40101 SET SQL_MODE=''*/;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ---------------------------------------------------------------------
-- Insert `bbcrm_communication` records using `bbcrm_letters`
-- ---------------------------------------------------------------------
INSERT INTO `bbcrm_communication` (
	`id`,
	`name`,
	`module`,
	`status`,
	`is_notification`,
	`date_entered`,
	`date_modified`,
	`modified_user_id`,
	`created_by`,
	`description`,
	`deleted`,
	`assigned_user_id`
) SELECT
	`id`,
	`document_name`,
	'bbcrm_Letters',
	`letter_status`,
	0,
	`date_entered`,
	`date_modified`,
	`modified_user_id`,
	`created_by`,
	`description`,
	`deleted`,
	`assigned_user_id`
FROM
	`bbcrm_letters`;

-- ---------------------------------------------------------------------
-- Insert `bbcrm_communication_accounts` records using `bbcrm_letters`
-- ---------------------------------------------------------------------
INSERT INTO `bbcrm_communication_accounts` (
	`id`,
	`account_id`,
	`communication_id`,
	`date_modified`,
	`deleted`
) SELECT
	uuid(),
	`account_id`,
	`id`,
	`date_entered`,
	`deleted`
FROM
	`bbcrm_letters`
WHERE
	`account_id` IS NOT NULL
	AND `account_id` != '';

-- ---------------------------------------------------------------------
-- Insert `bbcrm_communication` records using `bbcrm_smsmessages`
-- ---------------------------------------------------------------------
INSERT INTO `bbcrm_communication` (
	`id`,
	`name`,
	`module`,
	`status`,
	`is_notification`,
	`date_entered`,
	`date_modified`,
	`modified_user_id`,
	`created_by`,
	`description`,
	`deleted`,
	`assigned_user_id`
) SELECT
	`id`,
	`name`,
	'bbcrm_SMSMessages',
	`sms_status`,
	0,
	`date_entered`,
	`date_modified`,
	`modified_user_id`,
	`created_by`,
	`description`,
	`deleted`,
	`assigned_user_id`
FROM
	`bbcrm_smsmessages`;

-- ---------------------------------------------------------------------
-- Set the value to 1 if the SMS was created in either of these workflows:
-- 1) (BlueBilling) Account Notifications, Send Notifications
-- 2) (BlueBilling) Collection Workflow, Send Notifications
-- ---------------------------------------------------------------------
UPDATE `bbcrm_communication`
LEFT JOIN `bbcrm_smsrecipients` ON `bbcrm_communication`.`id` = `bbcrm_smsrecipients`.`sms_message_id`
SET `bbcrm_communication`.`is_notification` = 1
WHERE
  `bbcrm_smsrecipients`.`is_notification` = 1
  AND `bbcrm_communication`.`module` = 'bbcrm_SMSMessages';

-- ---------------------------------------------------------------------
-- Insert `bbcrm_communication_accounts` records using `bbcrm_smsmessages`
-- ---------------------------------------------------------------------
INSERT INTO `bbcrm_communication_accounts` (
	`id`,
	`account_id`,
	`communication_id`,
	`date_modified`,
	`deleted`
) SELECT
	uuid(),
	`account_id`,
	`id`,
	`date_entered`,
	`deleted`
FROM
	`bbcrm_smsmessages`
WHERE
	`account_id` IS NOT NULL
	AND `account_id` != '';

-- ---------------------------------------------------------------------
-- Insert `bbcrm_communication` records using `calls`
-- ---------------------------------------------------------------------
INSERT INTO `bbcrm_communication` (
	`id`,
	`name`,
	`module`,
	`status`,
	`is_notification`,
	`date_entered`,
	`date_modified`,
	`modified_user_id`,
	`created_by`,
	`description`,
	`deleted`,
	`assigned_user_id`
) SELECT
	`id`,
	`name`,
	'Calls',
	`status`,
	0,
	`date_entered`,
	`date_modified`,
	`modified_user_id`,
	`created_by`,
	`description`,
	`deleted`,
	`assigned_user_id`
FROM
	`calls`;

-- ---------------------------------------------------------------------
-- Insert `bbcrm_communication_accounts` records using `calls`
-- ---------------------------------------------------------------------
INSERT INTO `bbcrm_communication_accounts` (
	`id`,
	`account_id`,
	`communication_id`,
	`date_modified`,
	`deleted`
) SELECT
	uuid(),
	`parent_id`,
	`id`,
	`date_entered`,
	`deleted`
FROM
	`calls`
WHERE
  `parent_type` = 'Accounts'
	AND `parent_id` IS NOT NULL
	AND `parent_id` != '';

-- ---------------------------------------------------------------------
-- Insert `bbcrm_communication` records using `emails`
-- ---------------------------------------------------------------------
INSERT INTO `bbcrm_communication` (
	`id`,
	`name`,
	`module`,
	`status`,
	`is_notification`,
	`date_entered`,
	`date_modified`,
	`modified_user_id`,
	`created_by`,
	`description`,
	`deleted`,
	`assigned_user_id`
) SELECT
	`id`,
	`name`,
	'Emails',
	`status`,
	0,
	`date_entered`,
	`date_modified`,
	`modified_user_id`,
	`created_by`,
	'' AS `description`,
	`deleted`,
	`assigned_user_id`
FROM
	`emails`;

-- ---------------------------------------------------------------------
-- Set the value to 1 if the email was created in the (BlueBilling) Account Notifications, Send Notifications workflow
-- ---------------------------------------------------------------------
UPDATE `bbcrm_communication`
INNER JOIN `bbcrm_accountnotificationqueue` ON `bbcrm_communication`.`id` = `bbcrm_accountnotificationqueue`.`email_id`
SET `bbcrm_communication`.`is_notification` = 1
WHERE
  `bbcrm_accountnotificationqueue`.`email_id` IS NOT NULL
  AND `bbcrm_accountnotificationqueue`.`email_id` != ''
  AND `bbcrm_communication`.`module` = 'Emails';

-- ---------------------------------------------------------------------
--  Set the value to 1 if the email was created in the (BlueBilling) Collection Workflow, Send Notifications workflow
-- ---------------------------------------------------------------------
UPDATE `bbcrm_communication`
INNER JOIN `bbcrm_notificationsqueue` ON `bbcrm_communication`.`id` = `bbcrm_notificationsqueue`.`email_id`
SET `bbcrm_communication`.`is_notification` = 1
WHERE
	`bbcrm_notificationsqueue`.`email_id` IS NOT NULL
	AND `bbcrm_notificationsqueue`.`email_id` != ''
	AND `bbcrm_communication`.`module` = 'Emails';

-- ---------------------------------------------------------------------
-- Insert `bbcrm_communication_accounts` records using `emails`
-- ---------------------------------------------------------------------
INSERT INTO `bbcrm_communication_accounts` (
	`id`,
	`account_id`,
	`communication_id`,
	`date_modified`,
	`deleted`
) SELECT
	uuid(),
	`parent_id`,
	`id`,
	`date_entered`,
	`deleted`
FROM
	`emails`
WHERE
  `parent_type` = 'Accounts'
	AND `parent_id` IS NOT NULL
	AND `parent_id` != '';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;