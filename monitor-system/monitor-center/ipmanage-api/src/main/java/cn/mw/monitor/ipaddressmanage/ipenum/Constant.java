package cn.mw.monitor.ipaddressmanage.ipenum;


import java.util.Arrays;
import java.util.List;

/**
 * @author lumingming
 * @createTime 2023213 14:04
 * @description 13
 */
public class Constant {
    public static final  String  IPV4_JUDGE_CATIPAL= "IPv4";
    public static final String  IPV4_JUDGE= "IPv4";
    public static final String  IPV6_JUDGE = "IPv6";
    public static final String  TIME_STATUS = "yyyy-MM-dd";
    public static final  String  TRUE_JUDGE = "true";
    public static final  String  FALSE_JUDGE = "false";
    public static final String  IP_ID= "id";
    public static  final String  IP_LIST= "ips";
    public static final Integer GET_FRIST_NODE = 0;
    public static final Integer SIZE_LENGTH = 0;
    public static final Integer INT_NUMBER = 0;
    public static final Integer INT_STAUS_YES = 0;
    public static final String  IP_SIGN= "ip";
    public static final Integer INT_STAUS_NOT = 1;




    public static final String  INPUT_TYPE_ONE = "1";
    public static final String  INPUT_TYPE_TWO = "2";
    public static final  String  INPUT_TYPE_THERE = "3";

    public static final  String  IP_GROUP_ID = "ipgroup_id";
    public static final  String  BANG_DISTRI = "bang_distri";

    /*IP地址字段*/
    public static final  String  IP_GROUPING = "grouping";

    public static final  String  TREE_STRUCTURES = "treeStructures";

    public static final  String  DISTRIBUTION_STATUS = "distribution_status";

    /*ip橄榄修改*/
    public static final  String  IP_GROUP = "ipGroup";
    public static final  String  IP_ADDRESS = "ipAddress";
    public static final  String  IP_ADDRESSES = "ipAddresses";
    public static final  String  DISTRIBUTTIONER = "distributtioner";
    public static final  String  APPLICANT_DATE_START = "applicantDateStart";
    public static final  String  APPLICANT_DATE_END = "applicantDateEnd";
    public static final  String  OPER_TIME_START = "operTimeStart";
    public static final  String  OPER_TIME_END = "operTimeEnd";
    public static final  String  OPER_INT = "operInt";
    public static final  String  APPLICANTOR="申请人";
    public static final  String  APPLICANT_NAME="流程名称";
    public static final  String  APPLICANT_PROCES="applicantProces";
    public static final  String  APPLICANT="applicant";
    public static final  String  APPLICANTOR_ENGLISH="applicator";
    public static final  String  APPLICANT_URL="applicaturl";
    public static final  String START = "start";
    public static final  String END = "end";
    public static final  String  IPADDRESS_RELATION="ipaddress_relation";
    public static final  String  APPLICANT_DATE="applicantDate";

    public static final  String  OPER_TIME = "operTime";

    public static final  String  TEMPORARY_IP = "tiPaddresses";
    public static final  String  TEMPORARY_GROUP = "tgrouping";
    /*区分key*/
    public static final  String  DISTINGUISH_IPV4= "ipv4-";
    public static final  String  DISTINGUISH_IPV6= "ipv6-";
    public static final  String  DISTINGUISH_GROUP= "group_id";
    /*MAP常量*/
    public static final  String  MAP_VALUE="vaiue";
    public static final  String  MAP_KEY_VALUE="map_key_value";
    public static final  String  MAP_OPER_INT="oper_int";
    public static final  String  MAP_OPER_TIME = "oper_time";
    public static final  String  MAP_APPLICANT_DATE = "applicant_time";
    public static final  String  MAP_APPLICANTOR_ENGLISH = "oper_user";
    public static final  String  BAND_IP_TYPE = "bandIpType";
    public static final  String  BAND_IP_ID = "bandIp";
    public static final  String  DESCRIPTION = "desc";
    public static final  String  MAP_PARENT_ID = "parent_id";
    public static final  String  IP_TYPE = "ip_type";
    public static final  String  LIST_ID = "list_id";
    public static final  String  IP_ADDRESS_MAP = "ip_address";
    public static final  String  MAP_IP_LIST_ID= "iplist_id";
    public static final  String  MAP_IP_LIST_TYPE= "iplist_type";
    public static final  String  MAP_TYPE= "type";
    public static final  String  MAP_KEY_NAME= "keyName";
    public static final  String  MAP_IP_DISCRIPTION= "ip_discription";
    public static final  String  MAP_IP_MAC= "mac";
    public static final  String  MAP_IP_VENDOR= "vendor";
    public static final  String  MAP_IP_ACCESS_EPUIP= "access_equip";
    public static final  String  MAP_IP_ACCESS_PORT= "access_port";
    public static final  String  MAP_IP_ASSETS_NAME= "assets_name";
    public static final  String  MAP_IP_ASSETS_TYPE= "assets_type";
    public static final  String  REMARKS= "remarks";
    public static final  String  MAP_DIS_OLD_IPADDRESS= "dis_old_ipaddress";
    /*查询类常量*/
    public static final  String  ORDER_NAME="orderName";
    public static final  String  ORDER_TYPE="orderType";
    public static final  String  PAGE_NUMBER="pageNumber";
    public static final  String  PAGE_SIZE="pageSize";

