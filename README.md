# EsperService

This project provides a wrapper API for the CEP Engine [Esper](http://www.espertech.com/esper). To use this wrapper, run mvn install to generate a .war file, which can be deployed into the application server [Tomcat8](http://tomcat.apache.org/tomcat-8.5-doc/).  

How to use the API is explained in detail in the following:

**[1 Data Sources](#1-data-sources)**  
**[2 Event Management](#2-event-management)**  
**[3 Continuous Queries](#3-continuous-queries)**  

## 1 Data Sources

A data source provides event for the Esper engine. By adding a data source, an input adapter is instantiated, which extracts data from this data source and forwards this data to the Esper engine. Currently, it is possible to add a *MQTT-based Message Broker* and a *FIWARE Orion Context Broker* as data sources, however, only if authentication is not configured. 

### 1.1 Add Data Source
     
POST /EsperService/datasources HTTP/1.1  
Content-Type: application/json  
Accept: application/json  
```javascript
{
  "protocol": "MQTT", 
  "endpoint":"tcp://192.168.209.190:1883", 
  "topics":["TempEvent"]
}
```
HTTP/1.1 201 CREATED  
Content-Type: application/json  
```javascript
{
  "datasource_id":"paho704154760161418",
  "status":"BOUND"
}
```

To add a *FIWARE Orion Context Broker* as data source, where *EntityName = Demoraum* and *AttributeName = temp1*:  

POST /EsperService/datasources HTTP/1.1  
Content-Type: application/json  
Accept: application/json  
```javascript
{
  "protocol": "HTTP-Orion", 
  "endpoint":"http://192.168.209.165:1026/v2/entities", 
  "topics":["Demoraum/attrs/temp1/value"], 
  "headers":["Content-Type=text/plain"]
}
```
HTTP/1.1 201 CREATED  
Content-Type: application/json  
```javascript
{"datasource_id":"ha704154760161418","status":"BOUND"}
```

### 1.2 Get All Data Sources 
GET /EsperService/datasources HTTP/1.1  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
[
  {
    "paho704154760161418":{
      "protocol":"MQTT",
      "endpoint":"tcp://192.168.209.190:1883",
      "topics":["TempEvent"]
    }
  }
]
```

### 1.3 Get Data Source by {datasource_id}
GET /EsperService/datasources/paho704154760161418 HTTP/1.1  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "protocol":"MQTT",
  "endpoint":"tcp://192.168.209.190:1883",
  "topics":["TempEvent"]
}
```

### 1.4 Remove the Data Source {datasource_id}
DELETE /EsperService/datasources/paho704154760161418 HTTP/1.1  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "protocol":"MQTT",
  "endpoint":"tcp://192.168.209.190:1883",
  "topics":["TempEvent"]
}
```

## 2 Event Management
The provided API allows the definition of new event types at runtime. Furthermore, besides providing events to Esper through a *data source*, it is also possible to send events to Esper through a HTTP request. In this case, no data source is necessary to be added.

### 2.1 Add Event Type
POST /EsperService/event/types HTTP/1.1  
Content-Type: application/json  
Accept: application/json  
```javascript
{
  "eventtype": "create schema TempEvent(sensorID string, temperature double)"
}
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "eventtype_id":"stmt_1",
  "status":"running"
}
```

### 2.2 Get All Event Types
GET /EsperService/event/types HTTP/1.1  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
[
  {
    "eventtype_id":"TempEvent",
    "properties": {
      "temperature":"Double",
      "sensorID":"String"
    }
  }
]
```

### 2.3 Send an Event to the Esper Engine
POST /EsperService/event HTTP/1.1  
Content-Type: application/json  
```javascript
{
  "TempEvent": {
    "sensorID": "A0", 
    "temperature": 19.0
  }
}
```

HTTP/1.1 204 No Content  

## 3 Continuous Queries
The provided API allows the creation of continuous queries at runtime. Furthermore, subscribers can be added to a query in order to get notified when this query matches one or more input events. Currently, it is possible to add a *MQTT-based Message Broker* and a *FIWARE Orion Context Broker* as subscribers, however, only if authentication is not configured. 

### 3.1 Create and Start a Continuous Query
POST /EsperService/queries HTTP/1.1  
Content-Type: application/json  
```javascript
{
  "query": "select * from TempEvent(sensorID='A0')"
} 
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "query_id":"stmt_2",
  "status":"running"
}
```

### 3.2 Create and Start a Continuous Query with Subscriber
POST /EsperService/queries HTTP/1.1  
Content-Type: application/json  
```javascript
{
  "query": "select * from TempEvent(sensorID='A0')", 
  "subscriber": {
    "protocol": "MQTT", 
    "endpoint":"tcp://192.168.209.190:1883", 
    "topics":["situation"]
  }
}
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "query_id":"stmt_3",
  "status":"running"
}
```

The subscriber to the topic *situation* will receive a message in the following format containing the triggered *query_id* and the event properties: 
```javascript
{
  "query_id":"stmt_3",
  "event": {
    "temperature":39,
    "sensorID":"A0"
  }
}
```


### 3.3 Get all Continuous Queries
GET /EsperService/queries  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
[
  {
    "query_id":"stmt_2",
    "status":"STARTED"
  },
  {
    "query_id":"stmt_1",
    "status":"STARTED"
  },
  {
    "query_id":"stmt_3",
    "status":"STARTED"
  }
]
```

### 3.4 Stop the Continuous Query {query_id}
POST /EsperService/queries/stmt_2/stop  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "query_id":"stmt_2",
  "status":"STOPPED"
}
```

### 3.5 Start the Continuous Query {query_id}
POST /EsperService/queries/stmt_2/start  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "query_id":"stmt_2",
  "status":"STARTED"
}
```

### 3.6 Add Subscriber to the Continuous Query {query_id}
POST /EsperService/queries/stmt_2/subscriptions  
Content-Type: application/json  
```javascript
{
  "protocol": "MQTT", 
  "endpoint":"tcp://192.168.209.190:1883", 
  "topics":["situation"]
} 
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "subscription_id":"sub1520344781693",
  "query_id":"stmt_2"
}
```

The subscriber to the topic *situation* will receive a message in the following format containing the triggered *query_id* and the event properties: 
```javascript
{
  "query_id":"stmt_2",
  "event":{
    "temperature":39,
    "sensorID":"A0"
  }
}
```

To add Fiware Orion as subscriber, where *EntityName = Demoraum-switch* and *AttributeName = cmd*:

POST /EsperService/queries/stmt_2/subscriptions  
Content-Type: application/json  
```javascript
{
  "protocol": "HTTP-Orion", 
  "endpoint": "http://192.168.209.165:1026/v2/entities", 
  "topics": ["Demoraum-switch/attrs/cmd/value"]
} 
```

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "subscription_id":"sub1520344781693",
  "query_id":"stmt_2"
}
```


