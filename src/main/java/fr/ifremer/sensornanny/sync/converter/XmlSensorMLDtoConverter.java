package fr.ifremer.sensornanny.sync.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.JAXBElement;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import fr.ifremer.sensornanny.sync.dto.model.Axis;
import fr.ifremer.sensornanny.sync.dto.model.Comp;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import net.opengis.gml.v_3_2_1.CoordinatesType;
import net.opengis.sensorml.v_2_0.ComponentListType;
import net.opengis.sensorml.v_2_0.ComponentListType.Component;
import net.opengis.sensorml.v_2_0.IdentifierListPropertyType;
import net.opengis.sensorml.v_2_0.IdentifierListType.Identifier;
import net.opengis.sensorml.v_2_0.KeywordListPropertyType;
import net.opengis.sensorml.v_2_0.OutputListType;
import net.opengis.sensorml.v_2_0.OutputListType.Output;
import net.opengis.sensorml.v_2_0.PhysicalSystemType;
import net.opengis.sensorml.v_2_0.PositionUnionPropertyType;
import net.opengis.sensorml.v_2_0.TermType;
import net.opengis.swecommon.v_2_0.AbstractDataComponentType;
import net.opengis.swecommon.v_2_0.DataRecordType;
import net.opengis.swecommon.v_2_0.DataRecordType.Field;

/**
 * Convertor from XML to SensorMLDto
 * 
 * @author athorel
 *
 */
public class XmlSensorMLDtoConverter extends AbstractXMLConverter {

    public SensorML fromXML(JAXBElement<PhysicalSystemType> xml) {
        SensorML ret = new SensorML();
        PhysicalSystemType system = xml.getValue();
        ret.setDescription(StringUtils.trimToNull(system.getDescription().getValue()));
        ret.setName(extractFirstName(system.getName()));
        List<IdentifierListPropertyType> identification = system.getIdentification();
        for (IdentifierListPropertyType id : identification) {
            List<Identifier> identifierList = id.getIdentifierList().getSMLIdentifier();
            for (Identifier identifier : identifierList) {
                TermType term = identifier.getTerm();
                if (term != null) {
                    ret.setUuid(term.getValue());
                    break;
                }
            }
            // Extract the terms of the system
            extractTerms(ret, system);
            // Extract the components of the system
            extractComponents(ret, system);
            // Extract the position of the system
            extractPosition(ret, system);
        }
        return ret;
    }

    private void extractPosition(SensorML ret, PhysicalSystemType system) {
        List<PositionUnionPropertyType> positions = system.getPosition();
        if (positions != null) {
            for (PositionUnionPropertyType position : positions) {
                CoordinatesType coordinates = position.getPoint().getCoordinates();
                String value = coordinates.getValue();
                if (StringUtils.isNotBlank(value)) {
                    Axis axis = new Axis();
                    String[] split = value.split(" ");
                    switch (split.length) {
                        case 3:
                            axis.setDep(Double.valueOf(split[2]));
                        case 2:
                            axis.setLon(Double.valueOf(split[1]));
                        case 1:
                            axis.setLat(Double.valueOf(split[0]));
                            break;

                    }
                    ret.setCoordinate(axis);
                }
            }
        }
    }

    private void extractComponents(SensorML ret, PhysicalSystemType system) {
        if (system.getComponents() != null) {
            ComponentListType componentList = system.getComponents().getComponentList();
            List<Component> components = componentList.getComponent();
            List<Comp> componentHrefs = new ArrayList<>();
            for (Component component : components) {
                Comp comp = new Comp();
                comp.setHref(component.getHref());
                comp.setName(StringUtils.trimToNull(component.getName()));
                componentHrefs.add(comp);

            }
            ret.setComponents(componentHrefs);
        }
    }

    private void extractTerms(SensorML ret, PhysicalSystemType system) {
        if (system.getOutputs() != null) {
            OutputListType outputList = system.getOutputs().getOutputList();
            List<String> terms = new ArrayList<>();
            outputList.getOutput().forEach(new Consumer<Output>() {
                public void accept(Output output) {
                    Object value = output.getAbstractDataComponent().getValue();
                    if (value instanceof DataRecordType) {
                        List<Field> fields = ((DataRecordType) value).getField();
                        for (Field field : fields) {
                            AbstractDataComponentType quantity = field.getAbstractDataComponent().getValue();
                            terms.add(StringUtils.trimToNull(quantity.getDefinition()));
                        }
                    }
                };

            });
            ret.setTerms(terms);
        }

        if (CollectionUtils.isNotEmpty(system.getKeywords())) {
            List<String> keywords = new ArrayList<>();
            system.getKeywords().forEach(new Consumer<KeywordListPropertyType>() {

                @Override
                public void accept(KeywordListPropertyType t) {
                    keywords.addAll(t.getKeywordList().getKeyword());
                }
            });
            ret.setKeywords(keywords);
        }
    }

}
