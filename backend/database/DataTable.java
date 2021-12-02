import java.sql.*;

public class DataTable {
	static final String DB_URL = "jdbc:mysql://localhost/";
	static final String USER = "root";
	static final String PW = "root";

	public static void main(String[] args) {
		
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();) {
			System.out.println("Connection made! ");

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

	private void createGuestTable() {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();) {
			String sql = "CREATE TABLE Hosts" +
					"(Email STRING not NULL, " + 
					"FName STRING not NULL, " +
					"LName STRING, " + 
					"Pronouns STRING, " +
					"SessionID STRING not NULL, " +					
					"Active not NULL, " + 
					"PRIMARY KEY (Email) )";
			
			stmt.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	public void createHostTable() {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();) {
			String sql = "CREATE TABLE Hosts" +
				"(Email STRING not NULL, " + 
				"FName STRING not NULL, " +
				"LName STRING, " + 
				"Pronouns STRING, " +
				"InSession STRING not NULL, " +
				"EncryptedPassword not NULL," +
				"Salt not NULL, " + 
				"PRIMARY KEY (Email) )";
		
			stmt.executeUpdate(sql);

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
			pr.setString(5, "");
			pr.setString(6, "false");
			pr.executeUpdate();
			
			return "Successfully inserted guest.";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not insert guest.";
	}
	



}