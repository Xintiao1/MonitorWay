package cn.mw.monitor.weixin.entity.message;

import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class BaseMessage {

	//接受方账号    当是接受信息时：开发者微信号，也就自己
	@XStreamAlias("ToUserName")
	private String toUserName;

	//发送方帐号   当是接受信息时：关注的用户，（一个OpenID）理解为用户在微信服务器里面存的唯一标识，不是我们看见的微信名
	@XStreamAlias("FromUserName")
	private String fromUserName;

	@XStreamAlias("CreateTime")
	private String createTime;

	//信息类型，一共六种
	@XStreamAlias("MsgType")
	private String msgType;

	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public BaseMessage(Map<String, String> requestMap) {
		this.toUserName=requestMap.get("FromUserName");
		this.fromUserName=requestMap.get("ToUserName");
		this.createTime=System.currentTimeMillis()/1000+"";
	}

	@Override
	public String toString() {
		return "BaseMessage{" +
				"toUserName='" + toUserName + '\'' +
				", fromUserName='" + fromUserName + '\'' +
				", createTime='" + createTime + '\'' +
				", msgType='" + msgType + '\'' +
				'}';
	}
}
