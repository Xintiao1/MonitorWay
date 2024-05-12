
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
 *         &lt;element name="SMS_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "smsid"
})
@XmlRootElement(name = "GetReportResponse")
public class GetReportResponse {

    @XmlElement(name = "App_ID")
    protected int appID;
    @XmlElement(name = "App_PWD")
    protected String appPWD;
    @XmlElement(name = "SMS_ID")
    protected String smsid;

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
     * ��ȡsmsid���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSMSID() {
        return smsid;
    }

    /**
     * ����smsid���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSMSID(String value) {
        this.smsid = value;
    }

}
