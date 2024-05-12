package cn.mw.monitor.common.util;

public enum AlertAssetsEnum {
    unconfirmed("未确认"),
    confirmed("已确认"),
    Assets("assets"),
    Label("label"),
    Add("add"),
    Del("del"),
    Comma(","),
    MinusOne("-1"),
    Zero("0"),
    One("1"),
    Three("3"),
    LeftBracket("["),
    RightBracket("]"),
    LeftBracketZH("【"),
    RightBracketZH("】"),
    LEFTPARENTHESES("("),
    RIGETPARENTHESES(")"),
    COLON(":"),
    ELLIPSIS("..."),
    UNDERLINE("_"),
    ZEROZH("零"),
    ONEZH("一"),
    TWOZH("二"),
    THREEZH("三"),
    FOURZH("四"),
    FIVEZH("五"),
    SIXZH("六"),
    SEVENZH("七"),
    EIGHTZH("八"),
    NINEZH("九"),
    QUESTION("?"),
    EQUAL("="),
    AND("&"),
    SLASH("/"),
    greater("大于"),
    less("小于"),
    equal("等于"),
    Dash("-");

    private AlertAssetsEnum(String desc) {
        this.description = desc;
    }

    public String toString() {
        return description;
    }

    private String description;

    public String getDescription() {
        return description;
    }
}
