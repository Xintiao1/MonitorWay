package cn.mw.monitor.util.lucene;

import cn.mw.monitor.util.lucene.dto.LuceneFieldsDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SynonymQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.isEmpty;


/**
 * @author syt
 * @Date 2020/8/18 16:12
 * @Version 1.0
 */
@Slf4j
public class LuceneUtils {
    //Lucene索引库路径
    private static String luceneIndexUrl;
    private static Directory directory;

    static {
        CrmProperties properties = SpringContextUtils.getBean(CrmProperties.class);
        luceneIndexUrl = properties.getIndexUrl();
//        try {
//            directory = FSDirectory.open(Paths.get(luceneIndexUrl));
//        } catch (Exception e) {
//            log.error("fail to get FSDirectory.open(Paths.get(luceneIndexUrl)) cause:{}", e.getMessage());
//        }
    }
    /**
     * 中文分词器
     */
    private static SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
    /**
     * 创建索引写入配置
     */
//    private static IndexWriterConfig indexWriterConfig = new IndexWriterConfig(smartChineseAnalyzer);
    /**
     * 创建索引写入对象
     */
//    private static IndexWriter indexWriter;

    /**
     * 将indexReader维护成单例
     */
//    private static DirectoryReader indexReader;
//    private static IndexSearcher indexSearcher;

//    static {
//        try {
//            //在索引库没有建立并且没有索引文件的时候首先要commit一下让他建立一个索引库的版本信息
//            //如果第一次没有commit就打开一个索引读取器就会报异常
//            getIndexWriter();
//            indexWriter.commit();
//            indexReader = DirectoryReader.open(directory);
//            indexSearcher = new IndexSearcher(indexReader);
//        } catch (IOException e) {
//            log.error("fail to new indexSearcher(indexReader) cause:{}", e.getMessage());
//        }
////        } finally {
////            if (indexWriter != null) {
////                try {
////                    indexWriter.close();
////                } catch (Exception e) {
////                    log.error("fail to indexWriter.close(), cause:{}", e.getMessage());
////                }
////            }
////        }
//    }



    /**
     * 实例化indexWriter
     */
//    public static void getIndexWriter() {
//        if (indexWriter == null) {
//            try {
//                indexWriter = new IndexWriter(directory, indexWriterConfig);
//            } catch (Exception e) {
//                log.error("fail to new IndexWriter(directory,indexWriterConfig) cause:{}", e.getMessage());
//            }
//        }
//    }

