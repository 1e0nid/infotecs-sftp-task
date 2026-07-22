package org.example.tests;

import org.example.JsonParser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JsonParserTest {

    private JsonParser jsonParser;
    private Path tempFile;

    @BeforeMethod
    public void setUp() throws IOException {
        jsonParser = new JsonParser();
        tempFile = Files.createTempFile("addresses", ".json");
    }

    @AfterMethod
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void parsesValidFileWithMultipleEntries() throws IOException {
        writeFile("{\n" +
                "  \"addresses\": [\n" +
                "    { \"domain\": \"first.domain\", \"ip\": \"192.168.0.1\" },\n" +
                "    { \"domain\": \"second.domain\", \"ip\": \"192.168.0.2\" }\n" +
                "  ]\n" +
                "}");

        Map<String, String> result = jsonParser.parse(tempFile.toString());

        assertEquals(result.size(), 2);
        assertEquals(result.get("first.domain"), "192.168.0.1");
        assertEquals(result.get("second.domain"), "192.168.0.2");
    }

    @Test
    public void parsesEntryRegardlessOfKeyOrder() throws IOException {
        writeFile("{ \"addresses\": [ { \"ip\": \"10.0.0.5\", \"domain\": \"reversed.domain\" } ] }");

        Map<String, String> result = jsonParser.parse(tempFile.toString());

        assertEquals(result.get("reversed.domain"), "10.0.0.5");
    }

    @Test
    public void skipsEntryWithInvalidIp() throws IOException {
        writeFile("{ \"addresses\": [ { \"domain\": \"bad.domain\", \"ip\": \"999.999.999.999\" } ] }");

        Map<String, String> result = jsonParser.parse(tempFile.toString());

        assertTrue(result.isEmpty(), "Запись с некорректным IP не должна попасть в результат");
    }

    @Test
    public void emptyAddressesListProducesEmptyMap() throws IOException {
        writeFile("{ \"addresses\": [] }");

        Map<String, String> result = jsonParser.parse(tempFile.toString());

        assertTrue(result.isEmpty());
    }

    @Test
    public void missingFileProducesEmptyMapInsteadOfError() throws IOException {
        Files.deleteIfExists(tempFile);

        Map<String, String> result = jsonParser.parse(tempFile.toString());

        assertTrue(result.isEmpty());
    }

    @Test
    public void buildJsonThenParseRoundTripPreservesData() throws IOException {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("alpha.domain", "1.1.1.1");
        data.put("beta.domain", "2.2.2.2");

        jsonParser.buildJson(data, tempFile.toString());
        Map<String, String> parsed = jsonParser.parse(tempFile.toString());

        assertEquals(parsed, data);
    }

    @Test
    public void buildJsonWithEmptyMapProducesValidEmptyStructure() throws IOException {
        jsonParser.buildJson(new LinkedHashMap<>(), tempFile.toString());

        Map<String, String> parsed = jsonParser.parse(tempFile.toString());

        assertTrue(parsed.isEmpty());
    }

    private void writeFile(String content) throws IOException {
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
    }
}
