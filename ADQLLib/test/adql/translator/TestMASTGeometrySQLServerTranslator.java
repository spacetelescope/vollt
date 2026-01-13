package adql.translator;

import static adql.translator.TestJDBCTranslator.countFeatures;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import adql.parser.ADQLQueryFactory;
import adql.parser.ADQLParser.ADQLVersion;
import adql.parser.SQLServer_ADQLQueryFactory;
import adql.parser.feature.FeatureSet;
import adql.parser.feature.LanguageFeature;
import adql.parser.grammar.ParseException;
import adql.query.ADQLQuery;
import adql.query.ADQLSet;
import adql.query.constraint.ComparisonOperator;
import adql.query.operand.function.InUnitFunction;

public class TestMASTGeometrySQLServerTranslator {
	
	private List<DBTable> tables = null;
	private String[] geometrySchemas;
	private String[] geometryTables;
	private String[] geometryFunctions;
	private float maxRadius;

	@Before
	public void setUp() throws Exception {
		tables = new ArrayList<DBTable>(2);
		DefaultDBTable t = new DefaultDBTable("aTable");
		t.addColumn(new DefaultDBColumn("id", t));
		t.addColumn(new DefaultDBColumn("name", t));
		t.addColumn(new DefaultDBColumn("aColumn", t));
		tables.add(t);
		t = new DefaultDBTable(null, "dbo", "geoTable");
		t.addColumn(new DefaultDBColumn("id", t));
		t.addColumn(new DefaultDBColumn("name", t));
		t.addColumn(new DefaultDBColumn("anotherColumn", t));
        t.addColumn(new DefaultDBColumn("source_id", t));
        t.addColumn(new DefaultDBColumn("ra", t));
        t.addColumn(new DefaultDBColumn("dec", t));
		tables.add(t);
		
		geometrySchemas = new String[] {"dbo"};
		geometryTables = new String[] {"dbo.geoTable"};
		geometryFunctions = new String[] {"fnSPATIAL_SearchSTCSFootprint"};
		maxRadius = 30;
	}
    
    @Test
    public void testTranslateGeometryWithOffset() {
        MAST_Geometry_SQLServerTranslator tr = new MAST_Geometry_SQLServerTranslator(geometrySchemas, geometryTables, null, geometryFunctions, maxRadius);
        DBChecker dbChecker = new adql.db.DBChecker(tables);
        ADQLQueryFactory queryFactory = new SQLServer_ADQLQueryFactory();
        ADQLParser parser = new ADQLParser(ADQLVersion.V2_1, dbChecker, queryFactory, null);
        
        
        
        try {
            // CASE: Simple OFFSET (no geometry)
            String expectedSQLString = "SELECT aTable.id AS \"id\" , " +
            "aTable.name AS \"name\" , " +
            "aTable.aColumn AS \"acolumn\"\n" +
            "FROM aTable\n" +
            "ORDER BY 1 ASC\n" +
            "OFFSET 10 ROWS";
            String ADQLresults = tr.translate(parser.parseQuery("SELECT * FROM aTable OFFSET 10"));
            assertEquals(expectedSQLString, ADQLresults);
            
			// CASE: Geometry query with OFFSET - no limit
			String adql = "SELECT source_id, ra, dec " +
			"FROM dbo.geoTable " +
			"WHERE 1=CONTAINS(" +
				"POINT('ICRS', ra, dec), " +
				"CIRCLE('ICRS', 148.89, 69.0588, 0.01)) " +
			"OFFSET 4";
            
            expectedSQLString = "select geometryTempResults.source_id as \"source_id\" , " +
            "geometryTempResults.ra as \"ra\" , " +
            "geometryTempResults.dec as \"dec\"\n" +
            "FROM fnSPATIAL_SearchSTCSFootprint('circle 148.89 69.0588 0.01') AS geometryTempResults\n\n" +
            "order by geometrytempresults.distance asc\n" +
            "OFFSET 4 ROWS";
            ADQLresults = tr.translate(parser.parseQuery(adql));
            assertEquals(expectedSQLString, ADQLresults);
            
            // CASE: Geometry query with OFFSET and LIMIT
            adql = "SELECT TOP 10 source_id, ra, dec " +
            "FROM dbo.geoTable " +
            "WHERE 1=CONTAINS(" +
                "POINT('ICRS', ra, dec), " +
                "CIRCLE('ICRS', 148.89, 69.0588, 0.01)) " +
            "OFFSET 4";
            expectedSQLString = "select top 10 geometryTempResults.source_id as \"source_id\" , " +
            "geometryTempResults.ra as \"ra\" , " +
            "geometryTempResults.dec as \"dec\"\n" +
            "FROM fnSPATIAL_SearchSTCSFootprint('circle 148.89 69.0588 0.01') AS geometryTempResults\n\n" +
            "order by geometrytempresults.distance asc\n" +
            "OFFSET 4 ROWS " +
            "FETCH NEXT 10 ROWS ONLY";
            ADQLresults = tr.translate(parser.parseQuery(adql));
            assertEquals(expectedSQLString, ADQLresults);

        } catch (ParseException e) {
            e.printStackTrace(System.err);
            fail("Unexpected ParseException: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            fail("Unexpected Exception: " + e.getMessage());
        }
    }
    
}
