package be.formatech.filecomparator.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateUtilityTest {

    @Test
    public void testGetDateFormats() throws Exception {

    }

    @Test
    public void testGetDateFormatsAsPatternArray() throws Exception {

    }

    @Test
    public void testIsDate() throws Exception {
        assertFalse(DateUtility.isDate(null));
        assertFalse(DateUtility.isDate(StringUtils.EMPTY));
        assertFalse(DateUtility.isDate("24/mar/2013"));
        assertFalse(DateUtility.isDate("201401"));

        assertTrue(DateUtility.isDate("24/03/2013"));
        assertTrue(DateUtility.isDate("20130324"));
        assertTrue(DateUtility.isDate("24-03-2013"));
        assertTrue(DateUtility.isDate("2013-03-24"));
    }
}