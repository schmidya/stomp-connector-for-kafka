package com.github.schmidya.stomp.connector.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.kafka.connect.data.Schema;
import org.junit.jupiter.api.Test;

public class StringSerializerTest {

    @Test
    public void deserializeTest() {
        var serializer = new StringSerializer();

        var actual = serializer.deserialize("Test");

        assertEquals(Schema.STRING_SCHEMA, actual.schema());
        assertInstanceOf(String.class, actual.value());
        assertEquals("Test", (String) actual.value());

    }

    @Test
    public void serializeTest() {
        var serializer = new StringSerializer();

        var record = new MessageRecord("Test", Schema.STRING_SCHEMA);

        var actual = serializer.serialize(record);

        assertEquals("Test", actual);

    }

}
