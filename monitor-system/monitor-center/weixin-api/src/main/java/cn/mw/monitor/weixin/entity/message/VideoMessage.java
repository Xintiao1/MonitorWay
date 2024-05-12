package cn.mw.monitor.weixin.entity.message;

import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xml")
public class VideoMessage extends BaseMessage {

	private Video video;

	public Video getVideo() {
		return video;
	}

	public void setVideo(Video video) {
		this.video = video;
	}

	public VideoMessage(Map<String, String> requestMap, Video video) {
		super(requestMap);
		this.video = video;
	}
}
