# Magnolia event-driven

Usage of event-driven pattern to decouple Magnolia CMS from its consumers. 

This repository is related with [Implementing Event-driven architecture in Magnolia CMS using Kafka](https://medium.com/@joaquin.alfaro/implementing-event-driven-in-magnolia-cms-using-kafka-e9722eb43e7a)
### Features
The publication of contents generates events that are enqueued in Kafka.
This feature is combined with *REST Content Deliverty API* because it enqueues the same resources exposed by the endpoints configured in *REST Content Deliverty API*


For example if Magnolia stores *web pages*, *factsheet of shoes* and *description of stores* and that just *shoes* and *stores* are exposed in *REST content delivery*, then publication of *web pages* won't be sent to Kafka because *web pages* does not have an endpoint configured in REST content Delivery.


### Installation
1. Install Zookeeper and Apache Kafka. https://kafka.apache.org/quickstart  

2. Configure the host of Apache Kafka in *config:magnolia-event-driven-service/config@mq_server*. For example *localhost:9092*

3. Build and install magnolia-event-driven-service module.

````
$ cd magnolia-event-driven-service
$ mvn install
````

4.Add dependency with magnolia-event-driven-service in the bundle of Magnolia author.

```
<dependency>
    <groupId>com.formentor</groupId>
    <artifactId>magnolia-event-driven-service</artifactId>
    <version>${magnolia-event-driven.version}</version>
</dependency>
```
5.Use the command *eventdriven-publishAndEnqueue* for publication  

### Commands  

##### eventdriven-enqueue
Enqueue in Kafka the contents of a JCR Node of Magnolia

Parameters:  
> **repository**: name of the workspace of the Node  

> **path**: location of the Node

Example:  
~~~~
cm = info.magnolia.commands.CommandsManager.getInstance()
command = cm.getCommand('eventdriven', 'enqueue')
command.setRepository('tours')
command.setPath('/magnolia-travels/Vietnam--Tradition-and-Today')
command.execute(ctx)
~~~~

##### eventdriven-publishAndEnqueue  
It is a chained command composed by *default-publish* and *eventdriven-enqueue* commands. It is the command to be used for the publication action.

```
eventdriven:
  publishAndEnqueue:
    publish:
      class: info.magnolia.commands.DelegateCommand
      commandName: default-publish
    enqueue:
      class: com.formentor.magnolia.async.command.PublishingDeliveryCommand
      enabled: 'true'
```

### Testing
1. Publish contents of a Node whose node-type and workspace is configured as endpoint in REST Content Delivery  
2. Query the Node from REST Content Deliver API
```
$ curl -X GET \
  http://localhost:8080/magnolia-formentor-modules/.rest/tours/endpoint/magnolia-travels/Vietnam--Tradition-and-Today \
  -H 'Authorization: Basic c3VwZXJ1c2VyOnN1cGVydXNlcg=='
```  
3.Get the last message of the topic with the same name as the endpoint
```
$ bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic tours_endpoint --from-beginning
```
4.The payload of the message will be the same as the reponse of the REST API.
Please take note that the name of the topic replaces "/" of the endpoint with "_"
 
**NOTE**
> The name of the topic is the same as the REST Content Delivery


## License

MIT

## Contributors

Joaquín Alfaro, @Joaquin_Alfaro

Formentor Studio, http://formentor-studio.com/