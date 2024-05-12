package cn.mw.monitor.ipaddressmanage.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
public class BatchOnLineUpdate {
    private int ipState;
    private int online;
    private Date updateDate;
    private List<Integer> ids;

    public BatchOnLineUpdate(int ipState ,int ipOnline ,Integer id){
        this.ipState = ipState;
        this.online = ipOnline;
        if(null == ids){
            ids = new ArrayList<>();
        }
        ids.add(id);
    }

    public void add(Integer id){
        if(null == ids){
            ids = new ArrayList<>();
        }
        ids.add(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchOnLineUpdate that = (BatchOnLineUpdate) o;
        return ipState == that.ipState &&
                online == that.online;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipState, online);
    }
}
