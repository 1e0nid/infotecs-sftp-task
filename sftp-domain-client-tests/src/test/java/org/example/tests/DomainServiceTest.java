package org.example.tests;

import org.example.DomainService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DomainServiceTest {

    private static final String FILENAME = "addresses.json";

    private Path tempDir;
    private DomainService domainService;

    @BeforeMethod
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("domain-service-test");
        domainService = new DomainService(tempDir.toString(), FILENAME);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> p.toFile().delete());
    }

    @Test
    public void getAllMappingReturnsEmptyMapWhenFileMissing() throws IOException {
        assertTrue(domainService.getAllMapping().isEmpty());
    }

    @Test
    public void getAllMappingIsSortedAlphabeticallyByDomain() throws IOException {
        domainService.addPair("zebra.domain", "10.0.0.1");
        domainService.addPair("alpha.domain", "10.0.0.2");
        domainService.addPair("mike.domain", "10.0.0.3");

        Map<String, String> result = domainService.getAllMapping();

        assertEquals(new ArrayList<>(result.keySet()),
                Arrays.asList("alpha.domain", "mike.domain", "zebra.domain"));
    }

    @Test
    public void addPairStoresNewEntry() throws IOException {
        domainService.addPair("first.domain", "192.168.0.1");

        Optional<String> ip = domainService.getIp("first.domain");
        assertTrue(ip.isPresent());
        assertEquals(ip.get(), "192.168.0.1");
    }

    @Test
    public void addPairNormalizesDomainToLowerCase() throws IOException {
        domainService.addPair("First.DOMAIN", "192.168.0.1");

        assertTrue(domainService.getIp("first.domain").isPresent());
    }

    @Test
    public void ipCanBeReusedAfterOwningDomainIsRemoved() throws IOException {
        domainService.addPair("first.domain", "192.168.0.1");
        domainService.removePair("first.domain");

        domainService.addPair("second.domain", "192.168.0.1");

        assertEquals(domainService.getIp("second.domain").get(), "192.168.0.1");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addPairRejectsDuplicateDomain() throws IOException {
        domainService.addPair("first.domain", "192.168.0.1");
        domainService.addPair("first.domain", "192.168.0.2");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addPairRejectsDuplicateDomainDifferentCase() throws IOException {
        domainService.addPair("first.domain", "192.168.0.1");
        domainService.addPair("FIRST.DOMAIN", "192.168.0.2");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addPairRejectsDuplicateIp() throws IOException {
        domainService.addPair("first.domain", "192.168.0.1");
        domainService.addPair("second.domain", "192.168.0.1");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addPairRejectsInvalidIp() throws IOException {
        domainService.addPair("first.domain", "999.999.999.999");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addPairRejectsEmptyDomain() throws IOException {
        domainService.addPair("   ", "192.168.0.1");
    }

    @Test
    public void getIpReturnsEmptyForUnknownDomain() throws IOException {
        assertFalse(domainService.getIp("unknown.domain").isPresent());
    }

    @Test
    public void getDomainReturnsEmptyForUnknownIp() throws IOException {
        assertFalse(domainService.getDomain("10.10.10.10").isPresent());
    }

    @Test
    public void getDomainFindsDomainByIp() throws IOException {
        domainService.addPair("third.domain", "192.168.0.3");

        Optional<String> domain = domainService.getDomain("192.168.0.3");
        assertTrue(domain.isPresent());
        assertEquals(domain.get(), "third.domain");
    }

    @Test
    public void removePairByDomainSucceeds() throws IOException {
        domainService.addPair("first.domain", "192.168.0.1");

        assertTrue(domainService.removePair("first.domain"));
        assertFalse(domainService.getIp("first.domain").isPresent());
    }

    @Test
    public void removePairByIpSucceeds() throws IOException {
        domainService.addPair("first.domain", "192.168.0.1");

        assertTrue(domainService.removePair("192.168.0.1"));
        assertFalse(domainService.getIp("first.domain").isPresent());
    }

    @Test
    public void removePairReturnsFalseForUnknownIdentifier() throws IOException {
        assertFalse(domainService.removePair("unknown.domain"));
    }

    @Test
    public void removePairReturnsFalseForEmptyOrNullIdentifier() throws IOException {
        assertFalse(domainService.removePair(""));
        assertFalse(domainService.removePair(null));
    }
}
