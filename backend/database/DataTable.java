import java.sql.*;

public class DataTable {
	static final String DB_URL = "jdbc:mysql://localhost:3306/cs201final?";//?allowPublicKeyRetrieval=true&useSSL=false";
	static final String USER = "root";
	static final String PW = "root";

	public static void main(String[] args) {
		String sql = "Connection ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			DataTable dt = new DataTable();
			System.out.println(sql);
			dt.createGuestTable();
			dt.createHostTable();
			insertGuest("test@usc.edu", "Leon", "Zha", "he/him/his/", "testSession");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
//		DataTable dt = new DataTable();
//		
//		dt.createGuestTable();
//		dt.createHostTable();
//		
//		Host h = new Host();
//		while(true) {
//			//get frontend input to do various stuff
//			//e.g. create guest instances, create host instances,
//			//start sessions, etc. 
//		}
//		

	}

	
	//add checks for limits and strig and hash
	private void createGuestTable() {
		
		String sql = "DROP TABLE Guests";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {		
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.executeUpdate();
			sql = 	"CREATE TABLE Guests" +
					"(Email VARCHAR(20) not NULL, " + 
					"FName VARCHAR(20) not NULL, " +
					"LName VARCHAR(20), " + 
					"Pronouns VARCHAR(20), " +
					"SessionID VARCHAR(20) not NULL, " +					
					"Active VARCHAR(20) not NULL, " + 
					"PRIMARY KEY (Email) )";
			pr = conn.prepareStatement(sql);
			pr.executeUpdate();
			pr.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	public void createHostTable() {
		String sql = "DROP TABLE Hosts";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.executeUpdate();
			
			sql = "CREATE TABLE Hosts" +
			"(Email VARCHAR(40) not NULL, " + 
			"FName VARCHAR(20) not NULL, " +
			"LName VARCHAR(20), " + 
			"Pronouns VARCHAR(20), " +
			"InSession VARCHAR(20) not NULL, " +
			"EncryptedPassword VARCHAR(100) not NULL," +
			"Salt VARCHAR(100) not NULL, " + 
			"PRIMARY KEY (Email) )";
			pr = conn.prepareStatement(sql);
			pr.executeUpdate();
			pr.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	// todo: check valid email, no duplicate email
	public String insertHost(String email, String fName, String lName, String pronouns, String password) {
		String sql = "INSERT INTO HOSTS (Email, FName, LName, Pronouns, InSession, " +
				"EncryptedPassword, Salt) VALUES (?,?,?,?,?,?,?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement pr = conn.prepareStatement(sql);) {
			// get and check for duplicate email
			String salt = Host.getSalt();
			String encryptedPassword = Host.encrypt(email, salt);
			
			pr.setString(1, email);
			pr.setString(2, fName);
			pr.setString(3, lName);
			pr.setString(4, pronouns);
			pr.setString(5, "False");
			pr.setString(6, encryptedPassword);
			pr.setString(7, salt);
			pr.executeUpdate();
						
			return "Sucessfully inserted host.";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not insert host.";
	}
	

	// todo: check valid email, no duplicate email
	private static String insertGuest(String email, String fName, String lName, String pronouns, String sessionID) {
		String sql = "INSERT INTO GUESTS (Email, FName, LName, Pronouns, SessionID, Active) VALUES (?,?,?,?,?,?)";
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement pr = conn.prepareStatement(sql);) {
			// get and check for duplicate email

			pr.setString(1, email);
			pr.setString(2, fName);
			pr.setString(3, lName);
			pr.setString(4, pronouns);
			pr.setString(5, sessionID);
			pr.setString(6, "false");
			pr.executeUpdate();
			
			return "Successfully inserted guest.";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not insert guest.";
	}
	



}