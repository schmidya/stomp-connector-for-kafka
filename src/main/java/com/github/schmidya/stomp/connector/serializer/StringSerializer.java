package com.github.schmidya.stomp.connector.serializer;

import org.apache.kafka.connect.data.Schema;

public class StringSerializer implements MessageSerializer {

    @Override
    public MessageRecord deserialize(String message) {
        return new MessageRecord(message, Schema.STRING_SCHEMA);
    }

    @Override
    public String serialize(MessageRecord messageRecord) {
        return messageRecord.value().toString();
    }

}
