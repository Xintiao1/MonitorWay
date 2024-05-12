package cn.mw.monitor.user.control;


import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.user.dto.MwGroupDTO;
import cn.mw.monitor.user.dto.UserGroupSortDTO;
import cn.mw.monitor.user.model.MwUserGroupMapper;
import cn.mw.monitor.user.service.MwUserGroupMapperService;
import cn.mw.monitor.user.service.impl.MWUserGroupServiceImpl;
import cn.mwpaas.common.model.Reply;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author swy
 * @since 2023-11-21
 */
@RestController
@RequestMapping("/mwapi")
@Api(value = "用户组管理接口",tags = "用户组管理接口")
public class MwUserGroupMapperController extends BaseApiService {

    @Autowired
    private MwUserGroupMapperService userGroupService;

    /**
     * 拖拽数据更新排序
     */
    @ApiOperation(value="拖动排序")
    @PostMapping("/userGroup/sort")
    public ResponseBase sort(@RequestBody UserGroupSortDTO dto) {
        return setResultSuccess(userGroupService.sort(dto.getCurrentId(),dto.getTargetId()));
    }
}
