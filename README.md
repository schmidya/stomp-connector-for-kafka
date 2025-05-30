# STOMP Connector for Apache Kafka

[STOMP](https://stomp.github.io/) is a messaging protocol that uses HTTP-like frames with header values and a seperate body. It is supported by most popular message brokers, like [Apache ActiveMQ](https://activemq.apache.org/) and [RabbitMQ](https://www.rabbitmq.com/). The protocol can be used either over a plain TCP connection or over WebSockets, and most brokers support both.

[Apache Kafka](https://kafka.apache.org/) is an event streaming platform that allows producers to log massive numbers of small data items (events) which can then be consumed by other clients. Apache Kafka defines the Kafka Connect API which can be used to import or export events from/to external systems. To do this, a _connector_ needs to be implemented for the specific external system (here the STOMP message broker) and deployed using the Kafka Connect service.

This repository contains a pure Java implementation of a STOMP connector which can be used to connect Apache Kafka to any STOMP message broker. Each STOMP message corresponds to a Kafka event.

## Status

- [x] Core functionality: Connecting to Broker. Source connector subscribes to topic. Sink connector publishes to topic.
- [x] Pure Java STOMP client with support for TCP and WebSocket connections.
- [x] Heartbeating to monitor connection health (tested with [ToxiProxy](https://github.com/Shopify/toxiproxy))
- [ ] Automatic reconnection for unhealthy connections
- [ ] Exactly-once support via STOMP acknowledgements
- [ ] Options to map STOMP message metadata/headers to Kafka event
- [ ] Support for older versions of STOMP (currently only 1.2 is supported)
