import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Host extends Guest {
	// consider returning string instead of boolean for error reports
	// or if going hard returning specific errors...

	// consider: adding array of guests here

	public static String getSalt() {
		String strSalt = null;
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[16];
			sr.nextBytes(salt);
			strSalt = salt.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return strSalt;
	}

	// encrypt password
	// taken from:
	// https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
	public static String encrypt(String password, String salt) {
		String generatedPass = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt.getBytes());
			byte[] bytes = md.digest(password.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPass = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generatedPass;

	}

	/**
	 * SHA salt and hash ok Use password to encrypt email and check it against all
	 * stored emails Send back message to indicate successful login/unsuccessful
	 * login
	 * 
	 */
	public static String logIn(String email, String password) {

		String sql = "SELECT EncryptedPassword, Salt FROM Host WHERE Email = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			//checking credentials
			if (rs.next()) {
				String storedPass = rs.getString("EncryptedPassword");
				String salt = rs.getString("Salt");

				String encrypted = encrypt(password, salt);
				
				if (encrypted.equals(storedPass)) {
					
					//ensure not already logged in
					sql = "SELECT LoggedIn FROM Hosts WHERE Email = ?";

					ps = conn.prepareStatement(sql);
					ps.setString(1, email);
					rs = ps.executeQuery();
						
					if(rs.next()) {
						String loggedIn = rs.getString("LoggedIn");
						if(loggedIn.equals("True")) {
							return "You're already logged in.";
						}
					}

					
					//actually logging in
					sql = "UPDATE Hosts SET LogIn = ? WHERE Email = ?";
					ps = conn.prepareStatement(sql);
					ps.setString(1, "True");
					ps.setString(2, email);
					ps.executeUpdate();
					
					ps.close();
					rs.close();
					return "";

				}
				rs.close();
				ps.close();	
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Username/Password incorrect";

	}

	/*
	 * Find host’s table and check if it there’s currently a session If so, return
	 * an error Else Create a SessionID Store SessionID in Host’s table Return
	 * SessionID
	 * 
	 */
	
	
	public static String sessionGeneration(String hostEmail) {
		String sql = "UPDATE Hosts SET InSession = ? WHERE Email = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement ps = conn.prepareStatement(sql);
			
				
				ps = conn.prepareStatement(sql);
				ps.setString(1, "True");
				ps.setString(2, hostEmail);
				ps.executeUpdate();
				ps.close();
				
				return "";

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not generate session.";

	}

	public static String getNumGuestsInSession(String hostEmail) {
		String sql = "SELECT COUNT(*) FROM Guests WHERE SessionID = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, hostEmail);
			ResultSet rs = ps.executeQuery();

			// 0 is the column index...I think. SQL indexes generally start with 1
			String numGuests = "";
			if(rs.next()) {
				numGuests = rs.getString(1);
				rs.close();
			} else {
				numGuests = "0";
			}
			System.out.println("Here: " + numGuests);

			return numGuests;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * Find email If email not found, return an error message saying so Else send
	 * reset password link to found email Return message saying an email has been
	 * sent
	 * 
	 */
	private static String resetPasswordRequest(String email) {

		String sql = "SELECT Email FROM Hosts WHERE Email = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {

				// send link to email
				return "";
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Failed to send password reset link. Try again.";
	}
	
	

	/*
	 * Find table using email Encrypt email with new password Store encrypted
	 * password in table Send message back to indicate successful reset
	 * 
	 */
	private static String resetPassword(String hostEmail, String password) {

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {

			String sql = "SELECT Salt FROM Hosts WHERE Email = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, hostEmail);
			ResultSet rs = ps.executeQuery();
			
			String salt = "";
			if(rs.next()) {
				salt = ps.executeQuery().getString("Salt");
				rs.close();
			}

			String newEncryptedPass = encrypt(password, salt);

			sql = "UPDATE Hosts SET EncryptedPassword = ? WHERE Email = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, newEncryptedPass);
			ps.setString(2, hostEmail);
			ps.executeUpdate();

			ps.close();
			return "";

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Password not reset successfully. Try again.";
	}

	// host ends session
	/**
	 * Delete SessionID in host table Delete Guest instances Send message back to
	 * indicate successful session end
	 * 
	 */
	public static String endSession(String hostEmail) {

		String sql = "SELECT InSession FROM Hosts WHERE Email = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.setString(1, hostEmail);
			ResultSet rs = pr.executeQuery();
			
			// ensure the host exists
			if (rs.next()) {

				// ensure host is in session
				if (rs.getString("InSession").equals("True")) {
					sql = "UPDATE Hosts SET InSession = ? WHERE Email = ?";
					pr = conn.prepareStatement(sql);
					pr.setString(1, "False");
					pr.setString(2, hostEmail);
					pr.executeUpdate();
					
					sql = "DELETE FROM Guests WHERE sessionID = ?";
					pr = conn.prepareStatement(sql);
					pr.setString(1, hostEmail);
					pr.executeUpdate();

					rs.close();
					pr.close();
					return "";
				}
			}

			pr.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Session failed to end. Try again. ";
	}
	
	/**
	 * Logout
	 */
	
	public static String logOut(String email) {
		String sql = "UPDATE Hosts SET LoggedIn = ? WHERE Email = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, "False");
			ps.setString(2, email);
			ps.executeUpdate();
			ps.close();
			return "";
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return "Couldn't log out, try again.";
	}
	
	public static String isInSession(String email) {
		String sql = "SELECT InSession FROM Host WHERE Email = ?";
		try(Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				String inSession = rs.getString("InSession");
				if(inSession.equals("True")) {
					return "";
				}
				
				return "Host not in session.";
			}
			return "Host account doesn't exist, try looking for another session.";
		
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return "Couldn't determine if in session. Try again. ";
	}
	

}
