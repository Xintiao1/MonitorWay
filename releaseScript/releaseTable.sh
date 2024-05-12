#!/bin/sh

CUR_DIR=`pwd`
source $CUR_DIR/release.properties
rm -f syncTable.sql
SYNC_TABLE="mw_db_version mw_module mw_module_perm_mapper mw_pagefield_table mw_pageselect_table mw_notcheck_url"
SYNC_TABLE="$SYNC_TABLE mw_alert_action_type mw_layout_base mw_label_module_base mw_tag_action_table "
SYNC_TABLE="$SYNC_TABLE mw_report_type_table mw_mac_oui mw_base_monitor_component"
SYNC_TABLE="$SYNC_TABLE mw_select_url_base mw_macros_name_mapper"
SYNC_TABLE="$SYNC_TABLE MW_PROMETHEUS_QUERY_CONFIG MW_PROMETHEUS_PROPERTY"

mysqldump -u$DEV_DB_USER -p$DEV_DB_PASSWD -h$DEV_DB_IP --databases monitor --tables $SYNC_TABLE > syncTable.sql;