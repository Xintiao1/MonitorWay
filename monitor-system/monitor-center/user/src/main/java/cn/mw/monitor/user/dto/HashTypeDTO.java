package cn.mw.monitor.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class HashTypeDTO {
    private Integer id;
    private String hashName;
    private String implClass;
    private Integer iterations;
}
