import java.sql.*;
import java.util.Date;
import java.util.Properties;

public class Project3 {

	/**
	 * Object that stores the connection to the database
	 */
    private Connection conn;
    
    /**
      * This constructor should create and initialize the connection to the database.
      * @param username the mysql username
      * @param password the mysql password
      * @param schema the mysql schema
      */
    public Project3(String username, String password, String schema) throws SQLException {
        Properties info = new Properties();
        info.put( "user", username );
        info.put( "password", password );
        conn = DriverManager.getConnection("jdbc:mysql://CSDB1901/"+schema,
                info);
    }
    
    /**  This method implements:
     * List all projects: Display for each project, its description, 
     * the name of the organization(s) that host it and the name of its first category. 
     * Outputs: a table containing for each project, its description, the name of the organization(s)
     *  that host it and the name of its first category.
     *  @return none.
     */
    public void listAllProjects() throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("" +
                "select p.description, o.name as orgname, c.name as catname " +
                "from project p " +
                "join hosts using (projid) " +
                "join organization o using (OrgNo) " +
                "join (" +
                "   select * " +
                "   from projectcat pc " +
                "   where (ranking = 1)) as tp " +
                "using (projid) " +
                "join category c using (catID)");
        ResultSet rst = pstmt.executeQuery();

        ResultSetMetaData rsmd = rst.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        for (int i = 0; i < numberOfColumns; i++) {
            System.out.print(rsmd.getColumnName(i+1) + "\t");
        }
        System.out.println();

        while (rst.next()) {
            String projectDesc = rst.getString(1); // project desciption
            String orgName = rst.getString(2); // organization name
            String catName = rst.getString(3); // rank 1 category id name
            System.out.printf("%s | %s | %s \n", projectDesc, orgName, catName);
        }

        pstmt.close();
    }


    /**  This method implements: Find project by date: Find the projects (all attributes of project) 
     * that start on a particular date. 
     * Outputs: displays all the project attributes in a table format. 
     * Error handling: print an error message if the date inputted is not valid.
     * @param date: a string  .
     */
    public void findProjectByDate(String date) {
    }

    /** Search by category: Display all the project IDs and descriptions of projects that belong to 
     * the specified category. 
     * Error handling: print an error message if the category cannot be found.
     * @param name of the category as  .
     */
    public void searchByCategory(String name) {
    }

    /** Search by keywords: Display all the project IDs and descriptions of 
     * projects whose description matches the keywords entered. 
     * Outputs: displays project IDs and descriptions of projects whose description matches the keywords entered 
     * Action: using a single SQL query, this method searches the database for the requested data.
     * Error handling: print an error message if there are no projects that match the keywords. 
     * 
     * @param keywords one or more keywords as  .
	 */
    public void searchByKeywords(String[] keywords) {
    }

    /** Change the duration of a timeslot: change the duration of a timeslot to a new value. 
     * Outputs: displays a message that the duration was changed. Returns true if successful, false otherwise 
     * Action: updates the duration of the specified timeslot in the database 
     * Error handling: print an error message if the timeslot cannot be found.
     * @param projID: project ID
     * @param date: date of timeslot
     * @param time: starting time of timeslot 
     * @param newDuration: new duration as inputted from the user.
     */
    public boolean changeDuration(int projID, String date, String time, int newDuration) {
    	return false;
    }

    /** Volunteer for a timeslot: this method allows the user to volunteer for a timeslot. 
     * Outputs: displays a message that the sign up was complete/incomplete.
     * Action: this method has two actions:
     * Make sure that the timeslot is available: It should check if the status of the project is �open� and then compare the number of volunteers registered to the number of volunteers needed for the timeslot. If more volunteers are needed for this timeslot, then proceed to Action ii. Otherwise, display a message notifying the volunteer that this timeslot is fulfilled. 
     * Insert the necessary data into the database. 
     * Error handling: print an error message if the timeslot cannot be found or the volunteer ID cannot be found.
     * @param projID: project ID
     * @param date: date of timeslot
     * @param time: starting time of timeslot
     * @param VolID: volunteer ID as inputted from the user.
     */
    public boolean volunteer(int projID, String date, String time, int VolID) {
    	return false;
    }

    /** Add a project: the user needs to specify the information about the project. 
     * Then, the application adds the new project to the database. 
     * Output: a confirmation message when the project has been inserted
     * Action: this method inserts the project into the database. There are multiple insertions and queries as part of this method. You can use helper methods to split up the work. 
     * Error handling: print an error message if the enddate is earlier than startingDate or if startingDate is earlier than today�s date.
     * @param orgNo: Organization number
     * @param //projID calculated by the application by incrementing the highest projID in the database.
     * @param description: string describing project
     * @param startingDate: date of beginning of project
     * @param endDate: date of end of project
     * @param location: location of project
     * @param nVolunteers: number of volunteers needed
     */
    public boolean addProject(int orgNo, String description, String startingDate, String endDate, String location, int nVolunteers) {
    	return true;
    }

    /**
     * This method implements the functionality necessary to exit the application: 
     * this should allow the user to cleanly exit the application properly.
     * This should close the connection and any prepared statements.  
     */
    public void exitApplication() throws SQLException {
        if(conn != null) {
            conn.close();
        }
    }

    /**
     * This is the main method that should test all the methods above.
     * It is sufficient to call each method above once.  This method should not throw any exceptions.
     */
	public static void main(String[] args) {
        try {
            Project3 db = new Project3("u266638", "p266638", "schema266638_airline");

            db.listAllProjects();

//		db.findProjectByDate("2020-02-09");
//
//		db.searchByCategory("service");
//
//		String[] stuff = {"gcc", "ayugdahd; select * from timeslot"};
//
//		db.searchByKeywords(stuff);
//
//		db.changeDuration(2212, "2021-10-24", "10:00:00", 7 );
//
//		db.volunteer(2211, "2020-02-09","14:00:00", 5688);
//
//		db.addProject(2214, "Christmas Potluck", "2023-12-24", "2023-12-24", "MAPS", 5,"open");

            db.exitApplication();

        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
	}

}
