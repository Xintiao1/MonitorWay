package cn.mw.monitor.util;

import cn.mw.monitor.service.assets.model.AssetsInterfaceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import java.util.List;

@Slf4j
public class DBUtils {
    public <D ,I> void doBatchInsert(SqlSessionFactory sqlSessionFactory
            , List<D> dataList, Class<D> dClass, Class<I> iClass, int batchInsertSize, BatchInsert<I ,D> batchInsert) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        I mapper = session.getMapper(iClass);
        int count = 0;

        try {
            for (D data : dataList) {
                count++;
                batchInsert.accept(mapper ,data);
                if (0 == (count % batchInsertSize) || count == dataList.size()) {
                    log.info("{} doBatchInsert:{}" ,dClass.getSimpleName() ,count);
                    session.commit();
                    session.clearCache();
                }
            }
        } catch (Exception e) {
            //没有提交的数据可以回滚
            session.rollback();
            log.error("batchSuccInsert", e);
        } finally {
            session.close();
        }
    }

    @FunctionalInterface
    public interface BatchInsert<M ,D> {
        void accept(M m, D d);
    }
}
