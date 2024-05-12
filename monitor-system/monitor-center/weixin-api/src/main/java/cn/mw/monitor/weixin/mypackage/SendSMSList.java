
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="App_ID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="App_PWD" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Task_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Biz_Class_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Biz_Type_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Biz_Sub_Type_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Ext_No" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Info_List" type="{http://tempuri.org/}ArrayOfSend_Info" minOccurs="0"/>
 *         &lt;element name="Is_Need_Report" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Is_End" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "appID",
    "appPWD",
    "taskID",
    "bizClassID",
    "bizTypeID",
    "bizSubTypeID",
    "extNo",
    "infoList",
    "isNeedReport",
    "isEnd"
})
@XmlRootElement(name = "SendSMSList")
public class SendSMSList {

    @XmlElement(name = "App_ID")
    protected int appID;
    @XmlElement(name = "App_PWD")
    protected String appPWD;
    @XmlElement(name = "Task_ID")
    protected String taskID;
    @XmlElement(name = "Biz_Class_ID")
    protected String bizClassID;
    @XmlElement(name = "Biz_Type_ID")
    protected String bizTypeID;
    @XmlElement(name = "Biz_Sub_Type_ID")
    protected String bizSubTypeID;
    @XmlElement(name = "Ext_No")
    protected String extNo;
    @XmlElement(name = "Info_List")
    protected ArrayOfSendInfo infoList;
    @XmlElement(name = "Is_Need_Report")
    protected int isNeedReport;
    @XmlElement(name = "Is_End")
    protected int isEnd;

    /**
     * ��ȡappID���Ե�ֵ��
     * 
     */
    public int getAppID() {
        return appID;
    }

    /**
     * ����appID���Ե�ֵ��
     * 
     */
    public void setAppID(int value) {
        this.appID = value;
    }

    /**
     * ��ȡappPWD���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppPWD() {
        return appPWD;
    }

    /**
     * ����appPWD���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppPWD(String value) {
        this.appPWD = value;
    }

    /**
     * ��ȡtaskID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskID() {
        return taskID;
    }

    /**
     * ����taskID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskID(String value) {
        this.taskID = value;
    }

    /**
     * ��ȡbizClassID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBizClassID() {
        return bizClassID;
    }

    /**
     * ����bizClassID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBizClassID(String value) {
        this.bizClassID = value;
    }

    /**
     * ��ȡbizTypeID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBizTypeID() {
        return bizTypeID;
    }

    /**
     * ����bizTypeID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBizTypeID(String value) {
        this.bizTypeID = value;
    }

    /**
     * ��ȡbizSubTypeID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBizSubTypeID() {
        return bizSubTypeID;
    }

    /**
     * ����bizSubTypeID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBizSubTypeID(String value) {
        this.bizSubTypeID = value;
    }

    /**
     * ��ȡextNo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtNo() {
        return extNo;
    }

    /**
     * ����extNo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtNo(String value) {
        this.extNo = value;
    }

    /**
     * ��ȡinfoList���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSendInfo }
     *     
     */
    public ArrayOfSendInfo getInfoList() {
        return infoList;
    }

    /**
     * ����infoList���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSendInfo }
     *     
     */
    public void setInfoList(ArrayOfSendInfo value) {
        this.infoList = value;
    }

    /**
     * ��ȡisNeedReport���Ե�ֵ��
     * 
     */
    public int getIsNeedReport() {
        return isNeedReport;
    }

    /**
     * ����isNeedReport���Ե�ֵ��
     * 
     */
    public void setIsNeedReport(int value) {
        this.isNeedReport = value;
    }

    /**
     * ��ȡisEnd���Ե�ֵ��
     * 
     */
    public int getIsEnd() {
        return isEnd;
    }

    /**
     * ����isEnd���Ե�ֵ��
     * 
     */
    public void setIsEnd(int value) {
        this.isEnd = value;
    }

}
