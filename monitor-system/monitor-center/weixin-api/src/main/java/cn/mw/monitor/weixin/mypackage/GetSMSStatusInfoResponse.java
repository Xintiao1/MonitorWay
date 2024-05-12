
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
 *         &lt;element name="GetSMS_Status_InfoResult" type="{http://tempuri.org/}SMS_Status_Reponse" minOccurs="0"/>
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
    "getSMSStatusInfoResult"
})
@XmlRootElement(name = "GetSMS_Status_InfoResponse")
public class GetSMSStatusInfoResponse {

    @XmlElement(name = "GetSMS_Status_InfoResult")
    protected SMSStatusReponse getSMSStatusInfoResult;

    /**
     * ��ȡgetSMSStatusInfoResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link SMSStatusReponse }
     *     
     */
    public SMSStatusReponse getGetSMSStatusInfoResult() {
        return getSMSStatusInfoResult;
    }

    /**
     * ����getSMSStatusInfoResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link SMSStatusReponse }
     *     
     */
    public void setGetSMSStatusInfoResult(SMSStatusReponse value) {
        this.getSMSStatusInfoResult = value;
    }

}
