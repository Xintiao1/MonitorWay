package cn.mw.monitor.user.service.impl;


import cn.mw.monitor.user.dao.MwUserGroupMapperMapper;
import cn.mw.monitor.user.model.MwUserGroupMapper;
import cn.mw.monitor.user.service.MwUserGroupMapperService;
import cn.mwpaas.common.model.Reply;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author swy
 * @since 2023-11-21
 */
@Service
public class MwUserGroupMapperServiceImpl extends ServiceImpl<MwUserGroupMapperMapper, MwUserGroupMapper> implements MwUserGroupMapperService {

    @Override
    public Reply sort(Integer currentId, Integer targetId) {
        // 得到当前数据 获取sort
        MwUserGroupMapper current = this.getById(currentId);
        Integer currSort = current.getId();
        // 得到目标数据 获取目标sort
        MwUserGroupMapper target = this.getById(targetId);
        Integer targetSort = target.getId();

        // 获取当前和目标之间的数据
        QueryWrapper<MwUserGroupMapper> queryWrapper = new QueryWrapper<>();
        if (targetSort<currSort) {
            queryWrapper.lambda().between(MwUserGroupMapper::getId, targetSort, currSort);
        } else {
            queryWrapper.lambda().between(MwUserGroupMapper::getId, currSort, targetSort);
        }
        List<MwUserGroupMapper> list = this.list(queryWrapper);

        ArrayList<Integer> notList = new ArrayList<>();
        notList.add(currSort);
        list = list.stream().filter(model->{
            return !notList.contains(model.getId());
        }).map(model->{
            if (targetSort<currSort) {
                model.setId(model.getId() + 1);
            } else {
                model.setId(model.getId() - 1);
            }
            return model;
        }).collect(Collectors.toList());

        // 目标位置sort 给 当前数据sort
        Integer temp = current.getId();

        current.setId(target.getId());
        //2,2
        target.setId(temp);
        list.add(current);

        for (MwUserGroupMapper userGroup : list) {
            this.updateById(userGroup);
        }

        Stream<MwUserGroupMapper> stream = list.stream().sorted(Comparator.comparing(MwUserGroupMapper::getId));
        System.out.println(stream);
        return Reply.ok(stream);
    }
}
