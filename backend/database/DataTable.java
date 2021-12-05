import java.sql.*;

public class DataTable {
	static final String DB_URL = "jdbc:mysql://localhost:3306/cs201final?";// ?allowPublicKeyRetrieval=true&useSSL=false";
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
		String sql = "CREATE TABLE Guests" + "(Email VARCHAR(100) not NULL, " + "FName VARCHAR(100) not NULL, "
				+ "LName VARCHAR(100), " + "Pronouns VARCHAR(100), " + "SessionID VARCHAR(100) not NULL, "
				+ "Active VARCHAR(5) not NULL, " + "PRIMARY KEY (Email) )";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.executeUpdate();

		} catch (SQLSyntaxErrorException e) {
			sql = "DROP TABLE Guests";

			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
				PreparedStatement pr = conn.prepareStatement(sql);
				pr.executeUpdate();

				sql = "CREATE TABLE Guests" + "(Email VARCHAR(100) not NULL, " + "FName VARCHAR(100) not NULL, "
						+ "LName VARCHAR(100), " + "Pronouns VARCHAR(100), " + "SessionID VARCHAR(100) not NULL, "
						+ "Active VARCHAR(5) not NULL, " + "PRIMARY KEY (Email) )";
				pr = conn.prepareStatement(sql);
				pr.executeUpdate();
				pr.close();

			} catch (Exception ex) {
				System.out.println("Something went wrong!");
			}
		} catch (Exception e) {
			System.out.println("Something went wrong!");
		}

	}

	public static void createHostTable() {

		String sql = "CREATE TABLE Hosts" + "(Email VARCHAR(100) not NULL, " + "FName VARCHAR(100) not NULL, "
				+ "LName VARCHAR(100), " + "Pronouns VARCHAR(100), " + "InSession VARCHAR(5) not NULL, "
				+ "LoggedIn VARCHAR(5) not NULL,  " + "EncryptedPassword VARCHAR(1000) not NULL,"
				+ "Salt VARCHAR(1000) not NULL, " + "PRIMARY KEY (Email) )";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.executeUpdate();

		} catch (SQLSyntaxErrorException e) {
			sql = "DROP TABLE Hosts";

			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
				PreparedStatement pr = conn.prepareStatement(sql);
				pr.executeUpdate();

				sql = "CREATE TABLE Hosts" + "(Email VARCHAR(100) not NULL, " + "FName VARCHAR(100) not NULL, "
						+ "LName VARCHAR(100), " + "Pronouns VARCHAR(100), " + "InSession VARCHAR(5) not NULL, "
						+ "LoggedIn VARCHAR(5) not NULL,  " + "EncryptedPassword VARCHAR(1000) not NULL,"
						+ "Salt VARCHAR(1000) not NULL, " + "PRIMARY KEY (Email) )";

				pr = conn.prepareStatement(sql);
				pr.executeUpdate();
				pr.close();

			} catch (Exception ex) {
				System.out.println("Something went wrong!");
			}
		} catch (Exception e) {
			System.out.println("Something went wrong!");
		}

	}

	// todo: check valid email, no duplicate email
	public static String insertHost(String email, String fName, String lName, String pronouns, String password) {
		if (Guest.invalidLength(email) || Guest.invalidLength(fName) || Guest.invalidLength(lName)
				|| Guest.invalidLength(pronouns)) {
			return "Entered field of invalid length.";
		}

		if (password.length() < 8 || password.length() > 20) {
			return "Please enter a password between 8 to 20 characters long.";
		}

		String sql = "SELECT LoggedIn FROM Hosts WHERE Email = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String loggedIn = rs.getString("LogIn");
				if (loggedIn.equals("True")) {
					return "You're already logged in.";
				}

				return "Your account already exists.";
			}

			sql = "INSERT INTO HOSTS (Email, FName, LName, Pronouns, InSession, LoggedIn, "
					+ "EncryptedPassword, Salt) VALUES (?,?,?,?,?,?,?,?)";

			// get and check for duplicate email
			String salt = Host.getSalt();
			String encryptedPassword = Host.encrypt(password, salt);

			ps.setString(1, email);
			ps.setString(2, fName);
			ps.setString(3, lName);
			ps.setString(4, pronouns);
			ps.setString(5, "False");
			ps.setString(6, "True");
			ps.setString(7, encryptedPassword);
			ps.setString(8, salt);
			ps.executeUpdate();

			return "";
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not insert host.";
	}

	// todo: check valid email, no duplicate email
	private static String insertGuest(String email, String fName, String lName, String pronouns, String sessionID) {

		if (Guest.invalidLength(email) || Guest.invalidLength(fName) || Guest.invalidLength(lName)
				|| Guest.invalidLength(pronouns) || Guest.invalidLength(sessionID)) {
			return "Could not insert host.";
		}

		String sql = "SELECT Email FROM Guests WHERE Email = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement pr = conn.prepareStatement(sql);
			ResultSet rs = pr.executeQuery();

			if (rs.next()) {
				return "You're already logged in.";
			} else {
				sql = "INSERT INTO GUESTS (Email, FName, LName, Pronouns, SessionID, Active) VALUES (?,?,?,?,?,?)";
				pr = conn.prepareStatement(sql);
				pr.setString(1, email);
				pr.setString(2, fName);
				pr.setString(3, lName);
				pr.setString(4, pronouns);
				pr.setString(5, sessionID);
				pr.setString(6, "False");
				pr.executeUpdate();
				return "";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "Could not insert guest.";
	}

}