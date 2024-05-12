package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址分配回填 ")
public class ResponIpDistributtionParam {
    //主键
    @ApiModelProperty(value="主键")
    private Integer id;
    @ApiModelProperty(value="选项")
    private List<Check> radio;

    @ApiModelProperty(value="上节点")
    private List<Integer> upTree;

    @ApiModelProperty(value="上节点")
    private String upTreeText;

    @ApiModelProperty(value="上节点")
    private List<Integer> downTree;

    @ApiModelProperty(value="分配地址")
    private String primaryIp;

    @ApiModelProperty(value="分配上一级名称")
    private String label;

    @ApiModelProperty(value="源IP")
    private String sourceIP;

    @ApiModelProperty(value="源IP的parentIP")
    private String sourceRadio;

    @ApiModelProperty(value="选择选项")
    private String chose;
    @ApiModelProperty(value="选择选项类型")
    private Boolean choseType;

    @ApiModelProperty(value="选项值")
    String keyValue;
    @ApiModelProperty(value="选项值INT")
    Integer keyValueInt;

    @ApiModelProperty(value="地址是否为IPv4")
    private Boolean idType;

    @ApiModelProperty(value="属性")
    private List<Label> attrParam;

    @ApiModelProperty(value="属性")
    private List<MwAssetsLabelDTO> attrData;

    @ApiModelProperty(value="ip关系ip")
    String bangDistri;

    public void putradom( List<Check> checks){
        List<Check> checkList = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        for (Check check :checks){
            String s = getRandomString(strings,4);
            strings.add(s);
            if (check.isIdType()){

                check.setRadom("-"+check.getKeyValue());
            }else {
                Integer integer =Integer.valueOf(check.getKeyValue())*10000;
                check.setRadom(integer.toString());
            }

            checkList.add(check);
        }
        this.radio = checkList;
    }



    public  String getRandomString(List<String> strings,int length){
        String str="abcdefghijklmnopqrstuvwxyz";
        String string  = "";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(26);
            sb.append(str.charAt(number));
            string=sb.toString();
        }
        if (strings.contains(string)){
            string=getRandomString(strings,length);
        }
        else {
            return string;
        }
        return string;
    }
}
