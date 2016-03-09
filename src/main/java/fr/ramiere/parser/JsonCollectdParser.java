package fr.ramiere.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import fr.ramiere.Measurement;
import fr.ramiere.MearsurementParser;

import java.io.IOException;
import java.util.*;

public class JsonCollectdParser implements MearsurementParser {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String FIELD_VALUES = "values";
    private final String FIELD_DSTYPES = "dstypes";
    private final String FIELD_DSNAMES = "dsnames";
    private final String FIELD_INTERVAL = "interval";
    private final String FIELD_TIME = "time";
    private final String FIELD_HOST = "host";
    private final String FIELD_PLUGIN = "plugin";
    private final String FIELD_PLUGIN_INSTANCE = "plugin_instance";
    private final String FIELD_TYPE = "type";
    private final String FIELD_TYPE_INSTANCE = "type_instance";
    private final String FIELD_META = "meta";
    private final String FIELD_META_TSDB_TAG_ADD = "tsdb_tag_add_";
    private final String FIELD_META_TSDB_TAG_ADD_COLLECTOR = FIELD_META_TSDB_TAG_ADD + "collector";
    private final String FIELD_META_TSDB_TAG_ADD_COLLECTOR_COLLECTED = "collectd";
    private final String FIELD_META_TSDB_METRIC = "tsdb_metric";
    private final String FIELD_META_TSDB_TAG_PLUGIN = "tsdb_tag_plugin";
    private final String FIELD_META_TSDB_TAG_PLUGIN_INSTANCE = "tsdb_tag_pluginInstance";
    private final String FIELD_META_TSDB_TAG_TYPE = "tsdb_tag_type";
    private final String FIELD_META_TSDB_TAG_TYPE_INSTANCE = "tsdb_tag_typeInstance";
    private final String FIELD_META_TSDB_TAG_DSNAME = "tsdb_tag_dsname";

    @Override
    public boolean accept(String json) throws IOException {
        return accept(mapper.readTree(json));
    }

    @Override
    public boolean accept(JsonNode node) {
        return isArray(node, FIELD_VALUES)
                && isArray(node, FIELD_DSTYPES)
                && isArray(node, FIELD_DSNAMES)
                && isTextual(node, FIELD_HOST)
                && isTextual(node, FIELD_PLUGIN)
                && isTextual(node, FIELD_PLUGIN_INSTANCE)
                && isTextual(node, FIELD_TYPE)
                && isTextual(node, FIELD_TYPE_INSTANCE)
                && isNumeric(node, FIELD_INTERVAL)
                && isNumeric(node, FIELD_TIME)
                && isValidMeta(node, FIELD_META)
                ;
    }

    private boolean isValidMeta(JsonNode node, String field) {
        JsonNode meta = node.get(field);
        if (meta == null || !meta.isObject()) {
            return false;
        }
        JsonNode tsdb_tag_add_collector = meta.get(FIELD_META_TSDB_TAG_ADD_COLLECTOR);
        return tsdb_tag_add_collector != null && tsdb_tag_add_collector.isTextual() && FIELD_META_TSDB_TAG_ADD_COLLECTOR_COLLECTED.equals(tsdb_tag_add_collector.asText());
    }

    private boolean isArray(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isArray();
    }

    private boolean isTextual(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isTextual();
    }

    private boolean isNumeric(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isNumber();
    }

    @Override
    public List<Measurement> toValues(String json) throws IOException {
        return toValues(mapper.readTree(json));
    }

    @Override
    public List<Measurement> toValues(JsonNode node) {
        if (!accept(node)) {
            throw new IllegalArgumentException("Not a valid collectd metric");
        }

        JsonNode meta = node.get(FIELD_META);
        double time = node.get(FIELD_TIME).asDouble() * 1000;
        Map<String, String> tags = tags(node, meta);

        String metaDsName = metaDsName(meta);
        String measurementName = getMeasurementName(node, meta);

        List<Measurement> ret = new ArrayList<>();
        JsonNode values = node.get(FIELD_VALUES);
        JsonNode names = node.get(FIELD_DSNAMES);
        for (int i = 0; i < values.size(); i++) {
            Double v = values.get(i).asDouble();
            String dsName = names.get(i).asText();

            Map<String, String> valueTags = new HashMap<>(tags);
            if (metaDsName != null) {
                valueTags.put(metaDsName, dsName);
            } else if (dsName != null && !dsName.equals("value")) {
                valueTags.put("dsname", dsName);
            }
            ret.add(new Measurement(measurementName, time, v, valueTags));
        }

        return ret;
    }

    private String metaDsName(JsonNode meta) {
        JsonNode jsonNode = meta.get(FIELD_META_TSDB_TAG_DSNAME);
        if (jsonNode == null || jsonNode.isNull() || jsonNode.asText().isEmpty()) {
            return null;
        }
        return jsonNode.asText();
    }

    private Map<String, String> tags(JsonNode node, JsonNode meta) {
        Map<String, String> ret = new HashMap<>();
        ret.put("fqdn", node.get(FIELD_HOST).asText());
        Iterator<String> fieldNames = meta.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            switch (fieldName) {
                case FIELD_META_TSDB_TAG_PLUGIN:
                    addTagIfTagExists(ret, meta, node, FIELD_META_TSDB_TAG_PLUGIN, FIELD_PLUGIN);
                    break;
                case FIELD_META_TSDB_TAG_PLUGIN_INSTANCE:
                    addTagIfTagExists(ret, meta, node, FIELD_META_TSDB_TAG_PLUGIN_INSTANCE, FIELD_PLUGIN_INSTANCE);
                    break;
                case FIELD_META_TSDB_TAG_TYPE:
                    addTagIfTagExists(ret, meta, node, FIELD_META_TSDB_TAG_TYPE, FIELD_TYPE);
                    break;
                case FIELD_META_TSDB_TAG_TYPE_INSTANCE:
                    addTagIfTagExists(ret, meta, node, FIELD_META_TSDB_TAG_TYPE_INSTANCE, FIELD_TYPE_INSTANCE);
                    break;
                default:
                    if (fieldName.startsWith(FIELD_META_TSDB_TAG_ADD)) {
                        String tagName = fieldName.substring(FIELD_META_TSDB_TAG_ADD.length());
                        JsonNode tagValue = meta.get(fieldName);
                        if (tagValue.getNodeType() != JsonNodeType.STRING) {
                            return null;
                        }
                        ret.put(tagName, tagValue.asText());
                    }
                    break;
            }
        }
        return ret;
    }

    private void addTagIfTagExists(Map<String, String> tags, JsonNode meta, JsonNode node, String tagField, String valueField) {
        JsonNode tag = meta.get(tagField);
        if (tag == null || tag.isNull() || tag.getNodeType() != JsonNodeType.STRING) {
            return;
        }
        String tagValue = tag.asText();
        if (tagValue.isEmpty()) {
            return;
        }
        tags.put(tagValue, node.get(valueField).asText());
    }

    private String getMeasurementName(JsonNode node, JsonNode meta) {
        JsonNode tsdb_metric = meta.get(FIELD_META_TSDB_METRIC);
        if (tsdb_metric != null && !tsdb_metric.isNull()) {
            return tsdb_metric.asText();
        } else {
            return node.get(FIELD_TYPE_INSTANCE).asText();
        }
    }

}