### 3.7 Get Subscribers of the Continuous Query {query_id}
GET /EsperService/queries/stmt_2/subscriptions  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
[
  {
    "sub1520344781693":{
      "protocol":"MQTT",
      "endpoint":"tcp://192.168.209.190:1883",
      "topics":["situation"]
    }
  }
]
```

### 3.8 Remove the Subscriber {subscriber_id} of the Continuous Query {query_id}
DELETE /EsperService/queries/stmt_2/subscriptions/sub1520344781693  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "sub1520344781693":{
    "protocol":"MQTT",
    "endpoint":"tcp://192.168.209.190:1883",
    "topics":["situation"]
  }
}
```

### 3.9 Delete the Continuous Query {query_id}
DELETE /EsperService/queries/stmt_2  
Accept: application/json  

HTTP/1.1 200 OK  
Content-Type: application/json  
```javascript
{
  "query_id":"stmt_2",
  "status":"DESTROYED"
}
```

## Haftungsausschluss

Dies ist ein Forschungsprototyp.
Die Haftung für entgangenen Gewinn, Produktionsausfall, Betriebsunterbrechung, entgangene Nutzungen, Verlust von Daten und Informationen, Finanzierungsaufwendungen sowie sonstige Vermögens- und Folgeschäden ist, außer in Fällen von grober Fahrlässigkeit, Vorsatz und Personenschäden ausgeschlossen.

## Disclaimer of Warranty

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.
