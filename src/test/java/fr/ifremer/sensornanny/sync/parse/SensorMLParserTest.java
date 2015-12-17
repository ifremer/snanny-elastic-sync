package fr.ifremer.sensornanny.sync.parse;

import java.io.InputStream;

import javax.xml.bind.JAXBElement;

import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.converter.XmlSensorMLDtoConverter;
import fr.ifremer.sensornanny.sync.dto.model.Axis;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import fr.ifremer.sensornanny.sync.parse.impl.SensorMLParser;
import net.opengis.sensorml.v_2_0.PhysicalSystemType;

public class SensorMLParserTest {

    private SensorMLParser parser = new SensorMLParser();

    private XmlSensorMLDtoConverter converter = new XmlSensorMLDtoConverter();

    @Test
    public void testParseFile() throws Exception {

        String expectedDesc = "ATALANTE IFREMER Research Vessel operated by GENAVIR";
        String expectedName = "ATALANTE";

        InputStream inputStream = SensorMLParserTest.class.getClassLoader().getResourceAsStream(
                "sensorML/atalante_sensorML.xml");
        JAXBElement<PhysicalSystemType> element = parser.parse(inputStream);
        Assert.assertNotNull("element must not be null", element);

        Assert.assertEquals(expectedDesc, element.getValue().getDescription().getValue());

        Assert.assertEquals(expectedName, element.getValue().getName().get(0).getValue());

        SensorML sensorML = converter.fromXML(element);

        Assert.assertNotNull("sensorML must not be null", sensorML);
        Assert.assertEquals(expectedDesc, sensorML.getDescription());
        Assert.assertEquals(expectedName, sensorML.getName());
        Assert.assertEquals(2, sensorML.getComponents().size());
        Assert.assertEquals("http://ubisi54.ifremer.fr:8080/sensornanny/record/056cc4aa-d862-42b9-bd83-f537910405af",
                sensorML.getComponents().get(0).getHref());
        Assert.assertEquals("http://ubisi54.ifremer.fr:8080/sensornanny/record/056cc4aa-d862-42b9-bd83-f537910405af",
                sensorML.getComponents().get(1).getHref());
        Assert.assertNull(sensorML.getTerms());
    }

    @Test
    public void testParseFileCinna() throws Exception {

        String expectedDesc = "integrated navigation system on Atalante (CINNA)";
        String expectedName = "integrated navigation system";
        String expectedUuid = "6c6bf0c8-334d-48db-bda5-297b642a097b";

        InputStream inputStream = SensorMLParserTest.class.getClassLoader().getResourceAsStream(
                "sensorML/atalante_cinna_sensorML.xml");
        SensorML sensorML = converter.fromXML(parser.parse(inputStream));

        Assert.assertNotNull("sensorML must not be null", sensorML);
        Assert.assertEquals(expectedUuid, sensorML.getUuid());
        Assert.assertEquals(expectedDesc, sensorML.getDescription());
        Assert.assertEquals(expectedName, sensorML.getName());
        Assert.assertNull(sensorML.getComponents());
        Assert.assertEquals(16, sensorML.getTerms().size());
    }

    @Test
    public void testParseMomar() throws Exception {

        String expectedDesc = "mooring";
        String expectedName = "Borel";
        String expectedUuid = "f8ea62f1-277a-4fb6-bf19-0238ea8f8d54";

        InputStream inputStream = SensorMLParserTest.class.getClassLoader().getResourceAsStream("momar/Borel.xml");
        SensorML sensorML = converter.fromXML(parser.parse(inputStream));

        Assert.assertNotNull("sensorML must not be null", sensorML);
        Assert.assertEquals(expectedUuid, sensorML.getUuid());
        Assert.assertEquals(expectedDesc, sensorML.getDescription());
        Assert.assertEquals(expectedName, sensorML.getName());
        Assert.assertEquals(2, sensorML.getComponents().size());
        Assert.assertNull(sensorML.getTerms());
        Axis coordinate = sensorML.getCoordinate();
        Assert.assertNotNull("coordinates must not be null", coordinate);
        Assert.assertEquals(37.302666, coordinate.getLat());
        Assert.assertEquals(32.2765, coordinate.getLon());
    }
}
