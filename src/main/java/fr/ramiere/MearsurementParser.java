package fr.ramiere;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

public interface MearsurementParser {
    boolean accept(String json) throws IOException;

    boolean accept(JsonNode node);

    List<Measurement> toValues(String json) throws IOException;

    List<Measurement> toValues(JsonNode node);
}
