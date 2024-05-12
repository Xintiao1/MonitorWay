package cn.mw.monitor.weixin.entity.message;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author bkc
 * @create 2020-06-30 23:04
 */
@XStreamAlias("Voice")
public class Voice {

    @XStreamAlias("MediaId")
    private String mediaId;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public Voice(String mediaId) {
        this.mediaId = mediaId;
    }

    @Override
    public String toString() {
        return "Voice{" +
                "mediaId='" + mediaId + '\'' +
                '}'+super.toString();
    }
}
