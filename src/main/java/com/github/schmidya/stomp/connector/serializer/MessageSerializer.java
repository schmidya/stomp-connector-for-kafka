package com.github.schmidya.stomp.connector.serializer;

public interface MessageSerializer {

    public MessageRecord deserialize(String message);

    public String serialize(MessageRecord messageRecord);

}
