package databaseConnect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
class Field{
	String name=null;
	String type=null;
	public Field(String arg1,int arg2){
		name=arg1;
		type=getStringforInt(arg2);
	}
	private String getStringforInt(int arg) {
		switch(arg){
		case -7: return "BIT";
		case -6: return "TINYINT";
		case -5: return "BIGINT";
		case -4: return "LONGVARBINARY";
		case -3: return "VARBINARY";
		case -2: return "BINARY";
		case -1: return "LONGVARCHAR";
		case  0:return "NULL";
		case 1: return "CHAR";
		case 2: return "NUMERIC";
		case 3: return "DECIMAL";
		case 4: return "INTEGER";
		case 5: return "SMALLINT";
		case 6: return "FLOAT";
		case 7: return "REAL";
		case 8: return "DOUBLE";
		case 12 : return "VARCHAR";
		case 91 : return "DATE";
		case 92 : return "TIME";
		case 93 : return "TIMESTAMP";
		case 1111 : return "OTHER";
		}
		return "UNKNOWN";
	}
}
class TableInfo{
	private String tableName=null;
	public ArrayList<Field> fields=new ArrayList<Field>();
	private int numOfFields;
	public TableInfo(String args){
		tableName=args;
	}
	public String getTableName() {
		return tableName;
	}
	public void setNumOfFields(int columnCount) {
		numOfFields=columnCount;
		
	}
	public int getNumberOfFields() {
		return numOfFields;
	}
}
class Value{
	Field field=null;
	Object val;
	public Object getValue(){
		return val;
	}
	public Value(Object value,Field f){
		val=value;
		field=f;
	}
}
public class DatabaseConnect {
   private Connection connection=null;
   private Statement stmt=null;
   private String dbName=null;
   private String usrName=null;
   private String pswd=null;
   private String sqlType=null;
   private TableInfo tableInfo=null;
   private ArrayList<Field> fieldsInQuery=new ArrayList<Field>();
   private ArrayList<Value> values=new ArrayList<Value>();
   public void setSQLType(String args){
	   sqlType=args;
   }
   public void setPassword(String args){
	   pswd=args;
   }
   public void setDbName(String name){
	   dbName=name;
   }
   public void setUsrName(String name){
	   usrName=name;
   }
   public String getDbName(){
	   return dbName;
   }
   public String getUsrname(){
	   return usrName;
   }
   public String getPswd(){
	   return pswd;
   }
   public String getSQLType(){
	   return sqlType;
   }
   public void getTableInfo(String arg){
	   tableInfo=new TableInfo(arg);
	   String sqlQuery="SELECT * FROM "+tableInfo.getTableName();
	   try {
		ResultSet rs=stmt.executeQuery(sqlQuery);
		ResultSetMetaData rsmd=rs.getMetaData();
		tableInfo.setNumOfFields(rsmd.getColumnCount());
		tableInfo.fields.clear();
		for(int i=1;i<=tableInfo.getNumberOfFields();i++){
			tableInfo.fields.add(new Field(rsmd.getColumnName(i),rsmd.getColumnType(i)));
		}
	   }catch (SQLException e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
	   }
   }
   public void insertInto(String tableName,Object... vals){
	   String queryString="INSERT INTO "+tableName+" VALUES (";
	   for(Object i: vals){
		   queryString+="'"+i.toString()+"'"+",";
	   }
	   int len=queryString.length();
	   queryString=queryString.substring(0,len-1);
	   queryString+=")";
	   try {
		stmt.executeUpdate(queryString);
	} catch (SQLException e) {
		System.err.println("Unable to insert into the table "+tableName+" the command exected was "+queryString);
		e.printStackTrace();
	}
   }
   public void makeQuery(String query){
	   Object obj=new Object();
	   String tempString=new String();
	   try{
	   ResultSet rs=stmt.executeQuery(query);
	   ResultSetMetaData rsmd=rs.getMetaData();
	   int numOfColsInTheQuery=rsmd.getColumnCount();
	   fieldsInQuery.clear();
	   for(int i=1;i<=numOfColsInTheQuery;i++){
			fieldsInQuery.add(new Field(rsmd.getColumnName(i),rsmd.getColumnType(i)));
		}
	    values.clear();
		while(rs.next()){
			for(int i=0;i<numOfColsInTheQuery;i++){
		       obj=rs.getObject(fieldsInQuery.get(i).name);
		       if(obj==null){
		         tempString=rs.getString(fieldsInQuery.get(i).name);
		         obj=tempString;
		       }
		       values.add(new Value(obj,fieldsInQuery.get(i)));
		    }
		 }
	   }catch (SQLException e) {
		System.err.println("Can't exract row");
		e.printStackTrace();
	}
   }
   public String findValue(String fieldName,Object fieldValue,String resultFieldName){
	   String temp=null;
	   this.makeQuery("SELECT "+resultFieldName+" FROM "+tableInfo.getTableName()+" WHERE "+fieldName+" = '"+fieldValue+"'");
	   temp=this.getRow();
	   return temp;
   }
   public String findData(String fieldName,Object fieldValue){
	   this.makeQuery("SELECT * FROM "+tableInfo.getTableName()+" WHERE "+fieldName+" ='"+fieldValue+"'");
	   return this.getRow();
   }
   public String getRow(){
	   int rowLen=values.size();
	   String tempString="";
	   for(int i=0;i<rowLen;i++)
		   if(i==0)
		    tempString=tempString+values.get(i).val;
		   else
			tempString=tempString+" , "+values.get(i).val;
	   return tempString;
   }
   public void connectToDataBase(String Type,String Name,String usr,String password){
	   setDbName(Name);
	   setUsrName(usr);
	   setPassword(password);
	   setSQLType(Type);
	   try{
		   Class.forName("org.postgresql.Driver");
	   }catch(ClassNotFoundException e){
		   System.out.println("Driver Not Found");
		   e.printStackTrace();
	   }
	   try{
		   String _1stParameter="jdbc:"+sqlType+":"+dbName;
		   
		   connection= DriverManager.getConnection(_1stParameter,usrName,pswd);
		   stmt=connection.createStatement();
	   }catch(SQLException e){
		   System.out.println("Unable to connect to the database!!");
		   System.err.println(e.getMessage());
		   e.printStackTrace();
	   }
   }
   public void closeConnection(){
	   try{
		   stmt.close();
		   connection.close();
	   }catch(SQLException e){
		   System.err.println(e.getMessage());
		   e.printStackTrace();
	   }
   }
   public void CUI(String type,String name,String usr,String password) throws IOException{
	   BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
	   System.out.println("Connecting to the databse"+dbName+" ...");
	   connectToDataBase(type, name, usr, password);
	   System.out.println("Connection successful\nPreparing for taking SQL statements");
	   System.out.println("Welcome to the database "+dbName);
	   System.out.println("X------------------------------------X");
	   String st=br.readLine();
	   while(!st.equals("exit;")){
	   }
	   if(st.equals("exit;")){
		   closeConnection();
		   System.out.println("Connection to the database was closed successfully");
		   System.out.println("X-------------------------------------------------X");
	   }
   }
}
