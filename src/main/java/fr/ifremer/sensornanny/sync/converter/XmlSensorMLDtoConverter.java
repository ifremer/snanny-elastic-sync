package fr.ifremer.sensornanny.sync.converter;

import fr.ifremer.sensornanny.sync.dto.model.Axis;
import fr.ifremer.sensornanny.sync.dto.model.Comp;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import net.opengis.gml.v_3_2_1.CoordinatesType;
import net.opengis.gml.v_3_2_1.TimePeriodType;
import net.opengis.gml.v_3_2_1.TimePositionType;
import net.opengis.sensorml.v_2_0.ComponentListType;
import net.opengis.sensorml.v_2_0.ComponentListType.Component;
import net.opengis.sensorml.v_2_0.DescribedObjectType;
import net.opengis.sensorml.v_2_0.IdentifierListPropertyType;
import net.opengis.sensorml.v_2_0.IdentifierListType;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBElement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Convertor from XML to SensorMLDto
 *
 * @author athorel
 */
public class XmlSensorMLDtoConverter extends AbstractXMLConverter {

	public static final String YYYY_MM_DD_THH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_THH_MM_SS_Z);

	public SensorML fromXML(JAXBElement<PhysicalSystemType> xml) {
		SensorML ret = new SensorML();
		PhysicalSystemType system = xml.getValue();
		ret.setDescription(StringUtils.trimToNull(system.getDescription().getValue()));
		ret.setName(extractFirstName(system.getName()));
		List<IdentifierListPropertyType> identification = system.getIdentification();
		for (IdentifierListPropertyType id : identification) {
			IdentifierListType list = id.getIdentifierList();
			if (list != null) {
				List<Identifier> identifierList = list.getSMLIdentifier();
				if (CollectionUtils.isNotEmpty(identifierList)) {
					for (Identifier identifier : identifierList) {
						TermType term = identifier.getTerm();
						if (term != null) {
							ret.setUuid(term.getValue());
							break;
						}
					}
				}
			}
			// Extract the terms of the system
			extractTerms(ret, system);
			// Extract the components of the system
			extractComponents(ret, system);
			// Extract the position of the system
			extractPosition(ret, system);
			// Extract the times
			extractTimes(ret, system);
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
					if (output != null) {
						if (output.getAbstractDataComponent() != null) {
							Object value = output.getAbstractDataComponent().getValue();
							if (value instanceof DataRecordType) {
								List<Field> fields = ((DataRecordType) value).getField();
								for (Field field : fields) {
									AbstractDataComponentType quantity = field.getAbstractDataComponent().getValue();
									terms.add(StringUtils.trimToNull(quantity.getDefinition()));
								}
							}

						} else if (StringUtils.isNotBlank(output.getHref())) {
							terms.add(StringUtils.trimToNull(output.getHref()));
						}
					}
				}

				;

			});
			ret.setTerms(terms);
		}

		if (CollectionUtils.isNotEmpty(system.getKeywords())) {
			List<String> keywords = new ArrayList<>();
			system.getKeywords().forEach(new Consumer<KeywordListPropertyType>() {

				@Override
				public void accept(KeywordListPropertyType t) {
					if (t.getKeywordList() != null) {
						keywords.addAll(t.getKeywordList().getKeyword());
					}
				}
			});
			ret.setKeywords(keywords);
		}
	}

	private void extractTimes(SensorML ret, PhysicalSystemType system) {
		List<DescribedObjectType.ValidTime> validTimes = system.getValidTime();
		validTimes.stream().findFirst().ifPresent(validTime -> {
			TimePeriodType timePeriod = validTime.getTimePeriod();
			if (timePeriod != null) {
				TimePositionType beginPosition = timePeriod.getBeginPosition();
				TimePositionType endPosition = timePeriod.getEndPosition();
				Long startTime = parseSmlDate(beginPosition, true);
				if(startTime != null){
					ret.setStartTime(startTime);
					Long endTime = parseSmlDate(endPosition, false);
					ret.setEndTime(endTime);
					if(endTime == null){
						ret.setEndTime(startTime);
					}
				}
			}
		});
	}

	private Long parseSmlDate(TimePositionType timePositionType, boolean start) {
		Long result = null;
		if (timePositionType != null) {
			ZoneId zoneId = ZoneId.of("UTC");
			Optional<String> date = timePositionType.getValue().stream().findFirst();
			if (date.isPresent()) {
				String timeStr = date.get();
				String dateComplement = null;
				if (timeStr.matches("^[\\d]{4}$")) {
					if (start) {
						dateComplement = "-01-01T00:00:00Z";
					} else {
						dateComplement = "-12-31T00:00:00Z";
					}

				} else if (timeStr.matches("^[\\d]{4}-[\\d]{2}-[\\d]{2}$")) {
					dateComplement = "T00:00:00Z";
				} else if (timeStr.matches("^[\\d]{4}-[\\d]{2}-[\\d]{2}T[\\d]{2}:[\\d]{2}:[\\d]{2}[Z]{0,1}$")) {
					dateComplement = timeStr.endsWith("Z") ? "" : "Z";
				}
				result = dateComplement != null ? LocalDateTime.parse(timeStr + dateComplement, dateTimeFormatter)
						.atZone(zoneId).toEpochSecond() : null;
			}
		}
		return result;
	}

}
