package adql.translator;

import static org.junit.Assert.assertEquals;
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
import adql.parser.ParseException;
import adql.parser.ADQLQueryFactory;
import adql.translator.Q3CTranslator;
import adql.query.ADQLQuery;

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
			// Test with an easy translation:
			String adqlquery = 
                "SELECT * FROM aTable WHERE DISTANCE(POINT('J2000', 187.11, 11.58), POINT('J2000', 187.15, 12)) < 0.5";

            ADQLParser parser = new ADQLParser(new DBChecker(tables), new ADQLQueryFactory());
            ADQLQuery query = parser.parseQuery(adqlquery);
			Q3CTranslator translator = new Q3CTranslator();
       
            String xlated = translator.translate(query);
            System.out.println(xlated);

		}catch(ParseException pe){
			pe.printStackTrace();
			fail("The given ADQL query is completely correct. No error should have occurred while parsing it. (see the console for more details)");
		}catch(TranslationException te){
			te.printStackTrace();
			fail("No error was expected from this translation. (see the console for more details)");
		}
	}    

    @Test
	public void testFourParamDistance(){
		try{
			// Test with an easy translation:
			String adqlquery = 
                "SELECT * FROM aTable WHERE DISTANCE(POINT('J2000', 187.11, 11.58), POINT('J2000', ra, dec)) < 0.5";

            ADQLQuery query = (new ADQLParser(new DBChecker(tables), new ADQLQueryFactory())).parseQuery(adqlquery);
			Q3CTranslator translator = new Q3CTranslator();

			//assertEquals("SELECT 'a||b||c' + ' ' + 'd+e|f' AS \"concat\"", translator.translate(query.getSelect()));
        
            String xlated = translator.translate(query);
            System.out.println(xlated);

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
		try{
			// Test with an easy translation:
			String adqlquery = 
                "SELECT TOP 100 aTable.*, bTable.* " +
                "FROM (" +
                "    SELECT * FROM aTable" +
                "    WHERE DISTANCE(POINT('ICRS', 187.11, 11.58), POINT('ICRS', ra, dec)) < 0.5 )" +
                "    AS aTable " +
                "JOIN bTable ON " +
                "    DISTANCE(POINT('ICRS', aTable.ra, aTable.dec), POINT('ICRS', bTable.ra, bTable.dec)) < 0.01";

            ADQLQuery query = (new ADQLParser(new DBChecker(tables), new ADQLQueryFactory())).parseQuery(adqlquery);
			Q3CTranslator translator = new Q3CTranslator();

			//assertEquals("SELECT 'a||b||c' + ' ' + 'd+e|f' AS \"concat\"", translator.translate(query.getSelect()));
        
            String xlated = translator.translate(query);
            System.out.println(xlated);

		}catch(ParseException pe){
			pe.printStackTrace();
			//fail("The given ADQL query is completely correct. No error should have occurred while parsing it. (see the console for more details)");
		}catch(TranslationException te){
			te.printStackTrace();
			//fail("No error was expected from this translation. (see the console for more details)");
		}
	}
}
