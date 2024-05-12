package cn.mw.monitor.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DelMwInstanceViewParam {
    @NotNull
    private Long id;
}
