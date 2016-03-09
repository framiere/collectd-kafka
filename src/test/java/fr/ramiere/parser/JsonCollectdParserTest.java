package fr.ramiere.parser;

import fr.ramiere.MearsurementParser;
import fr.ramiere.Measurement;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonCollectdParserTest {

    public static final String META = "\"meta\":{\"tsdb_tag_plugin\":\"\",\"tsdb_tag_pluginInstance\":\"\",\"tsdb_tag_type\":\"\",\"tsdb_tag_typeInstance\":\"category2\",\"tsdb_tag_add_category1\":\"tcp_connections\",\"tsdb_prefix\":\"sys.network\",\"tsdb_metric\":\"sys.network\",\"tsdb_tag_add_port\":\"_all\",\"tsdb_tag_add_collector\":\"collectd\"}";
    private final MearsurementParser parser = new JsonCollectdParser();

    @Test
    public void measurementWithMetaIsValid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isTrue();

        String oracle = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457438756.436,\"interval\":30.000,\"host\":\"macvlid00498\",\"plugin\":\"oracle\",\"plugin_instance\":\"GRID\",\"type\":\"gauge\",\"type_instance\":\"wait_scheduler\",\"meta\":{\"tsdb_tag_add_collector\":\"collectd\"}}";
        assertThat(parser.accept(oracle)).isTrue();
    }

    @Test
    public void measurementWithoutMetaIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithMissingValuesIsInvalid() throws IOException {
        String json = "{\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithNonArrayValuesIsInvalid() throws IOException {
        String numeric = "{\"values\":0,\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(numeric)).isFalse();
        String object = "{\"values\":{\"value\": 0},\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(object)).isFalse();
    }

    @Test
    public void measurementWithMissingDsTypeIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithNonArrayDsTypeIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":{\"value\": 0},\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithMissingDsNamesIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithNonArrayDsNamesIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":{\"value\": 0},\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithMissingTimeIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithNonNumericTimeIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":\"invalid\",\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithoutIntervalIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithNonNumericIntervalIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":\"invalid\",\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithoutHostIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithoutPluginIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithoutPluginInstanceIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithoutTypeIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\", \"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementWithoutTypeInstanceIsInvalid() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\"," + META + "}";
        assertThat(parser.accept(json)).isFalse();
    }

    @Test
    public void measurementToValue() throws IOException {
        String json = "{\"values\":[0],\"dstypes\":[\"gauge\"],\"dsnames\":[\"value\"],\"time\":1457435801.786,\"interval\":60.000,\"host\":\"macvlii00983.xmp.net.intra\",\"plugin\":\"tcpconns\",\"plugin_instance\":\"all\",\"type\":\"tcp_connections\",\"type_instance\":\"SYN_RECV\"," + META + "}";
        assertThat(parser.toValues(json))
                .containsExactly(new Measurement("sys.network", 1457435801786d, 0,
                        new HashMap<String, String>() {{
                            put("fqdn", "macvlii00983.xmp.net.intra");
                            put("category1", "tcp_connections");
                            put("category2", "SYN_RECV");
                            put("collector", "collectd");
                            put("port", "_all");
                        }}));
    }

    @Test
    public void measurementWithDsNameToValue() throws IOException {
        String json = "{\"values\":[0,0],\"dstypes\":[\"derive\",\"derive\"],\"dsnames\":[\"read\",\"write\"],\"time\":1457350114.593,\"interval\":60.000,\"host\":\"macvlid00497.xmp.net.intra\",\"plugin\":\"disk\",\"plugin_instance\":\"rootvg-swap\",\"type\":\"disk_merged\",\"type_instance\":\"\"" +
                ",\"meta\":{\"tsdb_tag_pluginInstance\":\"name\",\"tsdb_tag_type\":\"\",\"tsdb_tag_typeInstance\":\"\",\"tsdb_prefix\":\"sys.\",\"tsdb_metric\":\"sys.disk\",\"tsdb_tag_dsname\":\"direction\",\"tsdb_tag_add_category1\":\"merged\",\"tsdb_tag_add_unit\":\"number\",\"tsdb_tag_add_collector\":\"collectd\"}}";

        assertThat(parser.toValues(json)).containsExactly(
                new Measurement("sys.disk", 1457350114593d, 0,
                        new HashMap<String, String>() {{
                            put("fqdn", "macvlid00497.xmp.net.intra");
                            put("category1", "merged");
                            put("collector", "collectd");
                            put("direction", "read");
                            put("name", "rootvg-swap");
                            put("unit", "number");
                        }}),
                new Measurement("sys.disk", 1457350114593d, 0,
                        new HashMap<String, String>() {{
                            put("fqdn", "macvlid00497.xmp.net.intra");
                            put("category1", "merged");
                            put("collector", "collectd");
                            put("direction", "write");
                            put("name", "rootvg-swap");
                            put("unit", "number");
                        }}));
    }

    @Test
    public void integrationTest() throws IOException {
        String json = "  \n" +
                "   {  \n" +
                "      \"values\":[  \n" +
                "         253870080\n" +
                "      ],\n" +
                "      \"dstypes\":[  \n" +
                "         \"gauge\"\n" +
                "      ],\n" +
                "      \"dsnames\":[  \n" +
                "         \"value\"\n" +
                "      ],\n" +
                "      \"time\":1457444699.028,\n" +
                "      \"interval\":60.000,\n" +
                "      \"host\":\"macvlii00970.xmp.net.intra\",\n" +
                "      \"plugin\":\"memory\",\n" +
                "      \"plugin_instance\":\"\",\n" +
                "      \"type\":\"memory\",\n" +
                "      \"type_instance\":\"used\",\n" +
                "      \"meta\":{  \n" +
                "         \"tsdb_tag_type\":\"\",\n" +
                "         \"tsdb_tag_typeInstance\":\"category1\",\n" +
                "         \"tsdb_tag_add_storage\":\"RAM\",\n" +
                "         \"tsdb_prefix\":\"sys.\",\n" +
                "         \"tsdb_metric\":\"sys.memory\",\n" +
                "         \"tsdb_tag_add_unit\":\"bytes\",\n" +
                "         \"tsdb_tag_add_collector\":\"collectd\"\n" +
                "      }\n" +
                "   }\n" +
                "";

        assertThat(parser.toValues(json)).containsExactly(
                new Measurement("sys.memory", 1457444699028d, 253870080d,
                        new HashMap<String, String>() {{
                            put("fqdn", "macvlii00970.xmp.net.intra");
                            put("category1", "used");
                            put("collector", "collectd");
                            put("storage", "RAM");
                            put("unit", "bytes");
                        }}));
    }
}