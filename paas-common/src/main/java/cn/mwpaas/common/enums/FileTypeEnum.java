package cn.mwpaas.common.enums;

import java.io.File;
import java.io.IOException;


/**
 * 文件路径
 *
 * @author bjyuan
 */
public enum FileTypeEnum {
    HEAD("001", "/head/", "头像"), IDCARD("002", "/idcard/", "身份证"), RECORD("003", "/record/", "语音"), CHAGPIC("004", "/chatpic/", "聊天图片"), HOSPHEAD("005", "/hosphead/", "医院小图标"), ADVERTISEMENT("006", "/advertisement/", "广告图片"), QUESTIONNAIRE("007", "/questionnaire/", "随访问卷"),
    MEDICAL_RECORDS("008", "/medical/", "自建病历"), ARTICLE("009", "/article/", "专题文章"), ARTICLECONTENT("010", "/articleContent/", "专题文章内容"),
    HEALTH("011", "/health/", "健康数据"), MENU("012", "/menu/", "菜单图片"), SHORT_VIEW("020", "/shortView/", "短视频"), DYNAMIC_IMAGE("021", "/dynamicImage/", "动态图片"),
    DYNAMIC_VIDEO("022", "/dynamicVideo/", "动态视频"), EQUIP_PIC("023", "/equipPic/", "设备图片"), HUG_DEVICE("024", "/hugDevice/", "hugDevice图片"),
    MATERMALCHILD_BOOK("025", "/matermalchildBook/", "母子健康手册图片"), KNOWLEDGE_REPOSITORY_DIET("028", "/knowledgeRepository/diet/", "饮食库图片"), KNOWLEDGE_REPOSITORY_SPORT("027", "/knowledgeRepository/sport/", "运动库图片"),
    HOLTER_PDF("026", "/holterPDF/", "心电PDF"), CHRONIC_MANAGE_PROGRAM("029", "/chronicManage/program/", "慢病管理方案库"), COP_HANDEL("030", "/cop/handel/", "投诉表扬上传图片"), CONTRACT_RECORD("031", "/contract/record/", "签约记录文件"),
    MEDICAL_EXAMINATION("032", "/specialInformation/medicalExamination/", "常规体格检查"), PERSONAL_HISTORY("033", "/specialInformation/personalHistory/", "专项信息-个人史"),
    CALL("034", "/call/", "通话录音"), INFORMED_CONSENT("035", "/informedConsent/", "知情同意书"), EDUCATION("036", "/education/", "宣教图片");

    private String code;       //编码
    private String path;       //相对路径
    private String desc;       //描述

    FileTypeEnum(String code, String path, String desc) {
        this.code = code;
        this.path = path;
        this.desc = desc;
    }

    public static void main(String[] args) throws IOException {
        String filePath = "D://aaabbb/cccddd/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getPathByCode(String code) {
        for (FileTypeEnum fileType : values()) {
            if (code.equals(fileType.getCode())) {
                return fileType.getPath();
            }
        }
        return "";
    }

    public static FileTypeEnum getTypeByCode(String code) {
        for (FileTypeEnum fileType : values()) {
            if (code.equals(fileType.getCode())) {
                return fileType;
            }
        }
        return null;
    }
}
