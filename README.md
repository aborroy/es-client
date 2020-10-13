# Elasticsearch Client Testing

This project includes sample source code for building Elasticsearch clients for different configurations:

* [http](http) unprotected HTTP Elasticsearch Server endpoint
* [http-basic-auth](http-basic-auth) HTTP protected with Basic Authentication Elasticsearch Server endpoint
* [https](https) HTTPs protected with Basic Authentication Elasticsearch Server endpoint

## http

Start Elasticsearch Server with following command.

```
$ docker-compose -f http/docker-compose.yml up --build --force-recreate
```

Set following constants in `org.alfresco.elasticsearch.ElasticsearchClient` class:

```java
private static final String MODE = "http";
```

And run the class locally.

You should receive a number as output.

```
15
```

## http-basic-auth

Start Elasticsearch Server with following command.

```
$ docker-compose -f http-basic-auth/docker-compose.yml up --build --force-recreate
```

Set following constants in `org.alfresco.elasticsearch.ElasticsearchClient` class:

```java
private static final String MODE = "basic";
```

And run the class locally.

You should receive a number as output.

```
57
```

## https

Create the certificates for every service.

```
$ cd https

$ docker-compose -f create-certs.yml run --rm create_certs

$ tree certs
certs
├── bundle.zip
├── ca
│   └── ca.crt
├── elasticsearch
│   ├── elasticsearch.crt
│   └── elasticsearch.key
└── kibana
    ├── kibana.crt
    └── kibana.key
```

Start Elasticsearch Server with following command.

```
$ docker-compose up elasticsearch
```

Generate passwords for internal Elasticsearch users.

```
$ docker exec https_elasticsearch_1 /bin/bash -c "bin/elasticsearch-setup-passwords auto --batch --url https://elasticsearch:9200"
Changed password for user apm_system
PASSWORD apm_system = zbxtEkGAzKVzkK273has

Changed password for user kibana_system
PASSWORD kibana_system = T3h50v6jdGV5e2XsHzQk

Changed password for user kibana
PASSWORD kibana = T3h50v6jdGV5e2XsHzQk

Changed password for user logstash_system
PASSWORD logstash_system = exdGU759uB3aTGZGQL5Y

Changed password for user beats_system
PASSWORD beats_system = 4yoPmUSWRr3DxLsFwsTo

Changed password for user remote_monitoring_user
PASSWORD remote_monitoring_user = GtQL7ev6udbqwJ2nd4Gt

Changed password for user elastic
PASSWORD elastic = JPMiXciBzCZfpgMNeAxw
```

Change the password in `docker-compose.yml` file for `kibana_system` user.

```
kibana:
  image: kibana:7.9.2
  depends_on: {"elasticsearch": {"condition": "service_healthy"}}
  ports:
    - 5601:5601
  environment:
    SERVERNAME: localhost
    ELASTICSEARCH_URL: https://elasticsearch:9200
    ELASTICSEARCH_HOSTS: https://elasticsearch:9200
    ELASTICSEARCH_USERNAME: kibana_system
    ELASTICSEARCH_PASSWORD: T3h50v6jdGV5e2XsHzQk
    ELASTICSEARCH_SSL_CERTIFICATEAUTHORITIES: /usr/share/elasticsearch/config/certificates/ca/ca.crt
    SERVER_SSL_ENABLED: "true"
    SERVER_SSL_KEY: /usr/share/elasticsearch/config/certificates/kibana/kibana.key
    SERVER_SSL_CERTIFICATE: /usr/share/elasticsearch/config/certificates/kibana/kibana.crt
  volumes:
    - ./certs:/usr/share/elasticsearch/config/certificates
```

Re-start Elasticsearch Server with following command.

```
$ docker-compose down

$ docker-compose up --build --force-recreate
```

Set following constants in `org.alfresco.elasticsearch.ElasticsearchClient` class:

```java
private static final String MODE = "https";

// Use "elastic" user password!
private static final String ELASTIC_PASS_HTTPS = "JPMiXciBzCZfpgMNeAxw";
private static final String CA_CRT_PATH = "/<YOUR_LOCAL_FOLDER_PATH>/https/certs/ca/ca.crt";
```

And run the class locally.

You should receive a number as output.

```
7
```
