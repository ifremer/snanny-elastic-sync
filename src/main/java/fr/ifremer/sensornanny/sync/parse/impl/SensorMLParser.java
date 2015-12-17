package fr.ifremer.sensornanny.sync.parse.impl;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import fr.ifremer.sensornanny.sync.parse.IContentParser;
import net.opengis.sensorml.v_2_0.PhysicalSystemType;

public class SensorMLParser implements IContentParser<JAXBElement<PhysicalSystemType>> {

    private static final String PARSER_TYPE = "SensorMLParser";
    private static final String SCHEMA = "net.opengis.sensorml.v_2_0";

    @SuppressWarnings("unchecked")
    @Override
    public JAXBElement<PhysicalSystemType> parse(InputStream stream) throws Exception {
        JAXBContext context = JAXBContext.newInstance(SCHEMA);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (JAXBElement<PhysicalSystemType>) unmarshaller.unmarshal(stream);
    }

    @Override
    public String getType() {
        return PARSER_TYPE;
    }

}
