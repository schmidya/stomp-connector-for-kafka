package com.github.schmidya.stomp.connector.source;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonRecord {
    private Schema schema;
    private Object value;

    public JsonRecord(String serializedJson){
        int abrIdx = serializedJson.indexOf('[');
        int sbrIdx = serializedJson.indexOf('{');
        if (abrIdx < 0 & sbrIdx < 0){
            // simple value
            if (NumberUtils.isParsable(serializedJson)){
                // just use Double for now
                schema = SchemaBuilder.float64().build();
                value = Double.parseDouble(serializedJson);
            } else {
                // string
                schema = SchemaBuilder.string().build();
                value = serializedJson;
            }
        } else if (abrIdx < 0 || sbrIdx < abrIdx){
            // struct
            JSONObject obj = new JSONObject(serializedJson);
            Map<String, Object> omap = obj.toMap();
            SchemaBuilder bld = SchemaBuilder.struct();
            for (String key : omap.keySet()) {
                Object v = omap.get(key);
                if (v instanceof String s){
                    bld.field(key, Schema.STRING_SCHEMA);
                } else if (v instanceof Integer i){
                    bld.field(key, Schema.INT32_SCHEMA);
                } else if (v instanceof Double d){
                    bld.field(key, Schema.FLOAT64_SCHEMA);
                } //TODO: nested types
            }
            schema = bld.build();
            Struct struct = new Struct(schema);
            for (String key : omap.keySet()) {
                Object v = omap.get(key);
                if (v instanceof String s){
                    struct.put(key, s);
                } else if (v instanceof Integer i){
                    struct.put(key, i);
                } else if (v instanceof Double d){
                    struct.put(key, d);
                }
            } 
            value = struct;
        } else if (abrIdx > 0){
            // array
            JSONArray arr = new JSONArray(serializedJson);
            List<Object> l = arr.toList();
            //TODO
        }
    }

    public Object getValue(){ return value; }
    public Schema getSchema(){ return schema; }

}
