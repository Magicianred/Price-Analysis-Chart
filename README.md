run kafka: docker-compose up -d kafka1   

create kafka topic: docker exec -it resources_kafka1_1 kafka-topics --zookeeper zoo1:2181 --create --topic new-topic --partitions 1 --replication-factor 1  

TO RESET OFFSETS: docker exec -it resources_kafka1_1 kafka-consumer-groups --bootstrap-server localhost:9092 --group consumer-consumer-1 --topic new-topic --reset-offsets --to-earliest --execute

run cassandra: docker-compose up -d cassandra  

docker exec -it cassandra bash  
cqlsh  
create keyspace "cimri" with replication={'class' : 'SimpleStrategy', 'replication_factor':2};   
use cimri;   
create table datePrice(url text, merchanturl text,date timestamp, price double,PRIMARY KEY(url,merchanturl,date));  

docker-compose up -d database
docker-compose run database bash    
psql --host=database --username=cimri --dbname=database  
CREATE TABLE producturl(url TEXT PRIMARY KEY,title TEXT);