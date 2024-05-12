package cn.mw.monitor.knowledgeBase.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * @author syt
 * @Date 2020/8/27 15:12
 * @Version 1.0
 */
@Slf4j
public class LuceneFactory {

    private static DirectoryReader indexReader = null;
    private static IndexSearcher indexSearcher = null;

    public static DirectoryReader getIndexReader(IndexWriter indexWriter) {
        synchronized (Object.class) {
            if (indexReader == null) {
                synchronized (Object.class) {
                    if (indexReader == null) {
                        try {
                            indexReader = DirectoryReader.open(indexWriter);
                        } catch (IOException e) {
                            log.error("fail to getIndexReader with indexWriter={}, cause:{}", indexWriter, e.getMessage());
                        }
                    } else {
                        try {
                            DirectoryReader reader = DirectoryReader.openIfChanged(indexReader);
                            indexReader.close();
                            indexReader = reader;
                        } catch (IOException e) {
                            log.error("fail to getIndexReader with indexWriter={}, cause:{}", indexWriter, e.getMessage());
                        }
                    }
                }
            }
        }
        return indexReader;
    }

    public static IndexSearcher getIndexSearcher(IndexWriter indexWriter) {
        synchronized (Object.class) {
            if (indexSearcher == null) {
                synchronized (Object.class) {
                    if (indexSearcher == null) {
                        DirectoryReader indexReader = LuceneFactory.getIndexReader(indexWriter);
                        indexSearcher = new IndexSearcher(indexReader);
                    } else {
                        try {
                            DirectoryReader directoryReader = DirectoryReader.openIfChanged(LuceneFactory.getIndexReader(indexWriter));
                            indexSearcher = new IndexSearcher(directoryReader);
                        } catch (IOException e) {
                            log.error("fail to getIndexSearcher with indexWriter={}, cause:{}", indexWriter, e.getMessage());
                        }
                    }
                }
            }
        }
        return indexSearcher;
    }
}
