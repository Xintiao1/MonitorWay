package cn.mw.monitor.script.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className FileTransParam
 * @description 自动化——文件分发参数
 * @date 2022/5/17
 */
@Data
@ApiModel(value = "文件下发参数")
public class FileTransParam {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 执行ID
     */
    private Integer execId;

    /**
     * 上传限速(大于0表示开启)
     */
    @ApiModelProperty("上传限速")
    private Integer uploadLimit;

    /**
     * 下载限速(大于0表示开启)
     */
    @ApiModelProperty("下载限速")
    private Integer downloadLimit;

    /**
     * 任务名称
     */
    @ApiModelProperty("分发任务名称")
    private String scriptName;

    /**
     * 上传文件名称(已经同步到服务器上)
     */
    @ApiModelProperty("文件名称")
    private List<FilePathInfo> fileNameList;

    /**
     * 最大超时时间
     */
    @ApiModelProperty("超时时间")
    private Integer maxOverTime;

    /**
     * 源文件地址
     */
    @ApiModelProperty("源文件地址")
    private String sourceFilePath;

    /**
     * 目标文件地址
     */
    @ApiModelProperty("目标地址")
    private String targetFilePath;

    /**
     * 传输类别(1:强制模式 2:严谨模式)
     */
    @ApiModelProperty("传输类别")
    private Integer transType;

    /**
     * 全局账户ID
     */
    @ApiModelProperty("全局账户ID")
    private Integer defaultAccountId;

    /**
     * 下发指令资产列表
     */
    @ApiModelProperty("待下发资产数据")
    private List<TransAssets> transAssetsList;

    /**
     * 是否自选字段
     */
    @ApiModelProperty("是否自选字段")
    private Integer isVarible;
    /**
     * 步骤名称
     */
    @ApiModelProperty("步骤名称")
    private String stepName;

    /**
     * 忽略错误
     */
    @ApiModelProperty("忽略错误")
    private Boolean ignoreError = false;

    public class FilePathInfo{

        private long fileSize;

        private String filePath;

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
