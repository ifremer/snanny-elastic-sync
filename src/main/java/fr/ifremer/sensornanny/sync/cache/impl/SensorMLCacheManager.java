package fr.ifremer.sensornanny.sync.cache.impl;

import com.google.inject.Inject;
import fr.ifremer.sensornanny.sync.cache.AbstractCacheManager;
import fr.ifremer.sensornanny.sync.converter.XmlSensorMLDtoConverter;
import fr.ifremer.sensornanny.sync.dao.IOwncloudDao;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import fr.ifremer.sensornanny.sync.parse.ParseException;
import fr.ifremer.sensornanny.sync.parse.ParseUtil;
import fr.ifremer.sensornanny.sync.parse.impl.SensorMLParser;

import java.util.Date;

/**
 * Concrete implementation of cache Manager for sensorML object
 * 
 * @author athorel
 *
 */
public class SensorMLCacheManager extends AbstractCacheManager<String, SensorML> {

    @Inject
    private IOwncloudDao owncloudDao;

    @Inject
    private XmlSensorMLDtoConverter converter;

    @Inject
    private SensorMLParser parser;

    @Override
    protected SensorML read(String key) {
        try {
            String xmlContent = owncloudDao.getSML(key);
            return converter.fromXML(ParseUtil.parse(parser, xmlContent));
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    protected SensorML read(String key, Date startTime, Date endTime) {
        try {
            String xmlContent = owncloudDao.getSML(key, startTime, endTime);
            return converter.fromXML(ParseUtil.parse(parser, xmlContent));
        } catch (ParseException e) {
            return null;
        }
    }
}
