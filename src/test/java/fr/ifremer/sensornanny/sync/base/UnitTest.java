package fr.ifremer.sensornanny.sync.base;

import java.io.InputStream;

import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public abstract class UnitTest {

    protected InputStream load(String file) {
        return UnitTest.class.getClassLoader().getResourceAsStream(file);
    }
}
