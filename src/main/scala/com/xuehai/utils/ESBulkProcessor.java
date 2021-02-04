//package com.xuehai.utils;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import org.elasticsearch.action.bulk.BackoffPolicy;
//import org.elasticsearch.action.bulk.BulkProcessor;
//import org.elasticsearch.action.bulk.BulkRequest;
//import org.elasticsearch.action.bulk.BulkResponse;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.action.update.UpdateRequest;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.common.transport.TransportAddress;
//import org.elasticsearch.common.unit.ByteSizeUnit;
//import org.elasticsearch.common.unit.ByteSizeValue;
//import org.elasticsearch.common.unit.TimeValue;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//public class ESBulkProcessor {
//
//    private static final Logger logger = LoggerFactory.getLogger(ESBulkProcessor.class);
//
//    private static final String HOST = "192.168.5.85";
//    private static final int PORT = 9300;
//    private static final String CLUSTER_NAME = "xh-es-cluster";
//    //private TransportClient client;
//
//
//    /**
//     * @return org.elasticsearch.action.bulk.BulkProcessor
//     * @description: BulkProcessor实例的创建
//     */
//    public BulkProcessor bulkProcessor() throws UnknownHostException {
//
//
//        // 设置集群名称
//        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME).build();
//
//        RestHighLevelClient client = ESUtils.getHighClient();
//
//
//        return BulkProcessor.builder(
//                restHighLevelClient::bulkAsync,
//                new BulkProcessor.Listener() {
//                    @Override
//                    public void beforeBulk(long executionId,
//                                           BulkRequest request) {
//                        logger.info("序号：{} ，开始执行 {} 条数据批量操作。", executionId, request.numberOfActions());
//                    }
//
//                    @Override
//                    public void afterBulk(long executionId,
//                                          BulkRequest request,
//                                          BulkResponse response) {
//                        // 在每次执行BulkRequest后调用，通过此方法可以获取BulkResponse是否包含错误
//                        if (response.hasFailures()) {
//                            logger.error("Bulk {} executed with failures", executionId);
//                        } else {
//                            logger.info("序号：{} ，执行 {} 条数据批量操作成功，共耗费{}毫秒。", executionId, request.numberOfActions(), response.getTook().getMillis());
//                            logger.info("当前时间是" + new Date());
//                        }
//                    }
//
//                    @Override
//                    public void afterBulk(long executionId,
//                                          BulkRequest request,
//                                          Throwable failure) {
//                        logger.error("序号：{} 批量操作失败，总记录数：{} ，报错信息为：{}", executionId, request.numberOfActions(), failure.getMessage());
//                    }
//                })
//
//                // 每添加1000个request，执行一次bulk操作
//                .setBulkActions(2000)
//                // 每达到5M的请求size时，执行一次bulk操作
//                .setBulkSize(new ByteSizeValue(2, ByteSizeUnit.MB))
//                // 每5s执行一次bulk操作
//                .setFlushInterval(TimeValue.timeValueSeconds(2))
//                // 设置并发请求数。默认是1，表示允许执行1个并发请求，积累bulk requests和发送bulk是异步的，其数值表示发送bulk的并发线程数（可以为2、3、...）；若设置为0表示二者同步。
//                .setConcurrentRequests(3)
//                // 最大重试次数为3次，启动延迟为100ms。
//                .setBackoffPolicy(
//                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
//                .build();
//    }
//
//
//    public static void main(String[] args) throws InterruptedException, UnknownHostException {
//        ESBulkProcessor esBulkProcessor = new ESBulkProcessor();
//        BulkProcessor esBulk = esBulkProcessor.bulkProcessor();
//
////        Map<String, Object> map = new HashMap<>();
////        map.put("er",20);
////        map.put("na4me",240);
////        map.put("er534",40);
////        map.put("you","how");
////
////      UpdateRequest doc = new UpdateRequest("coprocesser964", "_doc", "9").doc(map);
////        doc.upsert(new IndexRequest("coprocesser964", "_doc", String.valueOf(9)).source(map));
////        esBulk.add(doc);
////        esBulk.flush();
//
//        // System.out.println("开始时间是："+new Date());
//
//        for (int i = 0; i < 1000000; i++) {
//
//            JSONObject jsonObject = JSON.parseObject("{}");
//            jsonObject.put("reason","hello");
//            jsonObject.put("reason1","hello1");
//            jsonObject.put("reason2","hello2");
//
//            esBulk.add(new IndexRequest("es_json1", "_wlk", String.valueOf(i)).source(jsonObject));
//
//
//        }
//
//
//        try {
//            esBulk.awaitClose(30, TimeUnit.SECONDS);
//        } catch (InterruptedException ee) {
//            ee.printStackTrace();
//        }
//        // esBulk.awaitClose(30, TimeUnit.SECONDS);
//        //}
//        //}
////
////        try
////        {
////            Thread.sleep(5000);
////        }
////        catch (InterruptedException e)
////        {
////            e.printStackTrace();
////        }
//
//        // }
//        //}
//        // 30秒后关闭BulkProcessor
////        try {
////            esBulk.awaitClose(50, TimeUnit.SECONDS);
////        } catch (InterruptedException ee) {
////            ee.printStackTrace();
////        }
//
//        // 设置更新内容
//        //request.doc(jsonMap);
//        //esBulk.add(request);
//        // esBulk.flush();
//
//
//        // esBulkProcessor.mapData();
//        //System.out.println("是："+new Date());
//        //     esBulkProcessor.jsonData();
//        //    esBulkProcessor.bulkDelete();
//        //  esBulk.add(new DeleteRequest("wrong_topic_stream_2", "_doc", String.valueOf("主键错误")));
//        //     esBulk.flush();
//    }
//}