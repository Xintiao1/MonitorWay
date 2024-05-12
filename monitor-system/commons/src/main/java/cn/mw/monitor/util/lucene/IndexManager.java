package cn.mw.monitor.util.lucene;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author syt
 * @Date 2020/11/13 14:55
 * @Version 1.0
 */
@Slf4j
public class IndexManager {
    /**
     * 中文分词器
     */
    private static SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
    /**
     * writer公共配置
     */
    private static final IndexWriterConfig iwc = new IndexWriterConfig(smartChineseAnalyzer);

    static {
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        iwc.setRAMBufferSizeMB(20.0);
        iwc.setMaxBufferedDocs(10000);
    }

    private Directory dir;

    private IndexReader reader;

    private IndexSearcher searcher;

    private IndexWriter writer;

    /**
     * 构造函数
     */
    public IndexManager(String indexPath) {
        this(new File(indexPath));
    }

    private IndexManager(File indexDir) {
        init(indexDir);
    }

    /**
     * 初始化方法
     */
    private void init(File indexDir) {
        try {
            /**
             * Directory 初始化
             */
            this.dir = FSDirectory.open(Paths.get(indexDir.getPath()));

            /**
             * IndexWriter 初始化
             */
            this.writer = new IndexWriter(this.dir, IndexManager.iwc);

            this.commitWriter();

            /**
             * IndexReader 初始化
             */
            ReaderManager.getInstance().createIndexReader(dir);
            this.reader = ReaderManager.getInstance().getIndexReader(dir);
            /**
             * IndexSearcher 初始化
             */
            this.searcher = new IndexSearcher(this.reader);

        } catch (IOException e) {
            log.error("fail to init(File indexDir) cause:{}", e);
            throw new RuntimeException(e);
        }
    }

    public IndexWriter getWriter() {
        return this.writer;
    }

    public void commitWriter() {
        try {
            writer.commit();
        } catch (IOException e) {
            this.rollback();
        }
    }

    private void rollback() {
        try {
            writer.rollback();
        } catch (IOException e) {
            log.error("fail to rollback() cause:{}", e);
        }
    }

    public IndexSearcher getSearcher() throws IOException {
        if (this.reader == null) {
            this.reader = DirectoryReader.open(dir);
        } else {
            IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) reader);
            if (newReader != null) {
                this.reader.close();
                this.reader = newReader;
            }
        }
        return new IndexSearcher(this.reader);
    }

}
