package cn.mw.monitor.common.util;

import java.util.List;

/**
 * Created by 余品灵 on 2019/7/25.
 */
public class PageList {

    Page page = null;

    public PageList(){
        page = new Page();
    }

    public List getList(List list,int star,int pageSize){
        if(list.size()==0){
            page.setStartRow(0);
            page.setEndRow(0);
        }else{
            int starNum = (star - 1) * pageSize+1;
            if(starNum > list.size()){
                star = list.size() / pageSize;
                page.setStartRow((star - 1) * pageSize+1);
            }else{
                page.setStartRow((star - 1) * pageSize+1);
            }
            if(star>1){
                page.setEndRow(star * pageSize);
            }else{
                page.setEndRow(pageSize);
            }
        }
        if (page.getCurrentPage() == null){
            page.setCurrentPage(1);
        } else {
            page.setCurrentPage(page.getCurrentPage());
        }
        //list的大小
        int count = list.size();
        //设置每页数据为十条
        page.setPageSize(pageSize);
        //每页的开始数
        if(count == 0 || page.getStartRow() < 0){
            page.setStar(0);
        }else{
            page.setStar((star - 1) * page.getPageSize());
        }

        //设置总页数
        page.setTotalPage(count % 10 == 0 ? count / 10 : count / 10 + 1);
        //对list进行截取
        page.setDataList(list.subList(page.getStar(),count-page.getStar()>page.getPageSize()?page.getStar()+page.getPageSize():count));
        //设置作用域



        return  page.getDataList();

    }

    /**
     * 返回总页数
     * @return
     */
    public int getPages(){
        return page.getTotalPage();
    }

    public int getStartRow(){
        return page.getStartRow();
    }

    public int getEndRow(){
        return page.getEndRow();
    }




}
