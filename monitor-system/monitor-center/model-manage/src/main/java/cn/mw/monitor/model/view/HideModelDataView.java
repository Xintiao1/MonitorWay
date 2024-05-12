package cn.mw.monitor.model.view;

import lombok.Data;

import java.util.List;

@Data
public class HideModelDataView {
    private List<Integer> hide;
    private List<Integer> show;
}
