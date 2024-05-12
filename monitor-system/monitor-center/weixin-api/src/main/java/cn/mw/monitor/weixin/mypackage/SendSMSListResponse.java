
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
 *         &lt;element name="SendSMSListResult" type="{http://tempuri.org/}SendSMSListRet" minOccurs="0"/>
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
    "sendSMSListResult"
})
@XmlRootElement(name = "SendSMSListResponse")
public class SendSMSListResponse {

    @XmlElement(name = "SendSMSListResult")
    protected SendSMSListRet sendSMSListResult;

    /**
     * ��ȡsendSMSListResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link SendSMSListRet }
     *     
     */
    public SendSMSListRet getSendSMSListResult() {
        return sendSMSListResult;
    }

    /**
     * ����sendSMSListResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link SendSMSListRet }
     *     
     */
    public void setSendSMSListResult(SendSMSListRet value) {
        this.sendSMSListResult = value;
    }

}
