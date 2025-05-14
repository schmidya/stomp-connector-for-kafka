package com.github.schmidya.stomp.connector.serializer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.json.JSONArray;

public class JsonSerializer implements MessageSerializer {
    private static final Logger log = LoggerFactory.getLogger(MessageSerializer.class);

    @SuppressWarnings("unchecked")
    private static MessageRecord deserializeObject(Object v) {

        if (v instanceof String) {
            return new MessageRecord(v, Schema.STRING_SCHEMA);
        }

        if (v instanceof Integer) {
            return new MessageRecord(v, Schema.INT32_SCHEMA);
        }

        if (v instanceof BigDecimal dec) {
            return new MessageRecord(dec.floatValue(), Schema.FLOAT32_SCHEMA);
        }

        if (v instanceof Map smap) {

            return deserializeMap(smap);
        }

        if (v instanceof List list) {
            return deserializeList(list);
        }

        log.warn("Unable to build schema for value of type "
                + v.getClass() + ":\n"
                + v.toString());
        return null;

    }

    private static MessageRecord deserializeMap(Map<String, Object> omap) {
        SchemaBuilder bld = SchemaBuilder.struct();
        for (String key : omap.keySet()) {
            Object v = omap.get(key);
            var record = deserializeObject(v);
            if (record == null) {
                omap.remove(key);
                continue;
            }
            bld.field(key, record.schema());
            omap.put(key, record.value());
        }
        Schema schema = bld.build();
        Struct struct = new Struct(schema);
        for (String key : omap.keySet()) {
            struct.put(key, omap.get(key));
        }
        return new MessageRecord(struct, schema);
    }

    private static MessageRecord deserializeList(List<Object> list) {
        List<MessageRecord> records = new ArrayList<>();
        for (Object o : records)
            records.add(deserializeObject(o));

        SchemaBuilder bld = SchemaBuilder.array(records.get(0).schema());
        return new MessageRecord(records, bld.build());
    }

    @Override
    public MessageRecord deserialize(String serializedJson) {
        serializedJson = serializedJson.strip();
        int abrIdx = serializedJson.indexOf('[');
        int sbrIdx = serializedJson.indexOf('{');
        if (sbrIdx == 0) {
            JSONObject obj = new JSONObject(serializedJson);
            return deserializeMap(obj.toMap());
        } else if (abrIdx == 0) {
            JSONArray arr = new JSONArray(serializedJson);
            return deserializeList(arr.toList());
        } else {
            return new MessageRecord(serializedJson, Schema.STRING_SCHEMA);
        }
    }

    private String serialize(Object object, Schema schema) {
        if (object instanceof Struct struct) {
            String ret = "{ ";
            for (Field field : struct.schema().fields()) {
                if (ret.length() > 2)
                    ret += ", ";
                ret += "\"" + field.name() + "\" : ";
                ret += serialize(struct.get(field.name()), field.schema());
            }
            ret += " }";
            return ret;
        }

        if (object instanceof List array) {
            String ret = "[ ";
            for (Object element : array) {
                if (ret.length() > 2)
                    ret += ", ";
                ret += serialize(element, schema);
            }
            ret += " ]";
            return ret;
        }

        if (schema.equals(Schema.STRING_SCHEMA)) {
            return "\"" + object.toString() + "\"";
        }

        return object.toString();

    }

    @Override
    public String serialize(MessageRecord messageRecord) {

        return serialize(messageRecord.value(), messageRecord.schema());

    }

}
