package adql.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import adql.db.DBChecker;
import adql.db.DBTable;
import adql.db.DefaultDBColumn;
import adql.db.DefaultDBTable;
import adql.parser.ADQLParser;
import adql.parser.ADQLParser.ADQLVersion;
import adql.parser.ADQLQueryFactory;
import adql.parser.grammar.ParseException;
import adql.query.ADQLSet;

public class TestQ3CTranslator {

	private List<DBTable> tables = null;

	@Before
	public void setUp() throws Exception{
		tables = new ArrayList<DBTable>(2);
		DefaultDBTable t = new DefaultDBTable("aTable");
		t.addColumn(new DefaultDBColumn("id", t));
		t.addColumn(new DefaultDBColumn("name", t));
		t.addColumn(new DefaultDBColumn("ra", t));
        t.addColumn(new DefaultDBColumn("dec", t));
		tables.add(t);
		t = new DefaultDBTable("bTable");
		t.addColumn(new DefaultDBColumn("id", t));
		t.addColumn(new DefaultDBColumn("name", t));
		t.addColumn(new DefaultDBColumn("ra", t));
        t.addColumn(new DefaultDBColumn("dec", t));
		tables.add(t);
	}

    @Test
	public void testPointsDistance(){
		try{
			String adqlquery = 
                "SELECT top 1 * FROM aTable WHERE DISTANCE(POINT('ICRS', 187.11, 11.58), POINT('ICRS', 187.15, 12)) < 0.5";

            ADQLParser parser = new ADQLParser(ADQLVersion.V2_1, new DBChecker(tables), new ADQLQueryFactory(), null);
            ADQLSet query = parser.parseQuery(adqlquery);
            Q3CTranslator translator = new Q3CTranslator();
       
            assertTrue(translator.translate(query).contains("q3c_dist(187.11,11.58,187.15,12)"));

		}catch(ParseException pe){
			pe.printStackTrace();
			fail("The given ADQL query is completely correct. No error should have occurred while parsing it. (see the console for more details)");
		}catch(TranslationException te){
			te.printStackTrace();
			fail("No error was expected from this translation. (see the console for more details)");
		}
	}  

    @Test
	public void testCrossmatch(){
        // Test crossmatch query as derived from ESA examples using angular separation:
        // https://www.cosmos.esa.int/web/gaia-users/archive/writing-queries
        // Note this library only supports the POINT, POINT version of DISTANCE at this time.
		try{
			String adqlquery = 
                "SELECT TOP 100 geo_aTable.*, bTable.* " +
                "FROM (" +
                "    SELECT * FROM aTable" +
                "    WHERE DISTANCE(POINT('ICRS', 187.11, 11.58), POINT('ICRS', ra, dec)) < 0.5 )" +
                "    AS geo_aTable " +
                "JOIN bTable ON " +
                "    DISTANCE(POINT('ICRS', geo_aTable.ra, geo_aTable.dec), POINT('ICRS', bTable.ra, bTable.dec)) < 0.01";

			ADQLParser parser = new ADQLParser(ADQLVersion.V2_1);
			parser.setQueryChecker(new DBChecker(tables));
			
			ADQLSet query = parser.parseQuery(adqlquery);
			Q3CTranslator translator = new Q3CTranslator();
			
			assertTrue(translator.translate(query).contains("q3c_dist"));

		}catch(ParseException pe){
			pe.printStackTrace();
			fail("The given ADQL query is completely correct. No error should have occurred while parsing it. (see the console for more details)");
		}catch(TranslationException te){
			te.printStackTrace();
			fail("No error was expected from this translation. (see the console for more details)");
		}
	}
	
	@Test
	public void testPointsDistanceMAST() {
		try {
			String adqlquery = "SELECT top 1 * FROM aTable WHERE DISTANCE(POINT('ICRS', 187.11, 11.58), POINT('ICRS', 187.15, 12)) <= 0.5";

			ADQLParser parser = new ADQLParser(ADQLVersion.V2_1, new DBChecker(tables), new ADQLQueryFactory(), null);
			ADQLSet query = parser.parseQuery(adqlquery);
			MAST_Q3CTranslator translator = new MAST_Q3CTranslator();

			assertEquals(
			"SELECT aTable.id AS \"id\" , aTable.name AS \"name\" , aTable.ra AS \"ra\" , aTable.dec AS \"dec\"\n"+
			"FROM aTable\n" +
			"WHERE q3c_join(187.11,11.58,187.15,12,0.5)\n" +
			"LIMIT 1", translator.translate(query));
			

		} catch (ParseException pe) {
			pe.printStackTrace();
			fail("The given ADQL query is completely correct. No error should have occurred while parsing it. (see the console for more details)");
		} catch (TranslationException te) {
			te.printStackTrace();
			fail("No error was expected from this translation. (see the console for more details)");

		}
	}
}
