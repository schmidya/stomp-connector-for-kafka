package com.github.schmidya.stomp.connector.source;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRecord {
    private static final Logger log = LoggerFactory.getLogger(JsonRecord.class);
    Schema schema;
    Object value;

    public static Struct structFromMap(Map<String, Object> omap) {
        SchemaBuilder bld = SchemaBuilder.struct();
        for (String key : omap.keySet()) {
            Object v = omap.get(key);
            if (v instanceof String) {
                bld.field(key, Schema.STRING_SCHEMA);
            } else if (v instanceof Integer) {
                bld.field(key, Schema.INT32_SCHEMA);
            } else if (v instanceof BigDecimal d) {
                omap.put(key, d.floatValue());
                bld.field(key, Schema.FLOAT32_SCHEMA);
            } else if (v instanceof Map smap) {
                @SuppressWarnings("unchecked")
                Struct sstruct = structFromMap(smap);
                omap.put(key, sstruct);
                bld.field(key, sstruct.schema());
            } else {
                log.error("Unable to build schema for field " + key + " with value of type " + v.getClass() + ":\n"
                        + v.toString());
                omap.remove(key);
            }
        }
        Schema schema = bld.build();
        Struct struct = new Struct(schema);
        for (String key : omap.keySet()) {
            struct.put(key, omap.get(key));
        }
        return struct;
    }

    public JsonRecord(String serializedJson) {
        int abrIdx = serializedJson.indexOf('[');
        int sbrIdx = serializedJson.indexOf('{');
        if (sbrIdx == 0) {
            // struct
            JSONObject obj = new JSONObject(serializedJson);
            Struct s = structFromMap(obj.toMap());
            value = s;
            schema = s.schema();
        } else if (abrIdx == 0) {
            // TODO
            // array
            throw new NotImplementedException("Support for json Arrays not yet implemented!");
            // JSONArray arr = new JSONArray(serializedJson);
            // List<Object> l = arr.toList();
        } else {
            schema = SchemaBuilder.string().build();
            value = serializedJson;
        }
    }

    public Object getValue() {
        return value;
    }

    public Schema getSchema() {
        return schema;
    }

}