    private static IndexManager indexManager = new IndexManager(luceneIndexUrl);
//    private static IndexSearcher indexSearcher = indexManager.getSearcher();
    private static IndexWriter indexWriter = indexManager.getWriter();
    /**
     * 根据参数获取相应的分好页的搜索内容
     *
     * @param typeIds     知产分类作为筛选条件
     * @param fieldValues 关键字作为搜索内容
     * @param start       当前页的起始条数
     * @param end         当前页的结束条数（不能包含）
     * @return
     * @throws Exception
     */
    public static List<Map> searchByTypeIds(List<Integer> typeIds, Map<String, String> fieldValues, int start, int end) throws Exception {
        IndexSearcher searcher = indexManager.getSearcher();

        BooleanQuery.Builder mustQuery = new BooleanQuery.Builder();
        if (typeIds.size() > 0) {
            SynonymQuery.Builder shouldQuery = new SynonymQuery.Builder("typeId");
            //遍历or条件
            typeIds.forEach(typeId -> {
                shouldQuery.addTerm(new Term("typeId", typeId.toString()));
            });
            mustQuery.add(shouldQuery.build(), BooleanClause.Occur.MUST);
        }
        //遍历其他and条件
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            //用QueryParse解析查询表达式
            QueryParser queryParser = new QueryParser(entry.getKey(), smartChineseAnalyzer);
            //进行自动转义特殊字符后再进行关键字的查询
            String escape = QueryParser.escape(entry.getValue());
            //搜索语句
            Query query = queryParser.parse(escape);
//            TermQuery query = new TermQuery(new Term(entry.getKey(), entry.getValue()));
            mustQuery.add(query, BooleanClause.Occur.MUST);
        }
        //搜索数据，查询0-end条
        List<Map> list = new ArrayList<>();
        int count = searcher.count(mustQuery.build());
        if (count == 0) {
            return list;
        } else {
            if (end > count) {
                end = count;
            }
            TopDocs topDocs = searcher.search(mustQuery.build(), end);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            for (int i = start; i < end; i++) {
                //获取文档编号
                int id = scoreDocs[i].doc;
                Document hitDoc = searcher.doc(id);
                Map map = new HashMap<>();
                map.put("id", hitDoc.get("id"));
                map.put("title", hitDoc.get("title"));
                map.put("total", count);
                list.add(map);
            }
            return list;
        }
    }

    /**
     * 根据单个field单个条件查询
     *
     * @param field
     * @param value
     * @param n     查询的索引个数
     * @return
     * @throws Exception
     */
    public static List<Map> search(String field, String value, Integer n) throws Exception {
        IndexSearcher searcher = indexManager.getSearcher();
        //用QueryParse解析查询表达式
        QueryParser queryParser = new QueryParser(field, smartChineseAnalyzer);
        //进行自动转义特殊字符后再进行关键字的查询
        String escape = QueryParser.escape(value);
        //搜索语句
        Query query = queryParser.parse(escape);
        //获取搜索结果，指定返回document返回的个数
        ScoreDoc[] hits = searcher.search(query, n).scoreDocs;
        List<Map> list = new ArrayList<>();
        for (int i = 0; i < hits.length; i++) {
            int id = hits[i].doc;
            Document hitDoc = searcher.doc(id);
            Map map = new HashMap<>();
            map.put("id", hitDoc.get("id"));
            map.put("title", hitDoc.get("title"));
            list.add(map);
        }
//        ireader.close();
//        directory.close();
        return list;
    }

    /**
     * 根据多个field多个个条件查询,field和value要一一对应
     *
     * @param fields
     * @param values
     * @param n      查询的索引个数
     * @return
     * @throws Exception
     */
    public static List<Map> searchByMultiple(String[] fields, String values, int n) throws Exception {
        IndexSearcher searcher = indexManager.getSearcher();
        //搜索
        BooleanClause.Occur[] flags = new BooleanClause.Occur[fields.length];
        for (int i = 0; i < fields.length; i++) {
            flags[i] = BooleanClause.Occur.SHOULD;
        }
        //进行自动转义特殊字符后再进行关键字的查询
        String escape = QueryParser.escape(values);
        //创建一个MultiFieldQueryParser对象
        Query query = MultiFieldQueryParser.parse(escape, fields, flags, smartChineseAnalyzer);
        //获取搜索结果，指定返回document返回的个数
        ScoreDoc[] hits = searcher.search(query, n).scoreDocs;
        List<Map> list = new ArrayList<>();
        for (int i = 0; i < hits.length; i++) {
            int id = hits[i].doc;
            Document hitDoc = searcher.doc(id);
            Map map = new HashMap<>();
            map.put("id", hitDoc.get("id"));
            map.put("title", hitDoc.get("title"));
            list.add(map);
        }
//        ireader.close();
//        directory.close();
        return list;
    }

    /**
     * 删除全部索引
     *
     * @throws Exception
     */
    public static void deleteAll() throws Exception {
//        获得indexWriter对象
//        getIndexWriter();
//        删除所有索引
        indexWriter.deleteAll();
        indexWriter.commit();
//            indexWriter.close();
    }

    /**
     * 根据条件删除索引
     *
     * @param luceneFieldsDTO
     * @throws Exception
     */
    public static void delete(LuceneFieldsDTO luceneFieldsDTO) throws Exception {
//        getIndexWriter();
        Field[] fields = luceneFieldsDTO.getClass().getDeclaredFields();
//        从luceneFieldsDTO对象中获取可用的筛选条件，找到需要删除的文件对象索引
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            Object value = getFieldValueByName(name, luceneFieldsDTO);
            if (!isEmpty(value) && name != null && !"".equals(name)) {
                Term term = new Term(name, value.toString());
                TermQuery termQuery = new TermQuery(term);
                indexWriter.deleteDocuments(termQuery);
//                当条件满足后就不用再循环
                break;
            }
        }
        indexWriter.commit();