    public static final  String  DIS_IPADDRESS="dis_ipaddress";
    public static final  String  IP_STATUS="ip_status";
    public static final  String  DESCRIPT="descri";

    public static final  String  IP_IDS="ipIds";
    public static final  String  RADIO_BUTTON="radio";


    /*提示语*/
    public static final  String  UN_WRITE_TXET="未填写";
    public static final  String  REMOVE_TEXT="已删除";
    public static final  String  UN_DISTRI_IP="此地址段无法分配";
    public static final  String  SCREACH_TOO_IP="id查询过多，只存在单查询";
    public static final  String  HAVE_REPEAT_IP="存在重复地址分配";
    public static final  String  SUCCESSFUL="成功";
    public static final  String  UNFIND_USEFUL_IP="未找到可分配地址";
    public static final  String  DESCRIPTION_TEXT="备注";
    public static final  String  UN_DISTRI_IP_LIST="当前分配组存在无法分配地址段：";
    public static final  String  TEMPORARY_IP_LIST="临时地址段：";
    public static final  String  SUCCESSFUL_IMPORT="已导入";
    public static final  String  HAVE_PROBLRM_LINE="行数内有问题";
    public static final  String  MUST_REMOVR_IP="当前IP已经被删除！请回收";
    public static final  String  INTRANT="内网";
    public static final  String  UN_SUCCESS_FUL="存在IP导入未导入，IP不存在或者已分配";
    public static final  String  FALIE_FUL="存在未知导入失败，请后台日志";
    public static final  String  FALIE_LOG_FUL="错误返回 :{}";
    public static final  String  FALIE_NUMBER_FUL="数据存在问题";
    /*label不同像*/

    public static  Integer Level_BASCIS = 6;
    public static  Integer Level_SENIOR= 7;

    /*tree位置错拽标识*/
    public static final  String  INNER_JUDGE="inner";
    public static final  String  INNER_BEFORE="before";
    public static final  String  INNER_AFTER="after";
    public static final  String  CHILDREN="childrens";
    public static final  String  LIST_TREE="listTree";

    /*标签字段*/
    public static final  String  LABEL_LIST="labelList";
    public static final  String  LABEL_IS_REQUIRE="isRequired";
    public static final  String  LABEL="label";

    /*树形数组*/
    public static final  String  PRIMARY_IP = "primary_ip";
    public static final  String  IP_TYPE_ID = "idType";
    public static final  String  PARENT_ID = "parenId";
    public static final  String  SOURCR_ID="sourceId";
    public static final  String  PARENT_IP_TYPE = "parenIdType";
    public static final  String  PAREN_IP = "parentId";
    public static final  String  SOURCE_IP_TYPE = "sourceIdType";
    public static final  String  INDEX_SORT = "index_sort";
    public static final  String  INDEX_SORT_IP = "indexSort";
    public static final  String  FRIDST_NAME = "firstName";
    public static final  String  PRIMARY_IP_TYPE = "primary_type";
    public static final  String  IP_GROUP_TYPR = "ipgroup_type";
    public static final  String  CLEAN_DISTRIBUTTION = "cleanDistributtion";


    /*其他*/
    public static final  String  MW_IPADDRESSMANAGELIST_TABLE = "mw_ipaddressmanagelist_table";
    public static final  String  MW_IPV6MANAGELIST_TABLE = "mw_ipaddressmanagelist_table";
    public static final  String  MW_IPADDRESSMANAGELIST_TABLE_ORACLE = "\"mw_ipaddressmanagelist_table\"";
    public static final  String  MW_IPV6MANAGELIST_TABLE_ORACLE = "\"mw_ipaddressmanagelist_table\"";
    public static final  String  HAVE_WITH = "时间";
    public static final  String  HAVE_PERSON = "人";
    public static final  String  HAVE_OPER = "操作";

    public static final  String  SORT_INDEX_PUT = "sortIndexPut";

    /*数组类型数据*/
    public static final List<String> NAME_LIST = Arrays.asList("总部","南方中心","上海金桥灾备");

    /*文本时数组*/
    public static final  String  APPLICANT_PEOPLE = "分配人";
    public static final  String  IP_ADDRESS_TEXT = "IP地址";
    public static final  String  IP_ADDRESS_SIGN_TEXT = "局域网";
    public static final  String  SUBMIT_TIME = "提交时间";
    public static final  String  OPER_NUMBER = "操作数";

    /*HTTPCONTAXT*/
    public static final  String  HTTP_CONTAXT = "application/vnd.ms-excel";
    public static final  String  HTTP_UTF = "utf-8";
    public static final  String  CONTENT_DISPOSITION = "Content-disposition";
    public static final  String  ATTACHMENT_FILENAME = "attachment;filename=" ;
    public static final  String  FLIENAME_FIX = ".xlsx" ;
    public static final  String  SHEET_ONE = "sheet01" ;
    /*sayno*/
    public static final  String  ELEMENT = "no" ;
    public static final String DATEBASEMYSQL = "mysql";
    public static final String DATEBASEORACLE = "oracle";
}

