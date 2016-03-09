package fr.ramiere.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import fr.ramiere.Measurement;
import fr.ramiere.MearsurementParser;

import java.io.IOException;
import java.util.*;

public class JsonMeasurementParser implements MearsurementParser {
    public static final String FIELD_MEASUREMENT = "measurement";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_VALUE = "value";
    public static final String FIELD_TAGS = "tags";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean accept(String json) throws IOException {
        return accept(mapper.readTree(json));
    }

    @Override
    public boolean accept(JsonNode node) {
        JsonNode measurement = node.get(FIELD_MEASUREMENT);
        if (measurement == null || measurement.isNull() || !measurement.isTextual()) {
            return false;
        }
        JsonNode time = node.get(FIELD_TIME);
        if (time == null || time.isNull() || !time.isLong()) {
            return false;
        }
        JsonNode value = node.get(FIELD_VALUE);
        if (value == null || value.isNull() || !value.isNumber()) {
            return false;
        }
        JsonNode tags = node.get(FIELD_TAGS);
        if (tags != null && !tags.isNull() && !tags.isObject()) {
            return false;
        }
        return true;
    }

    Map<String, String> tags(JsonNode node) {
        Map<String, String> tags = new HashMap<>();
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode fieldValue = node.get(fieldName);
            if (fieldValue.getNodeType() == JsonNodeType.STRING) {
                tags.put(fieldName, fieldValue.asText());
            }
        }
        return tags;
    }

    @Override
    public List<Measurement> toValues(String json) throws IOException {
        return toValues(mapper.readTree(json));
    }

    @Override
    public List<Measurement> toValues(JsonNode jsonNode) {
        Iterator<JsonNode> iterator = jsonNode.iterator();
        List<Measurement> ret = new ArrayList<>();
        while (iterator.hasNext()) {
            ret.add(toValue(iterator.next()));
        }
        return ret;
    }

    Measurement toValue(String json) throws IOException {
        return toValue(mapper.readTree(json));
    }

    Measurement toValue(JsonNode node) {
        if (!accept(node)) {
            throw new IllegalArgumentException("Not a valid simple metric");
        }
        String measurement = node.get(FIELD_MEASUREMENT).asText();
        long time = node.get(FIELD_TIME).asLong();
        double value = node.get(FIELD_VALUE).asDouble();
        JsonNode tagsNode = node.get(FIELD_TAGS);
        if (tagsNode == null) {
            return new Measurement(measurement, time, value);
        }
        return new Measurement(measurement, time, value, tags(tagsNode));
    }
}
