package org.example.tests;

import org.example.IpValidator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class IpValidatorTest {

    @DataProvider(name = "validIps")
    public Object[][] validIps() {
        return new Object[][]{
                {"0.0.0.0"},
                {"255.255.255.255"},
                {"192.168.0.1"},
                {"1.1.1.1"},
                {"127.0.0.1"},
                {"  192.168.0.1  "}
        };
    }

    @DataProvider(name = "invalidIps")
    public Object[][] invalidIps() {
        return new Object[][]{
                {null},
                {""},
                {"   "},
                {"256.1.1.1"},
                {"192.168.1"},
                {"192.168.1.1.1"},
                {"abc.def.gh.i"},
                {"192.168.01.1"},
                {"-1.1.1.1"},
                {"192.168.1.1a"},
                {"192.168..1.1"},
                {"1.2.3.4.5.6"}
        };
    }

    @Test(dataProvider = "validIps")
    public void validIpIsAccepted(String ip) {
        assertTrue(IpValidator.isValidIPv4(ip), "Ожидался валидный IP: " + ip);
    }

    @Test(dataProvider = "invalidIps")
    public void invalidIpIsRejected(String ip) {
        assertFalse(IpValidator.isValidIPv4(ip), "Ожидался невалидный IP: " + ip);
    }
}
