# Purpose

The purpose of this repository is to reproduce a possible issue found while
trying to save data from Apache Spark into Elasticsearch, when the index in Elasticsearch
does not exist and `elasticsearch-spark` library is automatically creating it before
saving data to it.

# Prerequisites

 - new, empty Elasticsearch cluster in version 8.6.0 or newer

## Example - setup Elasticsearch using Docker

```bash
docker run \
  --name es8-12 \
  --network host \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -it \
  -m 1GB \
  docker.elastic.co/elasticsearch/elasticsearch:8.12.2
```
The above command uses host network to allow `elasticsearch-spark` easily connect
to the underlying Elasticsearch node directly.

# Issue description

With Elasticsearch running, to reproduce the issue, just run this project using
`./gradlew run` command. Most of the time, with Elasticsearch 8.6.0 and above, you will
get the following exception:

```
org.elasticsearch.hadoop.EsHadoopIllegalArgumentException: Cannot determine write shards for [test-index]; likely its format is incorrect (maybe it contains illegal characters? or all shards failed?)
        at org.elasticsearch.hadoop.util.Assert.isTrue(Assert.java:60)
        at org.elasticsearch.hadoop.rest.RestService.initSingleIndex(RestService.java:689)
        at org.elasticsearch.hadoop.rest.RestService.createWriter(RestService.java:634)
        at org.elasticsearch.spark.rdd.EsRDDWriter.write(EsRDDWriter.scala:71)
        at org.elasticsearch.spark.rdd.EsSpark$.$anonfun$doSaveToEs$1(EsSpark.scala:108)
        at org.elasticsearch.spark.rdd.EsSpark$.$anonfun$doSaveToEs$1$adapted(EsSpark.scala:108)
        at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:90)
        at org.apache.spark.scheduler.Task.run(Task.scala:136)
        at org.apache.spark.executor.Executor$TaskRunner.$anonfun$run$3(Executor.scala:548)
        at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1504)
        at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:551)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
        at java.base/java.lang.Thread.run(Thread.java:840)
```
Subsequent run of this application will finish successfully.

After some debugging, it looks like Elasticsearch cluster, despite returning information that
given index exists, for some short period of time (which is long enough to allow different threads
to execute), returns empty list of shards for given index.

Subsequent run is successful because the index already exists, created as part of the first run.

On Elasticsearch versions earlier than 8.6.0 the same scenario seems to work without any issue.
