package uk.dioxic.mgenerate.core;

import org.bson.json.JsonWriterSettings;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentStateCacheTest {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static JsonWriterSettings jws = JsonWriterSettings.builder()
            .indent(true)
            .build();

    @Test
    void get_Lookups() throws URISyntaxException, IOException {
        Template template = Template.from(Paths.get(getClass().getClassLoader().getResource("lookup-test.json").toURI()));

        String outJson = template.toJson(jws);
        logger.debug(outJson);

        DocumentStateCache.setEncodingContext(template);

        Object cachedValue = DocumentStateCache.get("c3");
        Object expected = String.format("%s <%s>", DocumentStateCache.get("b"), DocumentStateCache.get("c.cc.ccc"));
        assertThat(cachedValue).as("is resolvable").isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    void get_AggregatedLookups() throws URISyntaxException, IOException {
        Template template = Template.from(Paths.get(getClass().getClassLoader().getResource("dot-notation-test.json").toURI()));

        String outJson = template.toJson(jws);
        logger.debug(outJson);

        DocumentStateCache.setEncodingContext(template);

        Object actual = DocumentStateCache.get("allColours");
        assertThat(actual).isInstanceOf(List.class);
        assertThat((List) actual).hasSize(4);
        assertThat((List) actual).containsAnyOf("blue", "red", "white", "yellow", "orange");

        actual = DocumentStateCache.get("foodAndColours");
        assertThat(actual).isInstanceOf(List.class);
        assertThat((List) actual).hasSizeGreaterThan(5);
        assertThat((List) actual).containsAnyOf("blue", "red", "white", "yellow", "orange", "worms", "petals", "slugs", "snails", "duckweed");

        actual = DocumentStateCache.get("distinctFood");
        assertThat(actual).isInstanceOf(List.class);
        assertThat((List) actual).isNotEmpty();
        assertThat((List) actual).containsAnyOf("worms", "petals", "slugs", "snails", "duckweed");

        actual = DocumentStateCache.get("minAge");
        assertThat(actual).isInstanceOf(Integer.class);
        assertThat((Integer) actual).isBetween(1, 10);

        actual = DocumentStateCache.get("avgAge");
        assertThat(actual).isInstanceOf(Double.class);
        assertThat((Double) actual).isBetween(1d, 10d);
    }
}