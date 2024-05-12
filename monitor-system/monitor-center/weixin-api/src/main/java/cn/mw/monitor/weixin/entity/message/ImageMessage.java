package cn.mw.monitor.weixin.entity.message;

import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
@XStreamAlias("xml")
public class ImageMessage extends BaseMessage{

	private Image image;

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public ImageMessage(Map<String, String> requestMap, Image image) {
		super(requestMap);
		setMsgType("image");
		this.image = image;
	}

	@Override
	public String toString() {
		return "ImageMessage{" +
				"image=" + image +
				'}'+super.toString();
	}
}
