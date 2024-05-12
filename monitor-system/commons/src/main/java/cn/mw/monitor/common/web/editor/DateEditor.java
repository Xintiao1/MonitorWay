package cn.mw.monitor.common.web.editor;

import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateEditor extends PropertyEditorSupport {

    public String getAsText() {
        Date value = (Date) getValue();
        if(null == value){
            return null;
        }
        SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");
        return df.format(value);
    }

    public void setAsText(String text) throws IllegalArgumentException {
        Date value = null;
        if(null != text && !text.equals("")){
            SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");
            try{
                value = df.parse(text);
            }catch(Exception e){
                log.error("错误返回 :{}",e);
            }
        }
        setValue(value);
    }
}