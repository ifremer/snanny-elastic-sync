package fr.ifremer.sensornanny.sync.parse.impl;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import fr.ifremer.sensornanny.sync.parse.IContentParser;
import net.opengis.sos.v_2_0.InsertObservationType;

public class OMParser implements IContentParser<JAXBElement<InsertObservationType>> {

    private static final String PARSER_TYPE = "O&MParser";
    private static final String SCHEMA = "net.opengis.sos.v_2_0";

    @SuppressWarnings("unchecked")
    @Override
    public JAXBElement<InsertObservationType> parse(InputStream stream) throws Exception {
        JAXBContext context = JAXBContext.newInstance(SCHEMA);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (JAXBElement<InsertObservationType>) unmarshaller.unmarshal(stream);
    }

    @Override
    public String getType() {
        return PARSER_TYPE;
    }
}
