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


	/**
	 * Check to ensure SessionID is valid If SessionID invalid: send back a message
	 * saying so Else Store SessionID in Guest table Return successful message
	 * 
	 */
	public static String joinRoom(String guestEmail, String hostEmail) {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			String sql = "SELECT Email FROM Hosts WHERE Email = ?";
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.setString(1, hostEmail);
			ResultSet rs1 = pr.executeQuery();

			// ensure valid email/session
			if (rs1.next()) {
				
				
				// ensure guest has no current session
				sql = "SELECT SessionID FROM Guests WHERE Email = ?";
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, guestEmail);
				ResultSet rs2 = ps.executeQuery();

				if (rs2.getString("SessionID").equals("")) {
					// no current session
					sql = "UPDATE Guests SET Active = ? WHERE Email = ?";
					ps = conn.prepareStatement(sql);
					ps.setString(1, "True");
					ps.setString(2, guestEmail);
					ps.executeUpdate();
					
					rs1.close();
					rs2.close();
					pr.close();
					
					
					
					return "";
				}
				
				rs2.close();
				rs1.close();
				pr.close();
			}
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not join room.";
	}

	// guests leaving session
	public static String leaveSession(String guestEmail) {
		String sql = "UPDATE Guests SET SessionID = ?, Active = ? WHERE Email = ?";
		try(Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
			PreparedStatement pr = conn.prepareStatement(sql);) {
			
			pr.setString(1, "");
			pr.setString(2, "False");
			pr.setString(3, guestEmail);
			pr.executeUpdate();
			
			return "";
				
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return "Could not leaave room.";
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
			return false;
		}
		return true;
	}





	
}
