package fitnesse.fixtures;

import fitnesse.http.MockResponseSender;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class PageDriverTest {

    PageDriver pageDriver;

    @Before
    public void setup() throws Exception {
        pageDriver = new PageDriver();
        FitnesseFixtureContext.sender = new MockResponseSender();
        FitnesseFixtureContext.sender.send("\r\n\r\n<asdf id=\"123_\" /><asdf id=\"125_\" />".getBytes());
    }

    @Test
    public void idPrefixTagCount() throws Exception {
        assertEquals(0, pageDriver.countOfTagWithIdPrefix("asdf", null));
        assertEquals(0, pageDriver.countOfTagWithIdPrefix("asdf", "124"));
        assertEquals(0, pageDriver.countOfTagWithIdPrefix("blah", "123"));

        assertEquals(1, pageDriver.countOfTagWithIdPrefix("asdf", "123"));
        assertEquals(2, pageDriver.countOfTagWithIdPrefix("asdf", "12"));
    }

}
