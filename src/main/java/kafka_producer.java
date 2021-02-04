
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by root on 2019/7/5 0005.
 */
public class kafka_producer {
    public static String topic = "mongo-kafka";
    public static String brokerList = "192.168.5.85:9092,192.168.5.86:9092,192.168.5.87:9092";
    public static List<Boolean> victory = new ArrayList<Boolean>();

    public static void main(String[] args) throws InterruptedException {
//        kafka_producer();
        kafka_test();
    }

    public static void kafka_test() throws InterruptedException {
        Properties props = new Properties();

        props.put("bootstrap.servers", brokerList);
        props.put("acks", "1");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(props);

        for(int i=0;i<2;i++){
            String value1 = i + "";
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, value1);
            producer.send(record);

            //Thread.sleep(60*1000);
        }

        producer.close();
    }

    public static void kafka_producer(){
        Properties props = new Properties();

        props.put("bootstrap.servers", brokerList);
        props.put("acks", "1");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(props);

        victory.add(0, false);
        victory.add(1, true);

        for(int i=0; i<1; i++){
            JSONObject json = JSONObject.parseObject("{}");
            // {"season": 1,"gameId":2,"gradeId":91,"schoolId":2,"classIds":[1,2],"userId":7,"subjectId":1,"star":1,"victory":1,"time":1563811200000}
//            json.put("gameId", (int)(Math.random()*10)%2 + 1);
//            json.put("gradeId", (int)(Math.random()*10)%3 + 100);
//            json.put("schoolId", (int)(Math.random()*10)%3 + 100);
//            json.put("classId", (int)(Math.random()*10)%5 + 100);
//            json.put("userId", (int)(Math.random()*10)%10 + 100);
//            json.put("victory", victory.get((int)(Math.random()*10)%2));
//            json.put("subjectId", (int)(Math.random()*10)%3+1);
//            json.put("star", (int)(Math.random()*10)%7-2);
            json.put("season", 2);
            json.put("gameId", "id4");
            json.put("gradeId", 7);
            json.put("schoolId", 102);
            json.put("classIds", "[11,34,12]");
            json.put("userId", (int)(Math.random()*10)%3 + 1);
            json.put("victory", 2);
            json.put("subjectId", 12);

            json.put("star", -1);
            json.put("time", System.currentTimeMillis());

            System.out.println(json.toJSONString());

            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, 2, "", json.toJSONString());
            producer.send(record);
        }

        producer.close();
    }

}
