package com.javablog.elasticsearch.document.impl;

import com.alibaba.fastjson.JSON;
import com.javablog.elasticsearch.document.DocService;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service("docService")
public class DocServiceImpl implements DocService {
    private final static Logger log = LoggerFactory.getLogger(DocServiceImpl.class);
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean add(String index, String json) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index);
        indexRequest.source(json, XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        log.info("add: " + JSON.toJSONString(indexResponse));
        int status = indexResponse.status().getStatus();
        if (status == 200) {
            return true;
        }
        return false;
    }


    @Override
    public void add(String index, String json, String id) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index).id(id);
        indexRequest.source(json, XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        log.info("add: " + JSON.toJSONString(indexResponse));
    }

    /**
     * 删除
     *
     * @param indexName 索引名称
     * @param docId     文档ID
     * @throws IOException
     */
    @Override
    public void deleteDocWithId(String indexName, String docId) throws IOException {
        DeleteRequest request = new DeleteRequest(
                indexName,
                docId);
        DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.status().getStatus());
        System.out.println(response + "" + response.getResult());
    }

    @Override
    public void refresh(String indexName) throws IOException {
        RefreshRequest refreshRequest = new RefreshRequest(indexName);
        RefreshResponse refresh = restHighLevelClient.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
        log.info(refresh.toString());
    }

    @Override
    public long countDoc(String indexName) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        System.out.println(hits.getTotalHits());
        return searchResponse.getHits().getTotalHits().value;
    }

    @Override
    public UpdateResponse update(String index, Map<String, Object> map, String id) throws IOException {
        UpdateRequest request = new UpdateRequest(index, id).doc(map);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        log.info("update: " + JSON.toJSONString(updateResponse));
        return updateResponse;
    }

    /**
     * 批量增加与修改
     *
     * @param indexName   索引名称
     * @throws IOException
     */
    @Override
    public BulkResponse bulkUpdateOrInsertDoc(String indexName, BulkRequest builder) throws IOException {
        BulkResponse bulkResponse = restHighLevelClient.bulk(builder, RequestOptions.DEFAULT);
        return bulkResponse;
    }

    /**
     * 批量删除
     *
     * @param indexName 索引名称
     * @param docIdArr  _ID数组
     * @throws IOException
     */
    @Override
    public void bulkDeleteDoc(String indexName, String[] docIdArr) throws IOException {
        BulkRequest bulkRequestBuilder = new BulkRequest();
        for (String docId : docIdArr) {
            bulkRequestBuilder.add(new DeleteRequest(indexName, docId));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequestBuilder, RequestOptions.DEFAULT);
        log.info(bulkResponse.toString());
    }

    /**
     * 删除查询的数据
     *
     * @param indexName  索引名称
     * @param fieldName  查询字段名称
     * @param fieldValue 查询字段值
     * @throws IOException
     */
    @Override
    public void deleteByQuery(String indexName, String fieldName, String fieldValue) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(indexName).setQuery(QueryBuilders.termQuery(fieldName, fieldValue));
        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        log.info(bulkByScrollResponse.toString());
    }
}
