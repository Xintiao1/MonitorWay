package cn.mwpaas.common.utils;

import cn.mwpaas.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author phzhou
 * @ClassName BeansUtils
 * @CreateDate 2019/4/2
 * @Description
 */
public class BeansUtils extends BeanUtils {

    /**
     * lit转list
     * @param sourceList
     * @param type
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> type) {
        if (sourceList == null) {
            return null;
        }
        if (sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            T target = null;
            try {
                target = type.newInstance();
            } catch (Exception e) {
                String className = type.getName();
                String elementType = sourceList.get(0).getClass().getCanonicalName();
                String message = String.format("SYSTEM_LOG [][] element type = %s, result type = %s, message = %s", elementType, className, e.getMessage());
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
            }
            BeansUtils.copyProperties(source, target);
            result.add(target);
        }
        return result;
    }

    /**
     * bean 转 bean
     * @param source
     * @param type
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> T convert(S source, Class<T> type) {
        if (source == null) {
            return null;
        }
        try {
            T target = type.newInstance();
            BeansUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            String message = String.format("SYSTEM_LOG [][] element type = %s, result type = %s, message = %s", source.getClass().getCanonicalName(), type.getCanonicalName(), e.getMessage());
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
        }
    }
}
