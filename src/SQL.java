import java.sql.*;
import java.util.ArrayList;

public class SQL{
	private static Connection c = null;
  
	public static void Connect()  {
		//boolean isConn = false;
		try {
			Class.forName("org.sqlite.JDBC");
		  	c = DriverManager.getConnection("jdbc:sqlite:douban.db");
		  	//isConn = true;
		} catch ( Exception e) {
			Close();
			System.err.println(e.getMessage() );
			System.exit(0);
		}
		//return isConn;  
	}
	
	public static void Close()  {
		try {
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Close database successfully");
  }
  
  public static void InsertBookInfo(ArrayList<String> queries){
	  //if(Connect()){
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
	  //}else {  
		   //Close();  
		   //System.out.println("Cannot update database");
	  //}
  }
}