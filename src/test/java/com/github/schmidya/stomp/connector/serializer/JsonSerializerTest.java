package com.github.schmidya.stomp.connector.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.Test;

public class JsonSerializerTest {

    @Test
    public void deserializeObjectTest() {
        var serializer = new JsonSerializer();

        var actual = serializer.deserialize("{\"key\": 123}");

        var schemaBuilder = SchemaBuilder.struct();
        schemaBuilder.field("key", Schema.INT32_SCHEMA);
        var expected = new Struct(schemaBuilder.build());
        expected.put("key", 123);

        assertInstanceOf(Struct.class, actual.value());
        assertEquals(expected, (Struct) actual.value());

    }

    @Test
    public void serializeObjectTest() {
        var serializer = new JsonSerializer();

        var intermedediate = serializer.deserialize("{\"key\":123}");
        var actual = serializer.serialize(intermedediate);

        assertEquals("{ \"key\" : 123 }", actual);

    }

}
