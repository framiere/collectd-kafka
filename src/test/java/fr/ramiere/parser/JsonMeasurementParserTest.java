package fr.ramiere.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ramiere.Measurement;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonMeasurementParserTest {

    private final JsonMeasurementParser parser = new JsonMeasurementParser();

    @Test
    public void measurementWithTagsIsValid() throws IOException {
        String json = "{\"measurement\":\"badge\",\"tags\":{\"collector\":\"vrops\",\"color\":\"GREEN\",\"entityType\":\"VirtualMachine\",\"fqdn\":\"macvlid00364.xmp.net.intra\",\"type\":\"EFFICIENCY\"},\"time\":1457432331641,\"value\":100}";
        assertThat(parser.accept(json)).isTrue();
    }

    @Test
    public void measurementWithoutTagsIsValid() throws IOException {
        String json = "{\"measurement\":\"badge\",\"time\":1457432331641,\"value\":100}";
        assertThat(parser.accept(json)).isTrue();
    }

    @Test
    public void measurementWithoutNameIsInvalid() throws IOException {
        String json = "{\"XXX\":\"badge\",\"time\":1457432331641,\"value\":100}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithoutTimeIsInvalid() throws IOException {
        String json = "{\"measurement\":\"badge\",\"XXX\":1457432331641,\"value\":100}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithoutValueIsInvalid() throws IOException {
        String json = "{\"measurement\":\"badge\",\"time\":1457432331641,\"XXX\":100}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void extractTags() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"collector\":\"vrops\",\"color\":\"GREEN\",\"entityType\":\"VirtualMachine\",\"fqdn\":\"macvlid00364.xmp.net.intra\",\"type\":\"EFFICIENCY\"}";
        JsonNode jsonNode = mapper.readTree(json);
        assertThat(parser.tags(jsonNode)).containsKeys("collector", "color", "entityType", "fqdn", "type");
        assertThat(parser.tags(jsonNode)).containsValues("vrops", "GREEN", "VirtualMachine", "macvlid00364.xmp.net.intra", "EFFICIENCY");
    }

    @Test
    public void extractTagsOnlyWhenValueIsTextBased() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"numericTag\":1,\"stringTag\":\"GREEN\"}";
        JsonNode jsonNode = mapper.readTree(json);
        assertThat(parser.tags(jsonNode)).doesNotContainKeys("numericTag");
        assertThat(parser.tags(jsonNode)).containsKeys("stringTag");
        assertThat(parser.tags(jsonNode)).containsValue("GREEN");
    }

    @Test
    public void measurementWithTagsToValue() throws IOException {
        String json = "{\"measurement\":\"badge\",\"time\":1457432331641,\"value\":100,\"tags\":{\"collector\":\"vrops\",\"color\":\"GREEN\",\"entityType\":\"VirtualMachine\",\"fqdn\":\"macvlid00364.xmp.net.intra\",\"type\":\"EFFICIENCY\"}}";

        assertThat(parser.toValue(json)).isEqualTo(
                new Measurement("badge", 1457432331641l, 100d,
                        new HashMap<String, String>() {{
                            put("collector", "vrops");
                            put("color", "GREEN");
                            put("entityType", "VirtualMachine");
                            put("fqdn", "macvlid00364.xmp.net.intra");
                            put("type", "EFFICIENCY");
                        }}));
    }

    @Test
    public void measurementWithoutTagsToValue() throws IOException {
        String json = "{\"measurement\":\"badge\",\"time\":1457432331641,\"value\":100}";
        assertThat(parser.toValue(json)).isEqualTo(
                new Measurement("badge", 1457432331641l, 100d));
    }

    @Test
    public void measurementWithNumericTagsToValue() throws IOException {
        String json = "{\"measurement\":\"badge\",\"tags\":{\"numericTag\":1,\"stringTag\":\"GREEN\"}, \"time\":1457432331641,\"value\":100}";

        assertThat(parser.toValue(json)).isEqualTo(
                new Measurement("badge", 1457432331641l, 100d,
                        new HashMap<String, String>() {{
                            put("stringTag", "GREEN");
                        }}));
    }

    @Test
    public void measurementsWithoutTagsToValues() throws IOException {
        String json = "[" +
                "{\"measurement\":\"badge1\",\"time\":1457432331641,\"value\":100}," +
                "{\"measurement\":\"badge2\",\"time\":1457432331555,\"value\":200}" +
                "]";

        assertThat(parser.toValues(json)).containsExactly(
                new Measurement("badge1", 1457432331641l, 100d),
                new Measurement("badge2", 1457432331555l, 200d));
    }

    @Test
    public void measurementsWithTagsToValues() throws IOException {
        String json = "[" +
                "{\"measurement\":\"badge1\",\"tags\":{\"numericTag\":1,\"stringTag\":\"GREEN\"}, \"time\":1457432331641,\"value\":100}," +
                "{\"measurement\":\"badge2\",\"time\":1457432331555,\"value\":200}" +
                "]";
        assertThat(parser.toValues(json)).containsExactly(
                new Measurement("badge1", 1457432331641l, 100d,
                        new HashMap<String, String>() {{
                            put("stringTag", "GREEN");
                        }}),
                new Measurement("badge2", 1457432331555l, 200d));
    }
}