
package cn.mw.monitor.weixin.mypackage;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ArrayOfSMS_Status_Info complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="ArrayOfSMS_Status_Info">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SMS_Status_Info" type="{http://tempuri.org/}SMS_Status_Info" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfSMS_Status_Info", propOrder = {
    "smsStatusInfo"
})
public class ArrayOfSMSStatusInfo {

    @XmlElement(name = "SMS_Status_Info", nillable = true)
    protected List<SMSStatusInfo> smsStatusInfo;

    /**
     * Gets the value of the smsStatusInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the smsStatusInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSMSStatusInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SMSStatusInfo }
     * 
     * 
     */
    public List<SMSStatusInfo> getSMSStatusInfo() {
        if (smsStatusInfo == null) {
            smsStatusInfo = new ArrayList<SMSStatusInfo>();
        }
        return this.smsStatusInfo;
    }

}
