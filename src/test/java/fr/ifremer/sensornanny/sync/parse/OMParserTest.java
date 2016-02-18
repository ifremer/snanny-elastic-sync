package fr.ifremer.sensornanny.sync.parse;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.ifremer.sensornanny.sync.ElasticSyncModule;
import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.converter.XmlOMDtoConverter;
import fr.ifremer.sensornanny.sync.dto.model.OM;
import fr.ifremer.sensornanny.sync.parse.impl.OMParser;
import fr.ifremer.sensornanny.sync.util.DateUtils;
import net.opengis.om.v_2_0.OMObservationType;
import net.opengis.sos.v_2_0.InsertObservationType;
import net.opengis.sos.v_2_0.InsertObservationType.Observation;

public class OMParserTest extends UnitTest {

    private OMParser parser = new OMParser();

    private XmlOMDtoConverter converter;

    @Before
    public void beforeOMParserTest() {
        Injector injector = Guice.createInjector(new ElasticSyncModule());
        converter = injector.getInstance(XmlOMDtoConverter.class);
    }

    @Test
    public void testParseFile() throws Exception {

        InputStream inputStream = load("observation/292d9bd6-815c-11e4-a9c3-5c260a184584.xml");
        JAXBElement<InsertObservationType> element = parser.parse(inputStream);
        Assert.assertNotNull("element must not be null", element);

        String expectedDescription = "thermosalinometre ESS_PROP_13, 2013, THALASSA";
        String expectedName = "thermosalinometer ESS_PROP_13";
        String expectedUuid = "292d9bd6-815c-11e4-a9c3-5c260a184584";
        String expectedResultHref = "file:///home/sismer_donnees/geosciences/home9/atlantique/2013040050/201304005045-shipnav-TL_CINNA.nav";
        String expectedProcedure = "https://isi.ifremer.fr/snanny-sostServer/record/9e6d24d0-bc76-4aef-9d7d-f2bc052dde34";

        List<Observation> observations = element.getValue().getObservation();
        Assert.assertEquals(1, observations.size());
        Observation observation = observations.get(0);
        OMObservationType omObservation = observation.getOMObservation();

        Assert.assertEquals(expectedDescription, omObservation.getDescription().getValue());
        Assert.assertEquals(expectedName, omObservation.getName().get(0).getValue());
        Assert.assertEquals(expectedUuid, omObservation.getIdentifier().getValue());

        List<OM> results = converter.fromXML(element);

        Assert.assertEquals(1, results.size());
        OM om = results.get(0);

        Assert.assertEquals(expectedDescription, om.getDescription());
        Assert.assertEquals(expectedName, om.getName());
        Assert.assertEquals(expectedUuid, om.getIdentifier());
        Assert.assertEquals(47.654, om.getLowerCorner().getLat());
        Assert.assertEquals(-5.266, om.getLowerCorner().getLon());
        Assert.assertEquals(48.382, om.getUpperCorner().getLat());
        Assert.assertEquals(-4.39, om.getUpperCorner().getLon());

        Assert.assertEquals(DateUtils.parse("2013-09-05"), om.getBeginPosition());
        Assert.assertEquals(DateUtils.parse("2013-09-09"), om.getEndPosition());
        Assert.assertEquals(DateUtils.parse("2014-04-04T08:30:29.021042"), om.getUpdateDate());
        Assert.assertEquals(expectedResultHref, om.getResult().getUrl());
        Assert.assertEquals(expectedProcedure, om.getProcedure());
        Assert.assertEquals("application/netcdf", om.getResult().getRole());
    }

    @Test
    public void testParseFileArgo() throws Exception {

        InputStream inputStream = load("argo/argo.xml");
        JAXBElement<InsertObservationType> element = parser.parse(inputStream);
        List<OM> results = converter.fromXML(element);

        Assert.assertEquals(1, results.size());
        OM om = results.get(0);

        System.out.println(om);
    }
}
