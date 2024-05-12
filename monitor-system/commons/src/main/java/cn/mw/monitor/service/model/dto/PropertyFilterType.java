package cn.mw.monitor.service.model.dto;

public enum PropertyFilterType {
    insert(propertyInfo -> {
        return propertyInfo.getIsInsertShow();
    })
    ,list(propertyInfo -> {
        return propertyInfo.getIsListShow();
    })
    ,editor(propertyInfo -> {
        return propertyInfo.getIsEditorShow();
    })
    ,look(propertyInfo -> {
        return propertyInfo.getIsLookShow();
    });

    private PropertyFilter propertyFilter;

    PropertyFilterType(PropertyFilter propertyFilter){
        this.propertyFilter = propertyFilter;
    }

    public boolean filter(PropertyInfo propertyInfo){
        return this.propertyFilter.filter(propertyInfo);
    }
}
