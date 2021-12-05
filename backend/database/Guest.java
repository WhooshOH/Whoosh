import java.sql.*;

public class Guest {
	static final String DB_URL = "jdbc:mysql://localhost:3306/cs201final?";
	static final String USER = "root";
	static final String PW = "root";

	// fullname vs first and last name?
	// split into first last
	/*
	 * Find table using email Update name in Host/Guest table Send message back to
	 * indicate successful update
	 */

	public static String updateName(String email, String fName, String lName, boolean host) {
		String sql = "";
		if(host) {
			sql = "UPDATE Hosts SET fName=?, lName=? WHERE Email=?";
		} else {
			sql = "UPDATE Guests SET fName=?, lName=? WHERE Email=?";
		}
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setString(1, fName);
			ps.setString(2, lName);
			ps.setString(3, email);
			ps.executeUpdate();
			
			return "";

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not update name. Try again.";

	}

	/*
	 * Find table using email Update name in Host/Guest table Send message back to
	 * indicate successful update
	 * 
	 */
	public static String updatePronouns(String email, String pronouns, boolean host) {
		String sql = "";
		if(host) {
			sql = "UPDATE Hosts SET Pronouns = ? WHERE Email = ?";
		} else {
			sql = "UPDATE Guests SET Pronouns = ? WHERE Email = ?";
		}
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setString(1, pronouns);
			ps.setString(2, email);
			ps.executeUpdate();
			
			return "";

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not update pronouns. Try again.";

	}



	// guests leaving session
	public static String leaveSession(String guestEmail) {
		String sql = "DELETE FROM Guests WHERE guestEmail = ?";
		try(Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.setString(1, guestEmail);
			pr.executeUpdate();
			pr.close();
			
			return "";
				
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return "Could not leave session.";
	}
	


	public static boolean validUSCEmail(String email) {
		email = email.toLowerCase();
		int index = email.indexOf("@");

		// pretty sure usc emails need to be at least 3 chars long
		if (index < 3 || index > 92) {
			return false;
		}

		String last = email.substring(index);
		if (!last.equals("@usc.edu")) {
			return false;
		}

		return true;
	}
	
	public static boolean invalidLength(String str) {
		if(str.length() > 100 || str.length() < 1) {
			return true;
		}
		return false;
	}





	
}
