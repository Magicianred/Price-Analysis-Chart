## Objective
It is a price analysis chart implementation based on the one [cimri.com](https://www.cimri.com/) employs for products as seen below. 

![alt text](https://github.com/mrabiabrn/shops-prices-table/blob/main/priceanalysis.PNG)

In our implementation, the price analysis chart shows the **minimum prices per store** instead of the minimum price overall.

## Project Structure
![alt text](https://github.com/mrabiabrn/shops-prices-table/blob/main/project-overview.PNG)


## Requirements
All the tools employed are set up via **docker-compose.yml** file. Thus, 
- Docker

## Running The Project
- Run Apache Kafka

```
run kafka: docker-compose up -d kafka1 
```

- Create a topic named **new-topic** in Kafka
```
docker exec -it resources_kafka1_1 kafka-topics --zookeeper zoo1:2181 --create --topic new-topic --partitions 1 --replication-factor 1 
```
NOTE: When Kafka is interrupted when consumers are consuming, we need to reset offsets with the command below
```
docker exec -it resources_kafka1_1 kafka-consumer-groups --bootstrap-server localhost:9092 --group consumer-consumer-1 --topic new-topic --reset-offsets --to-earliest --execute
```

- Run Cassandra

```
docker-compose up -d cassandra  
```
- Create Keyspace and a Table in Cassandra
```
docker exec -it cassandra bash  
cqlsh  
create keyspace "cimri" with replication={'class' : 'SimpleStrategy', 'replication_factor':2};   
use cimri;   
create table datePrice(url text, merchanturl text,date timestamp, price double,PRIMARY KEY(url,merchanturl,date));  
```
- Run PostgreSQL
```
docker-compose up -d database
```
- Set Up PostgreSQL
```
docker-compose run database bash    
psql --host=database --username=cimri --dbname=database  
CREATE TABLE producturl(url TEXT PRIMARY KEY,title TEXT);
```
*To change settings, you can edit the docker-compose.yml file as needed.*
