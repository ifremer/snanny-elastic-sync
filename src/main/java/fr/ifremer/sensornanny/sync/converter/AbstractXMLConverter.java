package fr.ifremer.sensornanny.sync.converter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.opengis.gml.v_3_2_1.CodeType;

public abstract class AbstractXMLConverter {

    protected String extractFirstName(List<CodeType> name) {
        for (CodeType codeType : name) {
            return StringUtils.trimToNull(codeType.getValue());
        }
        return null;
    }
}
