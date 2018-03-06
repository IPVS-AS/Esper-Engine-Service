# EsperService

This project provides a wrapper API for the CEP Engine [Esper](http://www.espertech.com/esper). To use this wrapper, run mvn install to generate a .war file, which can be deployed into the application server [Tomcat8](http://tomcat.apache.org/tomcat-8.5-doc/).  

How to use the API is explained in detail in the following:

**[Data Sources](#data-sources)**  
**[Event Management](#event-management)**  
**[Continuous Queries](#continuous-queries)**  

## Data Sources

A data source provides event for the Esper engine. By adding a data source, an input adapter is instantiated, which extracts data from this data source and forwards this data to the Esper engine.

### Add data source:
     
POST /EsperService/datasources HTTP/1.1  
Content-Type: application/json  
Accept: application/json  
```javascript
{"protocol": "MQTT", "endpoint":"tcp://192.168.209.190:1883", "topics":["TempEvent"]}
```
HTTP/1.1 201 CREATED  
Content-Type: application/json  
```javascript
{"datasource_id":"paho704154760161418","status":"BOUND"}
```

### Get data sources: 
GET /EsperService/datasources HTTP/1.1  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
[{"paho704154760161418":{"protocol":"MQTT","endpoint":"tcp://192.168.209.190:1883","topics":["TempEvent"]}}]
```

### Get data source by {datasource_id}:
GET /EsperService/datasources/paho704154760161418 HTTP/1.1  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"protocol":"MQTT","endpoint":"tcp://192.168.209.190:1883","topics":["TempEvent"]}
```

### Remove data source {datasource_id}:
DELETE /EsperService/datasources/paho704154760161418 HTTP/1.1  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"protocol":"MQTT","endpoint":"tcp://192.168.209.190:1883","topics":["TempEvent"]}
```

## Event Management
The provided API allows the definition of new event types at runtime. Furthermore, besides providing events to Esper through a *data source*, it is also possible to send events to Esper through a HTTP request. In this case, no data source is necessary to be added.

### Add event type:
POST /EsperService/event/types HTTP/1.1  
Content-Type: application/json  
Accept: application/json  
```javascript
{"eventtype": "create schema TempEvent(sensorID string, temperature double)"}
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"eventtype_id":"stmt_1","status":"running"}
```

### Get event types:
GET /EsperService/event/types HTTP/1.1  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
[{"eventtype_id":"TempEvent","properties":{"temperature":"Double","sensorID":"String"}}]
```

### Send event to the Esper engine:
POST /EsperService/event HTTP/1.1  
Content-Type: application/json  
```javascript
{"TempEvent": {"sensorID": "A0", "temperature": "19.0"}}
```

HTTP/1.1 204 No Content  

## Continuous Queries
The provided API allows the creation of continuous queries at runtime. Furthermore, subscribers can be added to a query in order to get notified when this query matches one or more input events.

### Create and start a continuous query:
POST /EsperService/queries HTTP/1.1  
Content-Type: application/json  
```javascript
{"query": "select * from TempEvent(sensorID='A0')"} 
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"query_id":"stmt_2","status":"running"}
```

### Create and start a continuous query (with subscriber):
POST /EsperService/queries HTTP/1.1  
Content-Type: application/json  
```javascript
{"query": "select * from TempEvent(sensorID='A0')", "subscriber": {"protocol": "MQTT", "endpoint":"tcp://192.168.209.190:1883", "topics":["situation"]}}
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"query_id":"stmt_3","status":"running"}
```

### Get all continuous queries:
GET /EsperService/queries  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
[{"query_id":"stmt_2","status":"STARTED"},{"query_id":"stmt_1","status":"STARTED"},{"query_id":"stmt_3","status":"STARTED"}]
```

### Stop continuous query {query_id}:
POST /EsperService/queries/stmt_2/stop  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"query_id":"stmt_2","status":"STOPPED"}
```

### Start continuous query {query_id}:
POST /EsperService/queries/stmt_2/start  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"query_id":"stmt_2","status":"STARTED"}
```

### Add subscriber continuous query {query_id}:
POST /EsperService/queries/stmt_2/subscriptions  
Content-Type: application/json  
```javascript
{"protocol": "MQTT", "endpoint":"tcp://192.168.209.190:1883", "topics":["situation"]} 
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"subscription_id":"sub1520344781693","query_id":"stmt_2"}
```

### Get subscribers of continuous query {query_id}:
GET /EsperService/queries/stmt_2/subscriptions  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
[{"sub1520344781693":{"protocol":"MQTT","endpoint":"tcp://192.168.209.190:1883","topics":["situation"]}}]
```

### Remove subscriber {subscriber_id} of continuous query {query_id}:
DELETE /EsperService/queries/stmt_2/subscriptions/sub1520344781693  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"sub1520344781693":{"protocol":"MQTT","endpoint":"tcp://192.168.209.190:1883","topics":["situation"]}}
```

### Delete continuous query {query_id}:
DELETE /EsperService/queries/stmt_2  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{"query_id":"stmt_2","status":"DESTROYED"}
```

## Haftungsausschluss

Dies ist ein Forschungsprototyp.
Die Haftung für entgangenen Gewinn, Produktionsausfall, Betriebsunterbrechung, entgangene Nutzungen, Verlust von Daten und Informationen, Finanzierungsaufwendungen sowie sonstige Vermögens- und Folgeschäden ist, außer in Fällen von grober Fahrlässigkeit, Vorsatz und Personenschäden ausgeschlossen.

## Disclaimer of Warranty

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.
