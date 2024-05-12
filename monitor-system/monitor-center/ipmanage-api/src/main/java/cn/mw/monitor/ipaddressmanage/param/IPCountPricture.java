package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

/**
 * @author lumingming
 * @createTime 2022916 16:10
 * @description
 */
@Data
public class IPCountPricture {
    List<IPCountPrictureDetails> details;

    @ApiModelProperty(value = "分组数据")
    private String counts;

    @ApiModelProperty(value="0.创建 1.删除")
    private Integer type;

    public List<IPCountPrictureDetails> getDetails() {
        return details;
    }

    public void setDetails(List<IPCountPrictureDetails> details) {
        if (details.size()>7){
            details.sort(new Comparator<IPCountPrictureDetails>() {
                @Override
                public int compare(IPCountPrictureDetails o1, IPCountPrictureDetails o2) {
                    int i1 = o1.getCreateTime();
                    int i2 = o2.getCreateTime();
                    return i1-i2;
                }
            });
            this.details = details;
        }else {
            details.sort(new Comparator<IPCountPrictureDetails>() {
                @Override
                public int compare(IPCountPrictureDetails o1, IPCountPrictureDetails o2) {
                    int i1 = o1.getCreateTime();
                    int i2 = o2.getCreateTime();
                    if (i1-i2>10 || i1-i2<-10){
                        return -(i1-i2);
                    }
                    return i1-i2;
                }
            });
            for (int i = 0; i < details.size(); i++) {
                details.get(i).setCreateTime(i+1);
            }
            this.details = details;
        }

    }
}
