package cn.mw.monitor.weixin.entity.message;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;
@XStreamAlias("xml")
public class VoiceMessage extends BaseMessage{

	private Voice voice;

	public Voice getVoice() {
		return voice;
	}

	public void setVoice(Voice voice) {
		this.voice = voice;
	}

	public VoiceMessage(Map<String, String> requestMap, Voice voice) {
		super(requestMap);
		this.setMsgType("voice");
		this.voice = voice;
	}

	@Override
	public String toString() {
		return "VoiceMessage{" +
				"voice=" + voice +
				'}'+super.toString();
	}
}
