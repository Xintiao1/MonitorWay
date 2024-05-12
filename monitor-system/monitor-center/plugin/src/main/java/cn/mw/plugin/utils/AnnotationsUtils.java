package cn.mw.plugin.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 注解工具
 * @author starBlues
 * @version 1.0
 */
public class AnnotationsUtils {

    private AnnotationsUtils() {

    }

    /**
     * 类的方法是否具有 某些注解
     * @param aClass
     * @param annotationClass
     * @return
     */
    public static boolean haveMethodAnnotations(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        if (aClass == null) {
            return false;
        }
        if (annotationClass == null) {
            return false;
        }
        for (Method declaredMethod : aClass.getDeclaredMethods()) {
            if (null != declaredMethod.getAnnotation(annotationClass)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 存在注解判断
     *
     * @param aClass            类
     * @param isAllMatch        是否匹配全部注解
     * @param annotationClasses 注解类
     * @return boolean
     */
    @SafeVarargs
    public static boolean haveAnnotations(Class<?> aClass, boolean isAllMatch,
                                          Class<? extends Annotation>... annotationClasses) {
        if (aClass == null) {
            return false;
        }
        if (annotationClasses == null) {
            return false;
        }
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            Annotation annotation = aClass.getAnnotation(annotationClass);
            if (null == annotation) {
                for (Annotation aClassAnnotation : aClass.getAnnotations()) {
                    if (aClassAnnotation.annotationType().getSimpleName().equals(annotationClass.getSimpleName())) {
                        annotation = aClassAnnotation;
                    }
                }
            }
            if (isAllMatch) {
                if (annotation == null) {
                    return false;
                }
            } else {
                if (annotation != null) {
                    return true;
                }
            }
        }
        if (isAllMatch) {
            return true;
        } else {
            return false;
        }
    }

}
