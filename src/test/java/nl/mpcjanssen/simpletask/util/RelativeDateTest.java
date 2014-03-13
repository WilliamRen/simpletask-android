package nl.mpcjanssen.simpletask.util;

import nl.mpcjanssen.simpletask.GradleRunner;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.robolectric.Robolectric;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Calendar;

import org.junit.runner.RunWith;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: Mark Janssen
 * Date: 21-7-13
 * Time: 12:28
 */

@RunWith(GradleRunner.class) 
public class RelativeDateTest extends TestCase {
    @Test
    public void testMonthWrap()  {
        // Bug f35cd1b
        DateTimeFormatter df = ISODateTimeFormat.date();
        DateTime now = df.parseDateTime("2013-10-01");
        DateTime when = df.parseDateTime("2013-09-30");
        assertEquals("1 day ago", RelativeDate.computeRelativeDate(Robolectric.application, now, when));
    }
}
