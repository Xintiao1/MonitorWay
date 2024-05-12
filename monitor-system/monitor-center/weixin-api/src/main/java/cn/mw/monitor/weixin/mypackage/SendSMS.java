
package cn.mw.monitor.weixin.mypackage;

import java.math.BigDecimal;
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
 *         &lt;element name="Biz_Class_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Biz_Type_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Biz_Sub_Type_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Ext_No" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Phone_No" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Content" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Is_Need_Report" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Cust_ID" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="App_SMS_Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "bizClassID",
    "bizTypeID",
    "bizSubTypeID",
    "extNo",
    "phoneNo",
    "content",
    "isNeedReport",
    "custID",
    "appSMSCode"
})
@XmlRootElement(name = "SendSMS")
public class SendSMS {

    @XmlElement(name = "App_ID")
    protected int appID;
    @XmlElement(name = "App_PWD")
    protected String appPWD;
    @XmlElement(name = "Biz_Class_ID")
    protected String bizClassID;
    @XmlElement(name = "Biz_Type_ID")
    protected String bizTypeID;
    @XmlElement(name = "Biz_Sub_Type_ID")
    protected String bizSubTypeID;
    @XmlElement(name = "Ext_No")
    protected String extNo;
    @XmlElement(name = "Phone_No")
    protected String phoneNo;
    @XmlElement(name = "Content")
    protected String content;
    @XmlElement(name = "Is_Need_Report")
    protected int isNeedReport;
    @XmlElement(name = "Cust_ID", required = true)
    protected BigDecimal custID;
    @XmlElement(name = "App_SMS_Code")
    protected String appSMSCode;

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
     * ��ȡphoneNo���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNo() {
        return phoneNo;
    }

    /**
     * ����phoneNo���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNo(String value) {
        this.phoneNo = value;
    }

    /**
     * ��ȡcontent���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContent() {
        return content;
    }

    /**
     * ����content���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContent(String value) {
        this.content = value;
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
     * ��ȡcustID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCustID() {
        return custID;
    }

    /**
     * ����custID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCustID(BigDecimal value) {
        this.custID = value;
    }

    /**
     * ��ȡappSMSCode���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppSMSCode() {
        return appSMSCode;
    }

    /**
     * ����appSMSCode���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppSMSCode(String value) {
        this.appSMSCode = value;
    }

}
