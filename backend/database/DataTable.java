import java.sql.*;

public class DataTable {
	static final String DB_URL = "jdbc:mysql://localhost:3306/cs201final?";//?allowPublicKeyRetrieval=true&useSSL=false";
	static final String USER = "root";
	static final String PW = "root";
	
	public static void main(String[] args) {
		createGuestTable();
//		String sql = "Connection ?";
//		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
//			dt = new DataTable();
//			System.out.println(sql);
//			DataTable.createGuestTable();
//			DataTable.createHostTable();
//			insertHost("test@usc.edu", "Bob", "Joe", "he/him/his/", "testSession");
//			insertGuest("test2@usc.edu", "Leon", "Zha", "he/him/his/", "test@usc.edu");
//			Guest.updateName("test@usc.edu", "Luncida", "Quintal?", false);
//			System.out.println("num guests: " + Host.getNumGuestsInSession("test@usc.edu"));
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
			

	}

	public static void initialize() {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			DataTable.createGuestTable();
			DataTable.createHostTable();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void createGuestTable() {
		String sql = "CREATE TABLE Guests" +
				"(Email VARCHAR(100) not NULL, " + 
				"FName VARCHAR(100) not NULL, " +
				"LName VARCHAR(100), " + 
				"Pronouns VARCHAR(100), " +
				"SessionID VARCHAR(100) not NULL, " +					
				"Active VARCHAR(5) not NULL, " + 
				"PRIMARY KEY (Email) )";
		
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {		
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.executeUpdate();
			
		} catch(SQLSyntaxErrorException e) {
			sql = "DROP TABLE Guests";
			
			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {		
				PreparedStatement pr = conn.prepareStatement(sql);
				pr.executeUpdate();
				
				sql = 	"CREATE TABLE Guests" +
						"(Email VARCHAR(100) not NULL, " + 
						"FName VARCHAR(100) not NULL, " +
						"LName VARCHAR(100), " + 
						"Pronouns VARCHAR(100), " +
						"SessionID VARCHAR(100) not NULL, " +					
						"Active VARCHAR(5) not NULL, " + 
						"PRIMARY KEY (Email) )";
				pr = conn.prepareStatement(sql);
				pr.executeUpdate();
				pr.close();
				
			} catch(Exception ex) {
				System.out.println("Something went wrong!");
			}
		} catch(Exception e) {
			System.out.println("Something went wrong!");
		}
		
	}
	

	public static void createHostTable() {
		
		String sql = "CREATE TABLE Hosts" +
				"(Email VARCHAR(100) not NULL, " + 
				"FName VARCHAR(100) not NULL, " +
				"LName VARCHAR(100), " + 
				"Pronouns VARCHAR(100), " +
				"InSession VARCHAR(5) not NULL, " +
				"EncryptedPassword VARCHAR(1000) not NULL," +
				"Salt VARCHAR(1000) not NULL, " + 
				"PRIMARY KEY (Email) )";
		
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {		
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.executeUpdate();
			
		} catch(SQLSyntaxErrorException e) {
			sql = "DROP TABLE Hosts";
			
			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {		
				PreparedStatement pr = conn.prepareStatement(sql);
				pr.executeUpdate();
				
				sql = 	"CREATE TABLE Hosts" +
						"(Email VARCHAR(100) not NULL, " + 
						"FName VARCHAR(100) not NULL, " +
						"LName VARCHAR(100), " + 
						"Pronouns VARCHAR(100), " +
						"InSession VARCHAR(5) not NULL, " +
						"EncryptedPassword VARCHAR(1000) not NULL," +
						"Salt VARCHAR(1000) not NULL, " + 
						"PRIMARY KEY (Email) )";
				
				pr = conn.prepareStatement(sql);
				pr.executeUpdate();
				pr.close();
				
			} catch(Exception ex) {
				System.out.println("Something went wrong!");
			}
		} catch(Exception e) {
			System.out.println("Something went wrong!");
		}

	}
	

	

	// todo: check valid email, no duplicate email
	public static String insertHost(String email, String fName, String lName, String pronouns, String password) {
		if(Guest.invalidLength(email) || Guest.invalidLength(fName) || Guest.invalidLength(lName) ||
				Guest.invalidLength(pronouns) ) {
			return "Could not insert host.";
		}
		
		if(password.length() < 8 || password.length() > 20) {
			return "Could not insert host.";
		}
		
		String sql = "INSERT INTO HOSTS (Email, FName, LName, Pronouns, InSession, " +
				"EncryptedPassword, Salt) VALUES (?,?,?,?,?,?,?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement pr = conn.prepareStatement(sql);) {
			// get and check for duplicate email
			String salt = Host.getSalt();
			String encryptedPassword = Host.encrypt(password, salt);
			
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
		
		if(Guest.invalidLength(email) || Guest.invalidLength(fName) || Guest.invalidLength(lName) ||
				Guest.invalidLength(pronouns) || Guest.invalidLength(sessionID)) {
			return "Could not insert host.";
		}
			
		
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