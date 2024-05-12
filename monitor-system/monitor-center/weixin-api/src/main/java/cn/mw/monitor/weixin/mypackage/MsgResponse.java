
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Msg_Response complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="Msg_Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Ret" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Ret_Desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Msg_Response", propOrder = {
    "ret",
    "retDesc"
})
@XmlSeeAlso({
    SendSMSRet.class,
    SMSStatusReponse.class,
    SendSMSListRet.class,
    ReceveResponse.class,
    ReportResponse.class,
    TaskInfoResponse.class
})
public class MsgResponse {

    @XmlElement(name = "Ret")
    protected int ret;
    @XmlElement(name = "Ret_Desc")
    protected String retDesc;

    /**
     * ��ȡret���Ե�ֵ��
     * 
     */
    public int getRet() {
        return ret;
    }

    /**
     * ����ret���Ե�ֵ��
     * 
     */
    public void setRet(int value) {
        this.ret = value;
    }

    /**
     * ��ȡretDesc���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRetDesc() {
        return retDesc;
    }

    /**
     * ����retDesc���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRetDesc(String value) {
        this.retDesc = value;
    }

}
