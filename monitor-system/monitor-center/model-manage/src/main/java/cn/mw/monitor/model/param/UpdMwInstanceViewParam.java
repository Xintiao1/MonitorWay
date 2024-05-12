package cn.mw.monitor.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdMwInstanceViewParam {
    @NotNull
    private Long id;

    @NotBlank
    private String viewName;
}
