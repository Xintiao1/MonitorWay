package cn.mw.monitor.weixin.entity.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xml")
public class NewsMessage extends BaseMessage {

    @XStreamAlias("ArticleCount")
    private String articleCount;

    @XStreamAlias("Articles")
    private List<Article> articles = new ArrayList<>();

    public String getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(String articleCount) {
        this.articleCount = articleCount;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public NewsMessage(Map<String, String> requestMap, int articleCount, List<Article> articles) {
        super(requestMap);
        setMsgType("news");
        this.articleCount = articleCount+"";
        this.articles = articles;
    }

    @Override
    public String toString() {
        return "NewsMessage{" +
                "articleCount='" + articleCount + '\'' +
                ", articles=" + articles +
                '}'+super.toString();
    }
}
