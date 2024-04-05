package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf()
                .setAppName("Example")
                .setMaster("local[*]")
                .set("es.nodes", "localhost:9200");

        try (JavaSparkContext sparkContext = new JavaSparkContext(sparkConf)) {
            List<Map<String, String>> data = Arrays.asList(
                    Map.of("propertyA", "valueA1", "propertyB", "valueB1"),
                    Map.of("propertyA", "valueA2", "propertyB", "valueB2"),
                    Map.of("propertyA", "valueA3", "propertyB", "valueB3")
            );
            JavaRDD<Map<String, String>> stringJavaRDD = sparkContext.parallelize(data);

            JavaEsSpark.saveToEs(stringJavaRDD, "test-index");
        }
    }
}