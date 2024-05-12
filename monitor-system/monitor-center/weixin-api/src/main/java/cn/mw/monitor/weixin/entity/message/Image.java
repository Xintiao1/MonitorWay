package cn.mw.monitor.weixin.entity.message;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author bkc
 * @create 2020-06-30 22:43
 */
@XStreamAlias("Image")
public class Image {
    @XStreamAlias("MediaId")
    private String mediaId;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public Image(String mediaId) {
        this.mediaId = mediaId;
    }

    @Override
    public String toString() {
        return "Image{" +
                "mediaId='" + mediaId + '\'' +
                '}';
    }
}
