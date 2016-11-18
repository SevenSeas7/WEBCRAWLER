import java.sql.*; 
import java.util.Vector;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

public class SQL{
	private static Connection c = null;
  
	public static void Connect()  {
		try {
			SQLiteConfig config = new SQLiteConfig();
			config.setOpenMode(SQLiteOpenMode.READWRITE);
			config.setOpenMode(SQLiteOpenMode.NOMUTEX);
			
			Class.forName("org.sqlite.JDBC"); 
		  	c = DriverManager.getConnection("jdbc:sqlite:douban.db",config.toProperties());
		} catch ( Exception e) {
			Close();
			System.err.println(e.getMessage() );
			System.exit(0);
		}
	}
	
	public static void Close()  {
		try {
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
  }
  
  public static void InsertBookInfo(Vector<String> queries){
		  Statement stmt = null;
		  try{
			  c.setAutoCommit(false);
		      stmt = c.createStatement();
		      for(String s: queries){
			      String sql = "INSERT INTO BOOKS (ID,TITLE,SCORE,NUMBER_COMMENTS,AUTHOR,PUBLISHER,DATE,PRICE,TAG) " +
		                   "VALUES (NULL,"+s+");"; 
			      stmt.executeUpdate(sql);
			      }
		      stmt.close();
		      c.commit(); 
		  }catch(Exception e){
		      System.err.println(e.getMessage());
		      System.exit(0);
     	    }  
  }
}