//            indexWriter.close();
    }

    /**
     * 根据表格中主键ids 删除索引
     *
     * @param ids
     * @throws Exception
     */
    public static void delete(List<String> ids) throws Exception {
        if (ids.size() > 0) {
//            getIndexWriter();
            for (String id : ids) {
                Term term = new Term("id", id);
                Query termQuery = new TermQuery(term);
                indexWriter.deleteDocuments(termQuery);
            }
            indexWriter.commit();
//            indexWriter.close();
        }
    }

    /**
     * 创建文件对象索引
     *
     * @param luceneFieldsDTO
     * @throws Exception
     */
    public static void createIndex(LuceneFieldsDTO luceneFieldsDTO) throws Exception {
//        创建文件对象
        Document document = new Document();
//        访问类中的所有字段
        Field[] fields = luceneFieldsDTO.getClass().getDeclaredFields();
//        将需要索引的字段加入document对象中
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            Object value = getFieldValueByName(name, luceneFieldsDTO);
            if (value != null) {
                //索引库中只存知识库id,title
                if ("id".equals(name) || "title".equals(name) || "typeId".equals(name)) {
                    if ("title".equals(name)) {
                        document.add(new TextField(name, value.toString(), Store.YES));
                    } else {
                        document.add(new StringField(name, value.toString(), Store.YES));
                    }
                } else {
                    document.add(new TextField(name, value.toString(), Store.NO));
                }
            } else {
                continue;
            }
        }
//        getIndexWriter();
//        创建对应上面document对象的索引文件
        indexWriter.addDocument(document);
        indexWriter.commit();
////        释放资源
//        indexWriter.close();
    }

    /**
     * 修改文件对象索引
     *
     * @param luceneFieldsDTO
     * @throws Exception
     */
    public static void updateIndex(LuceneFieldsDTO luceneFieldsDTO) throws Exception {
//        创建文件对象
        Document document = new Document();
//        访问类中的所有字段
        Field[] fields = luceneFieldsDTO.getClass().getDeclaredFields();
        Term term = null;
//        将需要修改的索引的字段加入document对象中
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            Object value = getFieldValueByName(name, luceneFieldsDTO);
            if (value != null) {
                if ("id".equals(name)) {
                    term = new Term(name, value.toString());
                }
                //索引库中只存知识库id,title
                if ("id".equals(name) || "title".equals(name) || "typeId".equals(name)) {
                    if ("title".equals(name)) {
                        document.add(new TextField(name, value.toString(), Store.YES));
                    } else {
                        document.add(new StringField(name, value.toString(), Store.YES));
                    }
                } else {
                    document.add(new TextField(name, value.toString(), Store.NO));
                }
            } else {
                continue;
            }
        }
//        getIndexWriter();
//        修改对应上面document对象的索引文件
        indexWriter.updateDocument(term, document);
        indexWriter.commit();
////        释放资源
//        indexWriter.close();
    }

    /**
     * 根据属性名获取属性值
     *
     * @param fieldName
     * @param o
     * @return
     */
    private static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(o, new Object[]{});
            return value;
        } catch (Exception e) {
            log.error("fail to getFieldValueByName with fieldName={}, cause:{}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * 清空回收站，强制优化
     */
    public static void forceDelete() {
        try {
            indexWriter.forceMergeDeletes();
        } catch (Exception e) {
            log.error("fail to indexWriter.forceMergeDeletes(), cause:{}", e.getMessage());
        } finally {
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                } catch (Exception e) {
                    log.error("fail to indexWriter.close(), cause:{}", e.getMessage());
                }
            }
        }
    }
}
