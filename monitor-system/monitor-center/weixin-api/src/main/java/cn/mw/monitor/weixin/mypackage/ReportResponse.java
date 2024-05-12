
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Report_Response complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="Report_Response">
 *   &lt;complexContent>
 *     &lt;extension base="{http://tempuri.org/}Msg_Response">
 *       &lt;sequence>
 *         &lt;element name="Count" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="InfoList" type="{http://tempuri.org/}ArrayOfReport_Info" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Report_Response", propOrder = {
    "count",
    "infoList"
})
public class ReportResponse
    extends MsgResponse
{

    @XmlElement(name = "Count")
    protected int count;
    @XmlElement(name = "InfoList")
    protected ArrayOfReportInfo infoList;

    /**
     * ��ȡcount���Ե�ֵ��
     * 
     */
    public int getCount() {
        return count;
    }

    /**
     * ����count���Ե�ֵ��
     * 
     */
    public void setCount(int value) {
        this.count = value;
    }

    /**
     * ��ȡinfoList���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfReportInfo }
     *     
     */
    public ArrayOfReportInfo getInfoList() {
        return infoList;
    }

    /**
     * ����infoList���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfReportInfo }
     *     
     */
    public void setInfoList(ArrayOfReportInfo value) {
        this.infoList = value;
    }

}
