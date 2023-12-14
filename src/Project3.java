import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Properties;
import static java.lang.String.format;

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
    public void findProjectByDate(String date) throws SQLException {
        String invalidDate = "Error: Given date is invalid. Use valid date of format YYYY-MM-DD";

        // Check if date is valid
        if (date.length() != 10) {
            System.out.println(invalidDate);
            return;
        }
        String yearS = date.substring(0,4);
        String monthS = date.substring(5,7);
        String dayS = date.substring(8,10);
        int year;
        int month;
        int day;

        try {
            year = Integer.parseInt(yearS);
            month = Integer.parseInt(monthS);
            day = Integer.parseInt(dayS);
        } catch (Exception e) {
            System.out.println(invalidDate);
            return;
        }

        boolean valid = false;
        if (year > 0 && year <= 2023) { // check year
            if (month >= 1 && month <= 12) { // check month
                if (day >= 1) {
                    if ( (month == 4 || month == 6 || month == 9 || month == 11) && day <= 30) {  // check months with 30 days
                        // date is valid
                    } else if (month == 2) { // check february
                        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {  // is leap year
                            if (day <= 29) {
                                valid = true;
                            }
                        } else {  // is not a leap year
                            if (day <= 28) {
                                valid = true;
                            }
                        }
                    } else {  // remaining months have 31 days
                        if (day <= 31) {
                            valid = true;
                        }
                    }
                }
            }
        }

        if (!valid) {
            System.out.println(invalidDate);
            return;
        }

        date = yearS + "-" + monthS + "-" + dayS;  // reconstruct date with good spacers

        PreparedStatement pstmt = conn.prepareStatement("" +
                "select *\n" +
                "from project \n" +
                "where startingDate = ?");
        pstmt.setString(1, date);
        ResultSet rst = pstmt.executeQuery();

        ResultSetMetaData rsmd = rst.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        for (int i = 0; i < numberOfColumns; i++) {
            System.out.print(rsmd.getColumnName(i+1) + "\t");
        }
        System.out.println();

        while (rst.next()) {
            int projID = rst.getInt(1); // project id
            String desc = rst.getString(2); // project description
            Date startingDate = rst.getDate(3); // starting date
            Date endDate = rst.getDate(4); // ending date
            String location = rst.getString(5); // project location
            int volsNeeded = rst.getInt(6); // number of volunteers needed
            String status = rst.getString(7); // project status
            System.out.printf("%d | %s | %s | %s | %s | %d | %s \n", projID, desc, startingDate, endDate, location, volsNeeded, status);
        }

        pstmt.close();
    }

    /** Search by category: Display all the project IDs and descriptions of projects that belong to 
     * the specified category. 
     * Error handling: print an error message if the category cannot be found.
     * @param name of the category as  .
     */
    public void searchByCategory(String name) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("" +
                "select projID, p.description\n" +
                "from category c\n" +
                "join projectcat\n" +
                "using (catID)\n" +
                "join project p\n" +
                "using (projID)\n" +
                "where c.name = ?\n");
        pstmt.setString(1, name);
        ResultSet rst = pstmt.executeQuery();

        String table = "";
        int rows = 0;

        ResultSetMetaData rsmd = rst.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        for (int i = 0; i < numberOfColumns; i++) {
            table += rsmd.getColumnName(i+1) + "\t";
        }
        table += "\n";


        while (rst.next()) {
            int projID = rst.getInt(1); // project id
            String desc = rst.getString(2); // project description
            table += format("%d | %s \n", projID, desc);
            rows++;
        }

        if (rows == 0) {
            System.out.println("Error: Category cannot be found");
        } else {
            System.out.println(table);
        }

        pstmt.close();
    }

    /** Search by keywords: Display all the project IDs and descriptions of 
     * projects whose description matches the keywords entered. 
     * Outputs: displays project IDs and descriptions of projects whose description matches the keywords entered 
     * Action: using a single SQL query, this method searches the database for the requested data.
     * Error handling: print an error message if there are no projects that match the keywords. 
     * 
     * @param keywords one or more keywords as  .
	 */
    public void searchByKeywords(String[] keywords) throws SQLException {
        StringBuilder allWords = new StringBuilder();
        for (int i = 1; i < keywords.length; i++) {
            allWords.append("or description like ? ");
        }
        PreparedStatement pstmt = conn.prepareStatement("" +
                "select projID, description " +
                "from project " +
                "where description like ? " +
                allWords
        );
        for (int i = 0; i < keywords.length; i++) {
            pstmt.setString(i+1, "%"+keywords[i]+"%");
        }
        ResultSet rst = pstmt.executeQuery();

        String table = "";
        int rows = 0;

        ResultSetMetaData rsmd = rst.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        for (int i = 0; i < numberOfColumns; i++) {
            table += rsmd.getColumnName(i+1) + "\t";
        }
        table += "\n";


        while (rst.next()) {
            int projID = rst.getInt(1); // project id
            String description = rst.getString(2); // project description
            table += format("%d | %s \n", projID, description);
            rows++;
        }

        if (rows == 0) {
            System.err.println("Error: Descriptions with all given keywords cannot be found");
        } else {
            System.out.println(table);
        }
//        ResultSetMetaData rsmd = rst.getMetaData();
//        int numberOfColumns = rsmd.getColumnCount();
//        for (int i = 0; i < numberOfColumns; i++) {
//            System.out.print(rsmd.getColumnName(i+1) + "\t");
//        }
//        System.out.println();
//
//        int nothingCount = 0;
//        while (rst.next()) {
//            nothingCount++;
//            int projID = rst.getInt(1); // project id
//            String description = rst.getString(2); // project description
//            System.out.printf("%d | %s \n", projID, description);
//        }
//        if(nothingCount == 0){System.err.println("No descriptions with all the given keywords");}
        pstmt.close();
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
    public boolean changeDuration(int projID, String date, String time, int newDuration) throws SQLException {
            PreparedStatement pstmt = conn.prepareStatement("" +
                    "update timeslot " +
                    "set duration = ? " +
                    "where projID = ? " +
                    "and Day = ? " +
                    "and startingTime = ? "
            );
            pstmt.setInt(1, newDuration);
            pstmt.setInt(2, projID);
            pstmt.setString(3, date);
            pstmt.setString(4, time);

        // pstmt is ready, launch it
            int rows = pstmt.executeUpdate();
            if(rows>0){
                System.out.println("Duration change successful");
                return true;
            }
            else{
                System.err.println("Duration change failed: cannot find timeslot");
                return false;
            }

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
    public boolean volunteer(int projID, String date, String time, int VolID) throws SQLException {
        //TODO: print an error message if the timeslot cannot be found or the volunteer ID cannot be found.
        PreparedStatement pstmt1 = conn.prepareStatement("" +
                        "select status " +
                        "from project " +
                        "where projID = ? " +
                        "and status = \"open\" "
            );
        pstmt1.setInt(1, projID);
        ResultSet rst1 = pstmt1.executeQuery();

        if(rst1.next()) { // if has an element and thus is open
            PreparedStatement pstmtCompare1 = conn.prepareStatement("" +
                    "select count(VolID) as comparison " +
                    "from projvolunteers " +
                    "where projID = ? " +
                    "and Day = ? " +
                    "and startingTime = ? "
            );
            pstmtCompare1.setInt(1, projID);
            pstmtCompare1.setString(2, date);
            pstmtCompare1.setString(3, time);

            ResultSet rstC1 = pstmtCompare1.executeQuery();
            PreparedStatement pstmtCompare2 = conn.prepareStatement("" +
                    "select nVolneeded " +
                    "from timeslot " +
                    "where projID = ? " +
                    "and Day = ? " +
                    "and startingTime = ? "
            );
            pstmtCompare2.setInt(1, projID);
            pstmtCompare2.setString(2, date);
            pstmtCompare2.setString(3, time);
            ResultSet rstC2 = pstmtCompare2.executeQuery();

            rstC1.next();
            int comparison = rstC1.getInt(1) + 1;

            rstC2.next();
            int volsNeeded = rstC1.getInt(1) + 1;


            if(comparison > volsNeeded) { //TODO: Make sure this is correct!
                rstC1.close();
                rstC2.close();
                // ii
                PreparedStatement pstmt2 = conn.prepareStatement("" +
                        "insert into projvolunteers VALUES (?,?,?,?) "
                );
                pstmt1.setInt(1, VolID);
                pstmt1.setInt(2, projID);
                pstmt1.setString(2, date);
                pstmt1.setString(2, time);
                int rows = pstmt2.executeUpdate();
                if(rows>0){
                    System.out.println("Insert successful");
                    rstC1.close();
                    rstC2.close();
                    return true;
                } else {
                    rstC1.close();
                    rstC2.close();
                    System.out.println("Insert failed");
                    return false;
                }
            } else {
                System.out.println("Volunteer not needed");
                return false;
            }

        } else {
            System.out.println("Status not open");
            return false;
        }
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
    public boolean addProject(int orgNo, String description, String startingDate, String endDate, String location, int nVolunteers) throws SQLException {

        // test date validity
        String yearS = startingDate.substring(0,4);
        String monthS = startingDate.substring(5,7);
        String dayS = startingDate.substring(8,10);
        int syear;
        int smonth;
        int sday;
        Date start;

        try {
            syear = Integer.parseInt(yearS);
            smonth = Integer.parseInt(monthS);
            sday = Integer.parseInt(dayS);
            start = new Date(syear, smonth, sday);
        } catch (Exception e) {
            System.out.println("Error: Start date is invalid");
            return false;
        }

        String yearE = endDate.substring(0,4);
        String monthE = endDate.substring(5,7);
        String dayE = endDate.substring(8,10);
        int eyear;
        int emonth;
        int eday;
        Date end;

        try {
            eyear = Integer.parseInt(yearE);
            emonth = Integer.parseInt(monthE);
            eday = Integer.parseInt(dayE);
        } catch (Exception e) {
            System.out.println("Error: End date is invalid");
            return false;
        }

        LocalDate currtime = LocalDate.now();
        if (eyear<syear || (eyear==syear && emonth<smonth) || (eyear==syear && emonth==smonth && eday<sday)) {
            System.out.println("Error: Ending date before start date");
            return false;
        } else if (syear<currtime.getYear() || (syear==currtime.getYear() && smonth<currtime.getMonthValue()) || (syear== currtime.getYear() && smonth==currtime.getMonthValue() && sday<currtime.getDayOfMonth())) {
            System.out.println("Error: Starting date is earlier than today's date");
            return false;
        }

        // find projid
        PreparedStatement findprojid = conn.prepareStatement("" +
                "select max(projID)\n" +
                "from project");
        ResultSet findprojidrst = findprojid.executeQuery();
        findprojidrst.next();
        int projid = findprojidrst.getInt(1) + 1;
        findprojid.close();


        PreparedStatement pstmt = conn.prepareStatement("" +
                "insert into project value(?, ?, ?, ?, ?, ?, \"open\");");
        pstmt.setInt(1, projid);
        pstmt.setString(2, description);
        pstmt.setString(3, startingDate);
        pstmt.setString(4, endDate);
        pstmt.setString(5, location);
        pstmt.setInt(6, nVolunteers);
        int rows = pstmt.executeUpdate();

        pstmt.close();

        PreparedStatement hostinsert = conn.prepareStatement("" +
                "insert into hosts value(?, ?)");
        hostinsert.setInt(1, orgNo);
        hostinsert.setInt(2, projid);
        int hostrows = hostinsert.executeUpdate();

        hostinsert.close();

        System.out.println("Project successfully inserted");


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
            Project3 db = new Project3("u268614", "p268614", "schema268614_airline");


            db.volunteer(2211, "2022-02-09", "14:00:00", 5678);

            //db.changeDuration(2213, "2021-09-06", "12:00:00", 7);
            //db.searchByKeywords(new String[] {"Baby", "Shower"});
//            db.listAllProjects();

//		      db.findProjectByDate("2020-02-09");  // test valid date no project
//            db.findProjectByDate("2022-02-09");  // test valid date with project
//            db.findProjectByDate("4578-72-12");  // test invalid date
//            db.findProjectByDate("rjkafahf;u");  // test invalid format


//		      db.searchByCategory("service");  // test valid category name
//            db.searchByCategory("dsfjhk");  // test invalid category name
//            db.searchByCategory("evil; insert into category value(213, \"evil\", \"testing evil stuff\");"); // test dangerous category name
//
		    //String[] stuff = {"gcc", "ayugdahd; select * from timeslot"};

		    //db.searchByKeywords(stuff);
//
//		    db.changeDuration(2212, "2021-10-24", "10:00:00", 7 );
//
//		    db.volunteer(2211, "2020-02-09","14:00:00", 5688);
//
		    //db.addProject(10001, "Christmas Potluck", "2023-12-24", "2023-12-24", "MAPS", 5);  // test valid insert
            //db.addProject(10001, "test", "2023-12-24", "2023-12-23", "MAPS", 5); // test ending before start
            //db.addProject(10001, "Halloween potluck", "2023-10-24", "2023-12-24", "MAPS", 5); // test starting before today



            db.exitApplication();

        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
	}

}
