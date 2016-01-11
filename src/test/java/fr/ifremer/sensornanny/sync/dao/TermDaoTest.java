package fr.ifremer.sensornanny.sync.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import fr.ifremer.sensornanny.sync.base.IntegrationTest;
import fr.ifremer.sensornanny.sync.base.UnitTest;
import fr.ifremer.sensornanny.sync.dao.impl.TermDaoImpl;
import fr.ifremer.sensornanny.sync.dto.model.Term;

@Category(IntegrationTest.class)
public class TermDaoTest extends UnitTest {

    public ITermDao dao = new TermDaoImpl();

    @Test
    public void testTermDao() {
        Term term = dao.getTerm("132");
        Assert.assertNotNull("term must not be null", term);
        Assert.assertEquals("atmosphere", term.getLabel());
        Assert.assertEquals("atmosphere", term.getNotation());

        term = dao.getTerm("127");
        Assert.assertNotNull("term must not be null", term);
        Assert.assertEquals("GEOSSId", term.getLabel());
        Assert.assertEquals("hasGEOSSId", term.getNotation());
    }

}
