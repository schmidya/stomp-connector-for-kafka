package com.github.schmidya.stomp.connector.serializer;

import org.apache.kafka.connect.data.Schema;

public record MessageRecord(Object value, Schema schema) {
}