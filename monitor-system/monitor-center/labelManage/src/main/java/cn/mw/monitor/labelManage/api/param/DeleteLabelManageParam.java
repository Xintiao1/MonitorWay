package cn.mw.monitor.labelManage.api.param;

import lombok.Data;

import java.util.List;

@Data
public class DeleteLabelManageParam {

    //private List<Integer> labelIdList;

    private Integer labelId;

    private String labelName;

    private Boolean deleteRestrict;

    private Integer inputFormat;

    private String dropdownValue;
}
