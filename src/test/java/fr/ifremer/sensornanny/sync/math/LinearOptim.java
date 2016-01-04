package fr.ifremer.sensornanny.sync.math;

import java.io.InputStream;
import java.util.function.Consumer;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.dto.model.TimePosition;
import fr.ifremer.sensornanny.sync.parse.observations.impl.MomarObservationParser;

public class LinearOptim {

    private int cursor = 0;

    double[] lat = new double[200];
    double[] lon = new double[200];

    @Test
    public void testLinearOptm() {
        LoessInterpolator interpolator = new LoessInterpolator();

        InputStream stream = getClass().getClassLoader().getResourceAsStream(
                "netcdf/201304010045-shipnav-TL_CINNA.nav");
        String fileName = "201304010045-shipnav-TL_CINNA.nav";
        MomarObservationParser parser = new MomarObservationParser();
        parser.read(fileName, stream, new Consumer<TimePosition>() {

            @Override
            public void accept(TimePosition t) {
                if (cursor < 200) {
                    lat[cursor] = t.getLatitude();
                    lon[cursor] = t.getLongitude();
                } else {
                    PolynomialSplineFunction interpolate = interpolator.interpolate(lat, lon);
                    PolynomialFunction[] polynomials = interpolate.getPolynomials();
                    for (PolynomialFunction polynomialFunction : polynomials) {
                        // polynomialFunction.
                    }

                    cursor = 0;
                    lat = new double[200];
                    lon = new double[200];
                }

            }
        });
    }

}
