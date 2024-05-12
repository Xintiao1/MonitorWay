package cn.mw.monitor.script.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className HomeworkParam
 * @description 作业请求参数
 * @date 2022/5/19
 */
@Data
@ApiModel(value = "作业请求参数")
public class HomeworkParam extends DataPermissionParam {

    /**
     * 主键ID
     */
    @ApiModelProperty("主键ID")
    private Integer id;

    /**
     * id列表
     */
    private List<Integer> ids;

    /**
     * 执行ID
     */
    private Integer execId;

    /**
     * 作业名称
     */
    @ApiModelProperty("作业名称")
    private String homeworkName;

    /**
     * 作业所在树ID
     */
    @ApiModelProperty("作业所在树ID")
    private Integer homeworkTreeId;

    /**
     * 作业描述
     */
    @ApiModelProperty("作业描述")
    private String homeworkDesc;

    /**
     * 创建开始时间
     */
    @ApiModelProperty("创建开始时间")
    private Date createDateStart;

    /**
     * 创建结束时间
     */
    @ApiModelProperty("创建结束时间")
    private Date createDateEnd;

    /**
     * 更新开始时间
     */
    @ApiModelProperty("更新开始时间")
    private Date modificationDateStart;

    /**
     * 更新结束时间
     */
    @ApiModelProperty("更新结束时间")
    private Date modificationDateEnd;

    /**
     * 作业执行列表
     */
    @ApiModelProperty("作业执行列表")
    private List<HomeworkChildParam> stepList;

    /**
     * 模糊查询内容
     */
    @ApiModelProperty("模糊查询字段")
    private String searchAll;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String creator;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    private String updater;



    /**
     * 更新人
     */
    @ApiModelProperty("作业变量ids")
    private List<Integer> variableIds;




    /**
     * 作业版本ID,用于记录作业信息
     */
    @ApiModelProperty("作业版本ID")
    private Integer homeworkVersionId;

    /**
     * 获取数据类别
     *
     * @return
     */
    @Override
    public DataType getBaseDataType() {
        return null;
    }

    /**
     * 获取绑定的数据ID
     *
     * @return
     */
    @Override
    public String getBaseTypeId() {
        return null;
    }

    /**
     * 作业执行类
     */
    public static class HomeworkChildParam {

        /**
         * 步骤类别 1:执行脚本 2:分发文件
         */
        @ApiModelProperty("步骤类别 1:执行脚本 2:分发文件")
        private Integer stepType;

        /**
         * 步骤名称
         */
        @ApiModelProperty("步骤名称")
        private String stepName;

        /**
         * 文件分发参数
         */
        @ApiModelProperty("文件分发参数")
        private FileTransParam fileTransParam;

        /**
         * 脚本参数
         */
        @ApiModelProperty("脚本参数")
        private ScriptManageParam scriptParam;

        /**
         * 执行顺序索引
         */
        @ApiModelProperty("执行顺序索引")
        private Integer index;


        public Integer getStepType() {
            return stepType;
        }

        public void setStepType(Integer stepType) {
            this.stepType = stepType;
        }

        public FileTransParam getFileTransParam() {
            return fileTransParam;
        }

        public void setFileTransParam(FileTransParam fileTransParam) {
            this.fileTransParam = fileTransParam;
        }

        public ScriptManageParam getScriptParam() {
            return scriptParam;
        }

        public void setScriptParam(ScriptManageParam scriptParam) {
            this.scriptParam = scriptParam;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getStepName() {
            return stepName;
        }

        public void setStepName(String stepName) {
            this.stepName = stepName;
        }
    }
}
