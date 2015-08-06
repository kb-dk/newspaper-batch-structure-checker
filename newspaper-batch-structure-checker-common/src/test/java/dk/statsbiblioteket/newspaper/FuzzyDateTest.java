package dk.statsbiblioteket.newspaper;

import org.junit.Test;
import org.testng.Assert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

public class FuzzyDateTest {

    @Test
    public void validStrictStringTest() throws ParseException {
        String dateString = "2013-12-24";
        FuzzyDate fuzzyDate = new FuzzyDate(dateString);
        assertEquals(fuzzyDate.asString(), dateString);
    }

    @Test
    public void validPaddedFuzzyStringTest() throws ParseException {
        String dateString = "2013-00-00";
        FuzzyDate fuzzyDate = new FuzzyDate(dateString);
        assertEquals(fuzzyDate.asString(), dateString);

        dateString = "2013-01-00";
        fuzzyDate = new FuzzyDate(dateString);
        assertEquals(fuzzyDate.asString(), dateString);
    }

    @Test
    public void validFuzzyStringTest() throws ParseException {
        String dateString = "2013";
        FuzzyDate fuzzyDate = new FuzzyDate(dateString);
        assertEquals(fuzzyDate.asString(), dateString);

        dateString = "2013-01";
        fuzzyDate = new FuzzyDate(dateString);
        assertEquals(fuzzyDate.asString(), dateString);
    }

    @Test
    public void invalidStringTest() throws ParseException {
        String dateString = "2013-09--10";
        try {
            new FuzzyDate(dateString);
            fail("Invalid string " + dateString + " shouldn't be accepted");
        } catch (ParseException e) {
        }
        dateString = "2013-13-10";
        try {
            new FuzzyDate(dateString);
            fail("Invalid string " + dateString + " shouldn't be accepted");
        } catch (ParseException e) {
        }
        dateString = "2013-12-32";
        try {
            new FuzzyDate(dateString);
            fail("Invalid string " + dateString + " shouldn't be accepted");
        } catch (ParseException e) {
        }
        dateString = "2013-12-AA";
        try {
            new FuzzyDate(dateString);
            fail("Invalid string " + dateString + " shouldn't be accepted");
        } catch (ParseException e) {
        }
        dateString = "201312-10";
        try {
            new FuzzyDate(dateString);
            fail("Invalid string " + dateString + " shouldn't be accepted");
        } catch (ParseException e) {
        }
    }

    @Test
    public void strictBeforeTest() throws ParseException {
        FuzzyDate referenceDate = new FuzzyDate("2013-09-12");
        assertTrue(referenceDate.compareTo(new FuzzyDate("2013-09-11")) > 0);
        assertTrue(referenceDate.compareTo(new FuzzyDate("2013-08-13")) > 0);
        assertTrue(referenceDate.compareTo(new FuzzyDate("2012-10-13")) > 0);
    }

    @Test
    public void strictEqualTest() throws ParseException {
        FuzzyDate referenceDate = new FuzzyDate("2013-09-12");
        assertTrue(referenceDate.compareTo(new FuzzyDate("2013-09-12")) == 0);
    }

    @Test
    public void strictAfterTest() throws ParseException {
        FuzzyDate referenceDate = new FuzzyDate("2013-09-12");
        assertTrue(referenceDate.compareTo(new FuzzyDate("2013-09-13")) < 0);
        assertTrue(referenceDate.compareTo(new FuzzyDate("2013-10-11")) < 0);
        assertTrue(referenceDate.compareTo(new FuzzyDate("2014-10-13")) < 0);
    }

