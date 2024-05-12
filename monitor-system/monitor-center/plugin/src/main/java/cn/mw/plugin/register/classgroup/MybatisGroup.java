package cn.mw.plugin.register.classgroup;

import org.springframework.context.ApplicationContext;

/**
 * @author qiyao(1210)
 * @date 2022-03-14
 */
public class MybatisGroup extends CompoundClassGroup {
    public MybatisGroup() {

    }

    @Override
    public void initialize(ApplicationContext applicationContext) {
        groupList.add(new MybatisMapperGroup());
        // System.out.println(Arrays.toString(applicationContext.getBeanDefinitionNames()));
        if (applicationContext.containsBean("com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")) {
            groupList.add(new MybatisplusMapperGroup());
        }
    }

    @Override
    public String groupId() {
        return "mapper";
    }
}
