package cn.mw.monitor.server.serverdto;

import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className ApplicationDTOV6
 * @description 针对zabbix在5.4及以上版本后不支持应用集的修复
 * @date 2023/2/8
 */
@Data
public class ApplicationDTOV6 extends ApplicationDTO {

    /**
     * 标签集合
     */
    private List<TagV6> tags;

    /**
     * 标签类
     */
    public static class TagV6 {
        /**
         * 标签头
         */
        private String tag;

        /**
         * 标签值
         */
        private String value;

        public TagV6(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        public TagV6() {
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
