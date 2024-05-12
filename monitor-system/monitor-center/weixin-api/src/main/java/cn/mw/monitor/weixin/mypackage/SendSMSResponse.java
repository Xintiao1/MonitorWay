
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
 *         &lt;element name="SendSMSResult" type="{http://tempuri.org/}SendSMSRet" minOccurs="0"/>
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
    "sendSMSResult"
})
@XmlRootElement(name = "SendSMSResponse")
public class SendSMSResponse {

    @XmlElement(name = "SendSMSResult")
    protected SendSMSRet sendSMSResult;

    /**
     * ��ȡsendSMSResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link SendSMSRet }
     *     
     */
    public SendSMSRet getSendSMSResult() {
        return sendSMSResult;
    }

    /**
     * ����sendSMSResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link SendSMSRet }
     *     
     */
    public void setSendSMSResult(SendSMSRet value) {
        this.sendSMSResult = value;
    }

}
