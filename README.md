# EsperService API

## Data Sources

### Add data source: 
By adding a data source, an input adapter is instantiated, which extracts data from this data source and forwards this data to the Esper engine.
     
POST /EsperService/datasources HTTP/1.1
Content-Type: application/json
Accept: application/json
```javascript

{"protocol": "MQTT", "endpoint":"tcp://192.168.209.190:1883", "topics":["TempEvent"]}
```
HTTP/1.1 201 CREATED
Content-Type: application/json
{"datasource_id": "", "status": ""}


### Get data sources: 
GET /EsperService/datasources HTTP/1.1


### Get data source by id
GET /EsperService/datasources/{datasource_id} HTTP/1.1

### Remove data source
DELETE /EsperService/datasources/{datasource_id} HTTP/1.1

## Event Management

### Add event type
POST /EsperService/event/types HTTP/1.1
Content-Type: application/json
Accept: application/json
{"eventtype": "create schema TempEvent(sensorID string, temperature double)"}

HTTP/1.1 200 OK
Content-Type: application/json
{"eventtype_id": "stmt_1", "status": "running"}

### Get event types
GET /EsperService/event/types HTTP/1.1
Accept: application/json

HTTP/1.1 200 OK
Content-Type: application/json
[{"eventtype_id":"TempEvent","properties":{"temperature":"Double","sensorID":"String"}}]


### Send event to the Esper engine
POST /EsperService/event HTTP/1.1
Content-Type: application/json
{"TempEvent": {"sensorID": "A0", "timestamp": "", "temperature": "9"}}

## Continuous Queries

### Create and start a continuous query
POST /EsperService/queries HTTP/1.1
{"query": "select * from TempEvent(sensorID='A0')"} 
{"query": "select * from TempEvent(sensorID='A0')", "subscriber": {"protocol": "MQTT", "endpoint":"tcp://192.168.209.190:1883", "topics":["situation"]}}

  returns: the query_id

### Get all continuous queries
GET /EsperService/queries

### Stop a continuous query
 POST /EsperService/queries/{query_id}/stop

### Start a continuous query
POST /EsperService/queries/{query_id}/start

### Add subscriber to a continuous query
POST /EsperService/queries/{query_id}/subscriptions
Content-Type: application/json
{"protocol": "MQTT", "endpoint":"tcp://192.168.209.190:1883", "topics":["situation"]} 

### Get subscribers of a continuous query
POST /EsperService/queries/{query_id}/subscriptions

### Remove subscriber of a continuous query 
DELETE /EsperService/queries/{query_id}/subscriptions/{subscriber_id}

### Delete a continuous query
DELETE /EsperService/queries/{query_id}

