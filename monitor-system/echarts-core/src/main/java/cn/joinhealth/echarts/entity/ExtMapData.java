package cn.joinhealth.echarts.entity;

import lombok.Data;

@Data
public class ExtMapData<K,V> {
	private K name;
	private V value;

	public ExtMapData(K key ,V value){
		this.name = key;
		this.value = value;
	}


	@Override
	public boolean equals(Object obj) {

		ExtMapData testObj;
		try {
			testObj = (ExtMapData) obj;
		} catch (Exception e) {
			//类不同，直接返回false
			return false;
		}
		if(testObj.getName().equals(this.getName())){
			return true;
		}
		return false;
	}
}
