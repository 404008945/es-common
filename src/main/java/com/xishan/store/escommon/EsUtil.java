package com.xishan.store.escommon;

import com.alibaba.fastjson.JSON;
import com.xishan.store.escommon.model.Bulk;
import com.xishan.store.escommon.page.EsPage;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EsUtil {


    private RestHighLevelClient client;

    /**
     * 批量插入数据
     * @param indexName
     * @param bulks
     * @return
     */
    public BulkResponse bulkIndex(String indexName, List<Bulk> bulks) {
        try{
            BulkRequest bulkRequest = new BulkRequest();
            IndexRequest request = null;
            for(Bulk bulk: bulks) {
                request = new IndexRequest("post");
                request.index(indexName).id(String.valueOf(bulk.getId())).source(JSON.toJSONString(bulk.getData()), XContentType.JSON);
                bulkRequest.add(request);
            }
            BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            return response;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 更新一条数据
     */
    public IndexResponse index(String indexName, Bulk bulk){
        try{
            IndexRequest request = new IndexRequest("post");
            request.index(indexName).id(String.valueOf(bulk.getId())).source(JSON.toJSONString(bulk.getData()), XContentType.JSON);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            return response;
        }catch (Exception e){
           throw new RuntimeException(e.getMessage());
        }
    }


    public SearchResponse search(String indexName, BoolQueryBuilder boolQueryBuilder, int pageNo, int pageSize) {
        try {

            SearchRequest searchRequest = new SearchRequest(indexName);

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.size(pageSize);
            sourceBuilder.from(pageNo - 1);
            sourceBuilder.query(boolQueryBuilder);
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            searchResponse.getHits().forEach(message -> {
                try {
                    String sourceAsString = message.getSourceAsString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return searchResponse;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't get Detail");
        }
    }

    //再来一个根据id 查找的
    public GetResponse searchById(String indexName, String id) {
        try {
            GetRequest getRequest = new GetRequest(indexName).id(id);
            return client.get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't get Detail:",e);
        }
    }

    public <T> T getById(String indexName, String id, Class clzz) {
        GetResponse getResponse = searchById(indexName,id);
        if(getResponse.getSourceAsString() == null){
            return null;
        }
        return (T) JSON.parseObject(getResponse.getSourceAsString(), clzz);
    }


    public <T> EsPage<T> paging(String indexName, BoolQueryBuilder boolQueryBuilder, int pageNo, int pageSize, Class clzz) {
        SearchResponse response = search(indexName, boolQueryBuilder, pageNo, pageSize);
        //组装分页数据
        EsPage page = new EsPage<T>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setTotal(response.getHits().getTotalHits().value);
        //利用fastJson进行转换
        if (response.getHits().getHits() == null) {
            return page;
        }
        List<SearchHit> list = Arrays.asList(response.getHits().getHits());
        List<T> data = new ArrayList<>();
        list.forEach(it -> data.add((T) JSON.parseObject(it.getSourceAsString(), clzz)));
        page.setData(data);
        System.out.println(page);
        return page;
    }

    public boolean indexExists(String indexName) {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            return  client.indices().exists(getIndexRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public DeleteResponse delete(String indexName, String id) {
        try {
            DeleteRequest getIndexRequest = new DeleteRequest(indexName).id(id);
            return  client.delete(getIndexRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }
}
