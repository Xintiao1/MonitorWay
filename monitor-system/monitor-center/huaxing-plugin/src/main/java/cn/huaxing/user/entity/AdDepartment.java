package cn.huaxing.user.entity;

import cn.mw.monitor.util.Pinyin4jUtil;

import java.text.Collator;
import java.util.*;

/**
 * @ClassName AdDepartment
 * @Description: TODO
 * @Author zhaoy
 */
public class AdDepartment implements Comparable<AdDepartment>{
    private String id;
    private String name;
    private String cName;
    private String distinguishedName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public Set<AdDepartment> getChildren() {
        return children;
    }

    public void setChildren(Set<AdDepartment> children) {
        this.children = children;
    }

    private Set<AdDepartment> children = new TreeSet<>();


    public AdDepartment getAdDepartmentBycName(String cName) {
        if (this.cName.equals(cName) ) {
            return this;
        }else{
            for (AdDepartment adDepartment : children) {
                AdDepartment adDepartment1 =null;
                if ((adDepartment1 = adDepartment.getAdDepartmentBycName(cName)) != null) {
                    return adDepartment1;
                }
            }
        }
        return null;
    }
    public AdDepartment getParentAdDepartmentBycName(String cName) {
        int index;
        AdDepartment adDepartment = null;
        while ((index = cName.lastIndexOf("/")) != -1) {
            cName = cName.substring(0, index);
            adDepartment = getAdDepartmentBycName(cName);
            if (adDepartment != null) {
                return adDepartment;
            }
        }
        return null;
    }

    /**
     * 排序功能（中英文混合排序，按照A-Z排序）
     *
     * @param o set集合中上一个元素
     * @return 排序结果
     */
    @Override
    public int compareTo(AdDepartment o) {
        //先判断双方是否是纯中文
        Comparator<Object> com = Collator.getInstance(Locale.CHINA);
        Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
        return com.compare(pinyin4jUtil.getStringPinYin(cName), pinyin4jUtil.getStringPinYin(o.getcName()));
    }

    public void addChildren(AdDepartment adDepartment) {
        this.children.add(adDepartment);
    }

    @Override
    public String toString() {
        return "AdDepartment{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cName='" + cName + '\'' +
                ", distinguishedName='" + distinguishedName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdDepartment that = (AdDepartment) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(cName, that.cName) &&
                Objects.equals(distinguishedName, that.distinguishedName) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cName, distinguishedName, children);
    }
}
