package cn.mw.monitor.api.common;


import lombok.Data;

@Data
public class ResponseBase<T> {
	private int rtnCode;
	private String msg;
	private T data;
	private String licMsg;
	public ResponseBase(Integer rtnCode, String msg, T data) {
		this.rtnCode = rtnCode;
		this.msg = msg;
		this.data = data;
	}

}
