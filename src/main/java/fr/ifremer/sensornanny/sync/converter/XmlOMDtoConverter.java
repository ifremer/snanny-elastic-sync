package fr.ifremer.sensornanny.sync.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.dto.model.Axis;
import fr.ifremer.sensornanny.sync.dto.model.OM;
import fr.ifremer.sensornanny.sync.dto.model.OMResult;
import net.opengis.gml.v_3_2_1.BoundingShapeType;
import net.opengis.gml.v_3_2_1.EnvelopeType;
import net.opengis.gml.v_3_2_1.ReferenceType;
import net.opengis.gml.v_3_2_1.TimeInstantPropertyType;
import net.opengis.om.v_2_0.OMObservationType;
import net.opengis.om.v_2_0.TimeObjectPropertyType;
import net.opengis.sos.v_2_0.InsertObservationType;
import net.opengis.sos.v_2_0.InsertObservationType.Observation;

/**
 * Extracteur of Jaxb O&M to flat object
 * 
 * @author athorel
 *
 */
public class XmlOMDtoConverter extends AbstractXMLConverter {

    @Inject
    protected XmlTimePeriodDateConverter timeConverter;

    public List<OM> fromXML(JAXBElement<InsertObservationType> xml) {
        List<OM> result = new ArrayList<>();
        List<Observation> observations = xml.getValue().getObservation();
        for (Observation observation : observations) {
            OM om = new OM();
            OMObservationType omObservation = observation.getOMObservation();
            if (omObservation.getIdentifier() != null) {
                om.setIdentifier(StringUtils.trimToNull(omObservation.getIdentifier().getValue()));
            }
            if (omObservation.getDescription() != null) {
                om.setDescription(StringUtils.trimToNull(omObservation.getDescription().getValue()));
            }
            om.setName(extractFirstName(omObservation.getName()));

            TimeObjectPropertyType phnomenType = omObservation.getPhenomenonTime();
            if (phnomenType != null) {
                List<Date> dates = timeConverter.convertToDates(phnomenType.getAbstractTimeObject(), phnomenType
                        .getTYPE());
                if (CollectionUtils.isNotEmpty(dates)) {
                    om.setBeginPosition(dates.get(0));
                    if (dates.size() > 1) {
                        om.setEndPosition(dates.get(1));
                    }
                }
            }
            BoundingShapeType boundedBy = omObservation.getBoundedBy();
            if (boundedBy != null) {
                EnvelopeType envelop = boundedBy.getEnvelope().getValue();
                om.setLowerCorner(Axis.from(envelop.getLowerCorner().getValue()));
                om.setUpperCorner(Axis.from(envelop.getUpperCorner().getValue()));
            }

            TimeInstantPropertyType resultTime = omObservation.getResultTime();
            if (resultTime != null) {
                om.setUpdateDate(timeConverter.extractDate(resultTime.getTimeInstant().getTimePosition()));
            }

            om.setProcedure(omObservation.getProcedure().getHref());
            OMResult omResult = new OMResult();
            Object omObservationResult = omObservation.getResult();
            if (omObservationResult instanceof ReferenceType) {
                omResult.setUrl(((ReferenceType) omObservationResult).getHref());
                omResult.setRole(((ReferenceType) omObservationResult).getRole());
            }

            om.setResult(omResult);
            result.add(om);
        }
        return result;

    }

}
