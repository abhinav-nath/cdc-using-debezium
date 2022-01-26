# Change Data Capture using Debezium and Kafka

## Kafka Topics Names
The PostgreSQL connector writes events for all **insert**, **update**, and **delete** operations on a single table to a **single Kafka topic**.

The name of the Kafka topics takes by default the form `serverName.schemaName.tableName`, where `serverName` is the logical name of the connector as specified with the `database.server.name` configuration property, `schemaName` is the name of the database schema where the operation occurred, and `tableName` is the name of the database table on which the operation occurred.

For example, consider a PostgreSQL installation with a postgres database and an inventory schema that contains four tables: `products`, `products_on_hand`, `customers`, and `orders`. If the connector monitoring this database were given a logical server name of `fulfillment`, then the connector would produce events on these four Kafka topics:

```
fulfillment.inventory.products

fulfillment.inventory.products_on_hand

fulfillment.inventory.customers

fulfillment.inventory.orders
```

If on the other hand the tables were not part of a specific schema but rather created in the default `public` PostgreSQL schema, then the name of the Kafka topics would be:

```
fulfillment.public.products

fulfillment.public.products_on_hand

fulfillment.public.customers

fulfillment.public.orders
```

## Create a Debezium connector

POST `http://localhost:8083/connectors`

```json
{
    "name": "test-connector",
    "config": {
        "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
        "tasks.max": "1",
        "plugin.name": "wal2json",
        "database.hostname": "postgres",
        "database.port": "5432",
        "database.user": "postgres",
        "database.password": "postgres",
        "database.dbname": "testDb",
        "database.server.name": "testDbServer",
        "key.converter": "org.apache.kafka.connect.json.JsonConverter",
        "value.converter": "org.apache.kafka.connect.json.JsonConverter",
        "key.converter.schemas.enable": "false",
        "value.converter.schemas.enable": "false",
        "snapshot.mode": "always"
    }
}
```


## Get Debezium connectors

GET `http://localhost:8083/connectors`


## Delete Debezium connector

DELETE `http://localhost:8083/connectors/test-connector`


## SQLs

```sql
CREATE TABLE products
(
    code int,
    name varchar(255),
    PRIMARY KEY (code)
);

-- To show the previous values of all the table columns
ALTER TABLE public.products REPLICA IDENTITY FULL;

INSERT INTO products values (1, 'Pen');
INSERT INTO products values (2, 'Bat');
INSERT INTO products values (3, 'Ball');
INSERT INTO products values (4, 'Book');
INSERT INTO products values (5, 'Phone');

UPDATE products SET name='Charger' WHERE code=1;

UPDATE products SET name='Pen' WHERE code=1;

DELETE FROM products WHERE code=1;

SELECT * FROM public.products;

-- clean up
DROP TABLE products;

SELECT pg_drop_replication_slot('debezium');
```

## Kafka commands

### List all topics

```
./kafka-topics.sh --bootstrap-server kafka:9092 --list
```

### Kafka Console Consumer

```
./kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic testDbServer.public.products --from-beginning --max-messages 10
```

## Messages received by the Consumer

* Insert a new record

  ```json
  {
    "before": null,
    "after": {
      "code": 1,
      "name": "Pen"
    },
    "source": {
      "version": "1.8.0.Final",
      "connector": "postgresql",
      "name": "testDbServer",
      "ts_ms": 1643209693230,
      "snapshot": "false",
      "db": "testDb",
      "sequence": "[\"24134432\",\"24134848\"]",
      "schema": "public",
      "table": "products",
      "txId": 512,
      "lsn": 24134848,
      "xmin": null
    },
    "op": "c",
    "ts_ms": 1643209693522,
    "transaction": null
  }
  ```
* Update record

  ```json
  {
    "before": {
      "code": 1,
      "name": "Pen"
    },
    "after": {
      "code": 1,
      "name": "Charger"
    },
    "source": {
      "version": "1.8.0.Final",
      "connector": "postgresql",
      "name": "testDbServer",
      "ts_ms": 1643209705972,
      "snapshot": "false",
      "db": "testDb",
      "sequence": "[\"24134848\",\"24135128\"]",
      "schema": "public",
      "table": "products",
      "txId": 513,
      "lsn": 24135128,
      "xmin": null
    },
    "op": "u",
    "ts_ms": 1643209706215,
    "transaction": null
  }
  ```

* Delete record

  ```json
  {
    "before": {
      "code": 1,
      "name": "Charger"
    },
    "after": null,
    "source": {
      "version": "1.8.0.Final",
      "connector": "postgresql",
      "name": "testDbServer",
      "ts_ms": 1643209815657,
      "snapshot": "false",
      "db": "testDb",
      "sequence": "[\"24135128\",\"24135328\"]",
      "schema": "public",
      "table": "products",
      "txId": 514,
      "lsn": 24135328,
      "xmin": null
    },
    "op": "d",
    "ts_ms": 1643209816050,
    "transaction": null
  }
  ```
