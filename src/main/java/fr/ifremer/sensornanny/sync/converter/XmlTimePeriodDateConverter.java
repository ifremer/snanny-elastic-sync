package fr.ifremer.sensornanny.sync.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.collections4.CollectionUtils;
import org.hisrc.w3c.xlink.v_1_0.TypeType;

import fr.ifremer.sensornanny.sync.util.DateUtils;
import net.opengis.gml.v_3_2_1.AbstractTimeObjectType;
import net.opengis.gml.v_3_2_1.TimeInstantType;
import net.opengis.gml.v_3_2_1.TimePeriodType;
import net.opengis.gml.v_3_2_1.TimePositionType;

/**
 * Converter of AbstractTimeObjectType to date list
 * 
 * @author athorel
 *
 */
public class XmlTimePeriodDateConverter {

    public List<Date> convertToDates(JAXBElement<AbstractTimeObjectType> timeObject, TypeType type) {
        List<Date> dates = new ArrayList<>();
        AbstractTimeObjectType time = timeObject.getValue();
        if (time instanceof TimePeriodType) {

            TimePeriodType timePeriod = (TimePeriodType) time;
            Date date = extractDate(timePeriod.getBeginPosition());
            if (date != null) {
                dates.add(date);
            }
            date = extractDate(timePeriod.getEndPosition());
            if (date != null) {
                dates.add(date);
            }
        } else if (time instanceof TimeInstantType) {
            TimeInstantType timeInstant = (TimeInstantType) time;
            Date date = extractDate(timeInstant.getTimePosition());
            if (date != null) {
                dates.add(date);
            }
        }
        return dates;

    }

    protected Date extractDate(TimePositionType position) {
        List<String> value = position.getValue();
        if (CollectionUtils.isNotEmpty(value)) {
            String item = value.get(0);
            return DateUtils.parse(item);
        }
        return null;
    }

}
