package cn.mw.monitor.report.service.manager;


/**
 * @author xhy
 * @date 2020/12/29 17:00
 */
public enum AssetsLabel {
   // category("1", "设施分类标识符", 3),
    useDate("2", "投产日期", 2),
 //   useState("3", "在用状态", 1),
    assetsCode("4", "资产编码", 1),
    brandLand("5", "品牌属地", 3),
  //  systemVersion("6", "操作系统版本信息", 1),
    deviceHeight("7", "设备高度", 1),
    supportIpv6("8", "IPV6支持能力", 3),
    influenceSystem("9", "影响系统", 3),
    deployArea("10", "部署属性", 3),
    operationDepartment("11", "运维属性", 3),
    belongCabinet("12", "安装位置-机柜", 1),
    slotNo("13", "安装位置-槽位号", 1);


    private String id;
    private String name;
    private Integer inputFormat;

    AssetsLabel(String id, String name, Integer inputFormat) {
        this.id = id;
        this.name = name;
        this.inputFormat = inputFormat;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public Integer getInputFormat() {
        return inputFormat;
    }
}
