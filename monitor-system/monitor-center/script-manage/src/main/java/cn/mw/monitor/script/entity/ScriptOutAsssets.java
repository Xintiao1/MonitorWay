package cn.mw.monitor.script.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author lumingming
 * @createTime 20230412 17:03
 * @description
 */
@Data
@TableName("mw_script_out_asssets")
public class ScriptOutAsssets {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;
    /**
     * 资产名称
     */
    @TableField("hostname")
    private String hostname;
    @TableField("ip")
    private String ip;
    @TableField("account_id")
    private Integer accountId;
}
