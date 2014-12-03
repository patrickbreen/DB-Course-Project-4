package internal.database;

/*******************************************************************************
 * @file  TestTupleGenerator.java
 *
 * @author   Sadiq Charaniya, John Miller
 */

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NavigableMap;
import java.util.Scanner;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;


/*******************************************************************************
 * This class tests the TupleGenerator on the Student Registration Database
 * defined in the Kifer, Bernstein and Lewis 2006 database textbook (see figure
 * 3.6). The primary keys (see figure 3.6) and foreign keys (see example 3.2.2)
 * are as given in the textbook.
 */
@SuppressWarnings("all")
public class TestTupleGeneratorBeforeOptim {
	/***************************************************************************
	 * The main method is the driver for TestGenerator.
	 * 
	 * @param args
	 *            the command-line arguments
	 * @author patrickbreen
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
		TupleGenerator test = new TupleGeneratorImpl();

		test.addRelSchema("Student", "id name address status",
				"Integer String String String", "id", null);

		test.addRelSchema("Professor", "id name deptId",
				"Integer String String", "id", null);

		test.addRelSchema("Course", "crsCode deptId crsName descr",
				"String String String String", "crsCode", null);

		test.addRelSchema("Teaching", "crsCode semester profId",
				"String String Integer", "crsCode semester", new String[][] {
				{ "profId", "Professor", "id" },
				{ "crsCode", "Course", "crsCode" } });

		test.addRelSchema(
				"Transcript",
				"studId crsCode semester grade",
				"Integer String String String",
				"studId crsCode semester", null);


		//Project 4


		int numTuples = 50000;

		//generate test data
		Comparable[][][] TestData = test.generate(new int[] { numTuples, numTuples, numTuples, numTuples, numTuples });

		//set up connection with mysql using JDBC driver
		out.println("Connecting to MYSQL (database: project4, user:root, password: root) (must already be set)...");
		Class.forName("com.mysql.jdbc.Driver") ;
		java.sql.Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/project4", "root", "root") ;
		java.sql.Statement stmt = conn.createStatement() ;

		//reset database records
		out.println("Reseting database...");
		String query = "DROP TABLE IF EXISTS student;" ;
		Boolean suc = stmt.execute(query) ;
		query = "DROP TABLE IF EXISTS course;" ;
		suc = stmt.execute(query) ;
		query = "DROP TABLE IF EXISTS professor;" ;
		suc = stmt.execute(query) ;
		query = "DROP TABLE IF EXISTS teaching;" ;
		suc = stmt.execute(query) ;
		query = "DROP TABLE IF EXISTS transcript;" ;
		suc = stmt.execute(query) ;

		//import schema
		out.println("Create tables if not exist...");
		ScriptRunner runner = new ScriptRunner(conn, false, true);
		runner.runScript(new BufferedReader(new FileReader("mysql_schema_before_optimization.sql"))); 




		//insert test data into mysql
		System.out.println("Inserting records into mysql using " + numTuples +  " tuples...");
		for(int j=0; j<numTuples; j++){
			//student table
			stmt = conn.createStatement();
			String sql = "INSERT INTO Student(id, name, address, status) " +
					"VALUES ('" + TestData[0][j][0] + "', '" + TestData[0][j][1] + "', '" + TestData[0][j][2] + "', '" + TestData[0][j][3] + "')";
			stmt.executeUpdate(sql);

			//professor table
			stmt = conn.createStatement();
			sql = "INSERT INTO Professor(id, name, deptID) " +
					"VALUES ('" + TestData[1][j][0] + "', '" + TestData[1][j][1] + "', '" + TestData[1][j][2] + "')";
			stmt.executeUpdate(sql);

			//course table
			stmt = conn.createStatement();
			sql = "INSERT INTO Course(crsCode, deptID, crsName, descr) " +
					"VALUES ('" + TestData[2][j][0] + "', '" + TestData[2][j][1] + "', '" + TestData[2][j][2] + "', '" + TestData[2][j][3] + "')";
			stmt.executeUpdate(sql);

			//course teaching
			stmt = conn.createStatement();
			sql = "INSERT INTO Teaching(crsCode, semester, profID) " +
					"VALUES ('" + TestData[3][j][0] + "', '" + TestData[3][j][1] + "', '" + TestData[3][j][2] + "')";
			stmt.executeUpdate(sql);

			//transcript table
			stmt = conn.createStatement();
			sql = "INSERT INTO Transcript(studID, crsCode, semester, grade) " +
					"VALUES ('" + TestData[4][j][0] + "', '" + TestData[4][j][1] + "', '" + TestData[4][j][2] + "', '" + TestData[4][j][3] + "')";
			stmt.executeUpdate(sql);

		}
		out.println("insert is successful");

		//number of replicates for a query
		int replicates = 4;
		out.println("Using 4 replicates");


		//1. list the name of the student with id equal to v1(id)
		out.println("1. list the name of the student with id equal to v1(id) (time in ms)");
		query = "select student.name from student WHERE id = " + TestData[0][0][0]+ ";" ;
		ResultSet rs = stmt.executeQuery("explain " + query);
		out.println("Query plan: ");
		int c = 1;
		rs.next();
		while(rs.getString(c) != null){

			out.print(" " + rs.getString(c) + " ");
			c++;
		}
		out.println();
		out.println();
		for(int i =0; i < replicates; i++){

			double begin_time = System.currentTimeMillis();
			rs = stmt.executeQuery(query) ;
			double end_time = System.currentTimeMillis();
			out.println((end_time-begin_time)/1000);
			while(rs.next()){
				//Retrieve by column name
				String name  = rs.getString("name");
				//Display values
				//System.out.println("NAME: " + name);
			}
		}


		//2. list the name of the students with id in the range of v2 id to v3 (inclusive)
		out.println("2. list the name of the students with id in the range of v2 id to v3 (inclusive) v");

		Comparable firstVal = null;
		Comparable secondVal = null;
		if(TestData[0][2][0].compareTo(TestData[0][1][0]) > 0){
			firstVal = TestData[0][1][0];
			secondVal = TestData[0][2][0];
		} else {
			firstVal = TestData[0][2][0];
			secondVal = TestData[0][1][0];
		}
		query = "select student.name from student WHERE id >= " + firstVal + " and id <= " + secondVal + " ;" ;
		rs = stmt.executeQuery("explain " + query);
		out.println("Query plan: ");
		c = 1;
		rs.next();
		while(rs.getString(c) != null){

			out.print(" " + rs.getString(c) + " ");
			c++;
		}
		out.println();
		out.println();
		for(int i =0; i < replicates; i++){
			double begin_time = System.currentTimeMillis();
			rs = stmt.executeQuery(query) ;
			double end_time = System.currentTimeMillis();
			out.println((end_time-begin_time)/1000);

			while(rs.next()){
				//Retrieve by column name
				String name  = rs.getString("student.name");
				//Display values
				//System.out.println("NAME: " + name);
			}
		}

		//3. list the name of the students who have taken course v4
		out.println("3. list the names of the students who have taken course v4 (time in ms)");
		query = "select student.name from student, course, transcript WHERE (course.crsCode = '" +
				TestData[3][3][0] + "') and (course.crsCode = transcript.crsCode and " + 
				"transcript.studID = student.id);" ;
		rs = stmt.executeQuery("explain " + query);
		out.println("Query plan: ");
		c = 1;
		rs.next();
		try{
			while(rs.getString(c) != null){

				out.print(" " + rs.getString(c) + " ");
				c++;
			}
		}catch(Exception e){

		}
		out.println();
		out.println();
		
		for(int i =0; i < replicates; i++){
			double begin_time = System.currentTimeMillis();
			rs = stmt.executeQuery(query) ;
			double end_time = System.currentTimeMillis();
			out.println((end_time-begin_time)/1000);

			while(rs.next()){
				//Retrieve by column name
				String name  = rs.getString("name");
				//Display values
				//System.out.println("NAME: " + name);
			}
		}

		//4. list the names of students who have taken a course taught by professor v5 (name)
		out.println("4. list the names of students who have taken a course taught by professor v5 (name) (time in ms)");
		query = "select student.name from student, professor, teaching, transcript WHERE (professor.id = '" +
				TestData[1][4][0] + "' and teaching.crsCode = transcript.crsCode) and teaching.semester = transcript.semester and " + 
				"transcript.studID = student.id and teaching.profID = professor.id;" ;
		rs = stmt.executeQuery("explain " + query);
		out.println("Query plan: ");
		c = 1;
		rs.next();
		try{
			while(rs.getString(c) != null){

				out.print(" " + rs.getString(c) + " ");
				c++;
			} 
		}catch(Exception e){
			
		}
		out.println();
		out.println();
		for(int i =0; i < replicates; i++){

			double begin_time = System.currentTimeMillis();
			rs = stmt.executeQuery(query) ;
			double end_time = System.currentTimeMillis();
			out.println((end_time-begin_time)/1000 );

			while(rs.next()){
				//Retrieve by column name
				String name  = rs.getString("name");
				//Display values
				//System.out.println("NAME: " + name);
			}
		}

		//5. list the names of students who have taken a course from department v6 (deptId) but not department v7
		out.println("5. list the names of students who have taken a course from department v6 (deptId) but not department v7 (time in ms)");
		query = "select student.name from student, teaching, course, transcript WHERE course.deptId = '" +
				TestData[2][5][1] + "' and course.deptID != '" + TestData[2][6][1] +
				"' and (teaching.crsCode = transcript.crsCode and teaching.semester = transcript.semester and " + 
				"transcript.studID = student.id);" ;
		rs = stmt.executeQuery("explain " + query);
		out.println("Query plan: ");
		c = 1;
		rs.next();
		while(rs.getString(c) != null){

			out.print(" " + rs.getString(c) + " ");
			c++;
		}
		out.println();
		out.println();
		for(int i =0; i < replicates; i++){

			double begin_time = System.currentTimeMillis();
			rs = stmt.executeQuery(query) ;
			double end_time = System.currentTimeMillis();
			out.println((end_time-begin_time)/1000 );

			//			while(rs.next()){
			//				//Retrieve by column name
			//				String name  = rs.getString("name");
			//				//Display values
			//				//System.out.println("NAME: " + name);
			//			}
		}

		//6. list the names of students who have taken all courses offered by department v8 (deptID) 
		out.println("6. list the names of students who have taken all courses offered by department v8 (deptID)  (time in ms)");
		query = "(select student.name from student, course, transcript WHERE course.deptId != '" +
				TestData[2][7][1] + "' and (course.crsCode = transcript.crsCode and " + 
				"transcript.studId = student.id));" ;
		rs = stmt.executeQuery("explain " + query);
		out.println("Query plan: ");
		c = 1;
		rs.next();
		while(rs.getString(c) != null){

			out.print(" " + rs.getString(c) + " ");
			c++;
		}
		out.println();
		out.println();
		for(int i =0; i < replicates; i++){

			double begin_time = System.currentTimeMillis();
			rs = stmt.executeQuery(query) ;
			double end_time = System.currentTimeMillis();
			out.println((end_time-begin_time)/1000 );

			while(rs.next()){
				//Retrieve by column name
				String name  = rs.getString("student.name");
				//Display values
				//System.out.println("NAME: " + name);
			}
		}

	} // main

} // TestTupleGenerator
