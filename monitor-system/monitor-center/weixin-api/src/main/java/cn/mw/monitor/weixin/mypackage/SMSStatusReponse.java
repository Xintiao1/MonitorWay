
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>SMS_Status_Reponse complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="SMS_Status_Reponse">
 *   &lt;complexContent>
 *     &lt;extension base="{http://tempuri.org/}Msg_Response">
 *       &lt;sequence>
 *         &lt;element name="SMSStatusList" type="{http://tempuri.org/}ArrayOfSMS_Status_Info" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SMS_Status_Reponse", propOrder = {
    "smsStatusList"
})
public class SMSStatusReponse
    extends MsgResponse
{

    @XmlElement(name = "SMSStatusList")
    protected ArrayOfSMSStatusInfo smsStatusList;

    /**
     * ��ȡsmsStatusList���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSMSStatusInfo }
     *     
     */
    public ArrayOfSMSStatusInfo getSMSStatusList() {
        return smsStatusList;
    }

    /**
     * ����smsStatusList���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSMSStatusInfo }
     *     
     */
    public void setSMSStatusList(ArrayOfSMSStatusInfo value) {
        this.smsStatusList = value;
    }

}
