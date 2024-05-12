package cn.mw.monitor.link.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName MwLinkTreeDto
 * @Description 线路树状结构数据
 * @Author gengjb
 * @Date 2022/3/3 10:32
 * @Version 1.0
 **/
@Data
public class MwLinkTreeDto {

    //目录ID
    private Integer id;

    //父目录ID
    private int parentId;

    //目录排序
    private Integer sort;

    //目录下线路信息
    private List<NetWorkLinkDto> links;

    //目录名称
    private String contentsName;

    //目录下线路ID
    private List<String> linkIds;

    //子目录数据
    private List<MwLinkTreeDto> children;

    //机构
//    private List<Integer> orgId;

    private String orgIdStr;

    //用户组
    private List<Integer> groupIds;

    private String userGroupIdStr;

    //用户
    private List<Integer> userIds;

    private String userIdStr;

    //描述
    private String describe;

    private List<List<Integer>> orgIds;


    private Integer userId;

    private  Boolean isAdmin;

    //0：目录  1：线路
    private int type;

    private String strId;

    //拖动的目录或者线路
    private String originId;

    //拖动进去的目录
    private String targetId;

    private String linkId;

    private String moveSign;

    private int targetType;

    private int originType;

    private int count;
}
