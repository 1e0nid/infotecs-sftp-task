package org.example.tests;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestSuiteRunner {

    public static void main(String[] args) {
        XmlSuite suite = new XmlSuite();
        suite.setName("SFTP Domain Client Test Suite");

        XmlTest test = new XmlTest(suite);
        test.setName("Unit tests");

        List<XmlClass> classes = new ArrayList<>();
        classes.add(new XmlClass(IpValidatorTest.class));
        classes.add(new XmlClass(JsonParserTest.class));
        classes.add(new XmlClass(DomainServiceTest.class));
        test.setXmlClasses(classes);

        TestNG testng = new TestNG();
        testng.setXmlSuites(Collections.singletonList(suite));
        testng.setVerbose(1);
        testng.run();

        System.exit(testng.hasFailure() ? 1 : 0);
    }
}
