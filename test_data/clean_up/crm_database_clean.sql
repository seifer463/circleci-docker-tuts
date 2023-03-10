/** CLEAN THE SUGARCRM DATABASE */

SET NAMES utf8;
SET SQL_MODE='';
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0;

/** BlueBilling CRM specific tables */
TRUNCATE bbcrm_accountnotificationqueue;
TRUNCATE bbcrm_billing;
TRUNCATE bbcrm_chargeprofiles_cases;
TRUNCATE bbcrm_chargeprofiles_documents;
TRUNCATE bbcrm_chargeprofiles_notes;
TRUNCATE bbcrm_clientidentifier;
TRUNCATE bbcrm_clientidentifier_audit;
TRUNCATE bbcrm_communication;
TRUNCATE bbcrm_communication_accounts;
TRUNCATE bbcrm_communication_audit;
TRUNCATE bbcrm_communication_online_requests;
TRUNCATE bbcrm_consumptionreadings;
TRUNCATE bbcrm_contacts_notify_settings;
TRUNCATE bbcrm_enginereports;
TRUNCATE bbcrm_enginereports_audit;
TRUNCATE bbcrm_entityimports;
TRUNCATE bbcrm_entityimports_audit;
TRUNCATE bbcrm_gatemeters_cases;
TRUNCATE bbcrm_gatemeters_documents;
TRUNCATE bbcrm_indexedreadings;
TRUNCATE bbcrm_intervalreadings;
TRUNCATE bbcrm_letters;
TRUNCATE bbcrm_letters_audit;
TRUNCATE bbcrm_locationmanager;
TRUNCATE bbcrm_locationmanager_audit;
TRUNCATE bbcrm_meterproviderinstaller;
TRUNCATE bbcrm_meterproviderinstaller_audit;
TRUNCATE bbcrm_meterreadingperiods;
TRUNCATE bbcrm_notificationsqueue;
TRUNCATE bbcrm_notificationsqueue_audit;
TRUNCATE bbcrm_notificationsqueue_online_requests;
TRUNCATE bbcrm_online_request_documents;
TRUNCATE bbcrm_online_request_tasks;
TRUNCATE bbcrm_personalidentification;
TRUNCATE bbcrm_personalidentification_audit;
TRUNCATE bbcrm_servicemodels;
TRUNCATE bbcrm_servicemodels_audit;
TRUNCATE bbcrm_servicepoints_cases;
TRUNCATE bbcrm_servicepoints_documents;
TRUNCATE bbcrm_siteprofiles_bbcrm_sites;
TRUNCATE bbcrm_sites_cases;
TRUNCATE bbcrm_sites_documents;
TRUNCATE bbcrm_smsmessages;
TRUNCATE bbcrm_smsmessages_audit;
TRUNCATE bbcrm_smsrecipients;
TRUNCATE bbcrm_smsrecipients_audit;
TRUNCATE bbcrm_smssignatures;
TRUNCATE bbcrm_smssignatures_audit;
TRUNCATE bbcrm_smstemplates;
TRUNCATE bbcrm_smstemplates_audit;
TRUNCATE bbcrm_spmeterreadingperiods;

/** SugarCRM specific tables */

TRUNCATE accounts;
TRUNCATE accounts_audit;
TRUNCATE accounts_bugs;
TRUNCATE accounts_calls;
TRUNCATE accounts_cases;
TRUNCATE accounts_contacts;
TRUNCATE accounts_opportunities;
TRUNCATE bugs;
TRUNCATE bugs_audit;
TRUNCATE calls;
TRUNCATE calls_contacts;
TRUNCATE calls_leads;
TRUNCATE calls_users;
TRUNCATE cases;
TRUNCATE cases_audit;
TRUNCATE cases_bugs;
TRUNCATE contacts;
TRUNCATE contacts_audit;
TRUNCATE contacts_bugs;
TRUNCATE contacts_cases;
TRUNCATE contacts_users;
TRUNCATE contacts_users;
TRUNCATE comments;
TRUNCATE dashboards;
TRUNCATE document_revisions;
TRUNCATE documents;
TRUNCATE documents_accounts;
TRUNCATE documents_bugs;
TRUNCATE documents_cases;
TRUNCATE documents_contacts;
TRUNCATE documents_opportunities;
TRUNCATE documents_products;
TRUNCATE documents_quotes;
TRUNCATE documents_revenuelineitems;
TRUNCATE email_addr_bean_rel;
TRUNCATE email_addresses;
TRUNCATE email_cache;
TRUNCATE email_marketing;
TRUNCATE email_marketing_prospect_lists;
TRUNCATE email_templates;
TRUNCATE emailman;
TRUNCATE emails;
TRUNCATE emails_beans;
TRUNCATE emails_email_addr_rel;
TRUNCATE emails_text;
TRUNCATE tracker_sessions;
TRUNCATE notes;
TRUNCATE product_types;
TRUNCATE teams;
TRUNCATE team_sets;
TRUNCATE tracker;
TRUNCATE kbtags;
TRUNCATE sugarfavorites;
TRUNCATE notifications_audit;
TRUNCATE report_cache;
TRUNCATE product_categories;
TRUNCATE product_product;
TRUNCATE record_list;
TRUNCATE product_templates;
TRUNCATE filters;
TRUNCATE product_types;
TRUNCATE calls_users;
TRUNCATE currencies;
TRUNCATE taxrates;
TRUNCATE users;
TRUNCATE contacts_cases;
TRUNCATE notes;
TRUNCATE folders; 
TRUNCATE folders_subscriptions;
TRUNCATE fts_queue;
TRUNCATE job_queue;
TRUNCATE notifications;
TRUNCATE pdfmanager;
TRUNCATE acl_roles;
TRUNCATE acl_roles_users;
TRUNCATE subscriptions;
TRUNCATE outbound_email;
TRUNCATE systems;
TRUNCATE team_memberships;
TRUNCATE team_sets_teams;
TRUNCATE upgrade_history;
TRUNCATE user_preferences;
TRUNCATE vcals;
TRUNCATE versions;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_NOTES=@OLD_SQL_NOTES;
