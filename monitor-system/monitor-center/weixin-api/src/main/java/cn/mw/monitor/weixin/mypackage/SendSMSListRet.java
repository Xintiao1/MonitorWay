
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>SendSMSListRet complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="SendSMSListRet">
 *   &lt;complexContent>
 *     &lt;extension base="{http://tempuri.org/}Msg_Response">
 *       &lt;sequence>
 *         &lt;element name="Remaining" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SendSMSListRet", propOrder = {
    "remaining"
})
public class SendSMSListRet
    extends MsgResponse
{

    @XmlElement(name = "Remaining")
    protected int remaining;

    /**
     * ��ȡremaining���Ե�ֵ��
     * 
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * ����remaining���Ե�ֵ��
     * 
     */
    public void setRemaining(int value) {
        this.remaining = value;
    }

}