    @Test
    public void fuzzyBeforeTest() throws ParseException {
        FuzzyDate strictReferenceDate = new FuzzyDate("2013-09-12");
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2013-08-00")) > 0);
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2012-00-00")) > 0);

        FuzzyDate fuzzyDayReferenceDate = new FuzzyDate("2013-09-00");
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-08-10")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-08-00")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2012-09-00")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2012-00-00")) > 0);

        FuzzyDate fuzzyMonthReferenceDate = new FuzzyDate("2013-00-00");
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2012-09-00")) > 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2012-00-00")) > 0);
    }

    @Test
    public void fuzzyEqualTest() throws ParseException {
        FuzzyDate strictReferenceDate = new FuzzyDate("2013-09-12");
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2013-09-00")) == 0);
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2013-00-00")) == 0);

        FuzzyDate fuzzyDayReferenceDate = new FuzzyDate("2013-09-00");
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-09-10")) == 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-09-00")) == 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-00-00")) == 0);

        FuzzyDate fuzzyMonthReferenceDate = new FuzzyDate("2013-00-00");
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2013-09-10")) == 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2013-09-00")) == 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2013-00-00")) == 0);
    }

    @Test
    public void fuzzyPaddingAfterTest() throws ParseException {
        FuzzyDate strictReferenceDate = new FuzzyDate("2013-09-09");
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2013-10-00")) < 0);
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2014-00-00")) < 0);

        FuzzyDate fuzzyDayReferenceDate = new FuzzyDate("2013-09-00");
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-10-10")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-10-00")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2014-00-00")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2014-01-00")) < 0);

        FuzzyDate fuzzyMonthReferenceDate = new FuzzyDate("2013-00-00");
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2014-10-00")) < 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2014-00-00")) < 0);
    }

    @Test
    public void fuzzyPaddingBeforeTest() throws ParseException {
        FuzzyDate strictReferenceDate = new FuzzyDate("2013-09-12");
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2013-08")) > 0);
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2012")) > 0);

        FuzzyDate fuzzyDayReferenceDate = new FuzzyDate("2013-09");
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-08-10")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-08")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2012-09")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2012")) > 0);

        FuzzyDate fuzzyMonthReferenceDate = new FuzzyDate("2013");
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2012-09")) > 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2012")) > 0);
    }

    @Test
    public void fuzzyPaddingEqualTest() throws ParseException {
        FuzzyDate strictReferenceDate = new FuzzyDate("2013-09-12");
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2013-09")) == 0);
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2013")) == 0);

        FuzzyDate fuzzyDayReferenceDate = new FuzzyDate("2013-09");
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-09-10")) == 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-09")) == 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013")) == 0);

        FuzzyDate fuzzyMonthReferenceDate = new FuzzyDate("2013-00-00");
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2013-09-10")) == 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2013-09")) == 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2013")) == 0);
    }

    @Test
    public void fuzzyAfterTest() throws ParseException {
        FuzzyDate strictReferenceDate = new FuzzyDate("2013-09-09");
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2013-10")) < 0);
        assertTrue(strictReferenceDate.compareTo(new FuzzyDate("2014")) < 0);

        FuzzyDate fuzzyDayReferenceDate = new FuzzyDate("2013-09");
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-10-10")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2013-10")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2014")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(new FuzzyDate("2014-01")) < 0);

        FuzzyDate fuzzyMonthReferenceDate = new FuzzyDate("2013");
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2014-10")) < 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(new FuzzyDate("2014")) < 0);
    }

    @Test
    public void fuzzyCompareToRealTest() throws ParseException {

        FuzzyDate strictReferenceDate = new FuzzyDate("2013-09-12");
        assertTrue(strictReferenceDate.compareTo(getDate("2013-10-01")) < 0);
        assertTrue(strictReferenceDate.compareTo(getDate("2014-01-01")) < 0);
        assertTrue(strictReferenceDate.compareTo(getDate("2013-08-00")) > 0);
        assertTrue(strictReferenceDate.compareTo(getDate("2012-00-00")) > 0);
        assertTrue(strictReferenceDate.compareTo(getDate("2013-09-12")) == 0);

        FuzzyDate fuzzyDayReferenceDate = new FuzzyDate("2013-09");
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2013-10-10")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2013-10-01")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2014-01-01")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2014-01-01")) < 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2013-08-10")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2013-08-00")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2012-09-00")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2012-00-00")) > 0);
        assertTrue(fuzzyDayReferenceDate.compareTo(getDate("2013-09-01")) == 0);

        FuzzyDate fuzzyMonthReferenceDate = new FuzzyDate("2013-00-00");
        assertTrue(fuzzyMonthReferenceDate.compareTo(getDate("2014-10-01")) < 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(getDate("2014-01-01")) < 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(getDate("2012-09-00")) > 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(getDate("2012-00-00")) > 0);
        assertTrue(fuzzyMonthReferenceDate.compareTo(getDate("2013-01-01")) == 0);
    }

    @Test
    public void testGetMinDate() throws ParseException {
        FuzzyDate date = new FuzzyDate("1831-00-00");
        // java Date constructor uses y-1900 so 1831 -> -69
        // also January is month 0
        Assert.assertTrue(date.getMinDate().equals(new Date(-69, 0, 1)));
        date = new FuzzyDate("1831-02-00");
        Assert.assertEquals(date.getMinDate().toString(), new Date(-69, 1, 1).toString());
    }

    @Test
    public void testGetMaxDate() throws ParseException {
        FuzzyDate date = new FuzzyDate("1831-00-00");
        Assert.assertTrue(date.getMaxDate().equals(new Date(-69, 11, 31)));
        date = new FuzzyDate("1831-02-00");
        Assert.assertTrue(date.getMaxDate().equals(new Date(-69, 1, 28)));
        date = new FuzzyDate("1984-02-00");
        Assert.assertTrue(date.getMaxDate().equals(new Date(84, 1, 29)));
        date = new FuzzyDate("1955-12-00");
        assertEquals(date.getMaxDate().toString(), new Date(55,11,31).toString());
    }

    private Date getDate(String values) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(values);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
