package cn.mw.monitor.logManage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author zyq
 * @since 2023-06-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MwVectorChannelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * vector通道名称
     */
    private String channelName;

    /**
     * vector_ip地址
     */
    private String channelIp;

    /**
     * vector服务端口
     */
    private Integer channelPort;

    private Integer type;

    private String channelUserName;

    private String channelPassword;

    private String keyPath;

    /**
     * vector服务状态0.正常1.异常
     */
    private Integer status;


    private String relevanceRule;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    private String createUser;

    /**
     * 更新时间
     */
    private LocalDateTime modificationDate;

    private String modificationUser;


}
