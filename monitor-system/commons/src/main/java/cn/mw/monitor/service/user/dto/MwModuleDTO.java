package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.common.bean.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwModuleDTO extends BaseDTO {

    private Integer id;

    private Integer pid;

    private String moduleName;

    private String moduleDesc;

    private String url;

    private Byte isNode;

    private Byte deep;

    private String nodes;

    private Boolean enable;

    private Integer version;

    private Boolean deleteFlag;

    private List<MwPermissionDTO> mwPermissionList;

}
