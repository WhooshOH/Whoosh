
import java.sql.DriverManager;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Tables {
	static final String DB_URL = "jdbc:mysql://localhost/";
	static final String USER = "root";
	static final String PW = "root";

	public static void main(String[] args) {


	}
	// consider returning string instead of boolean for error reports
	// or if going hard returning specific errors...

	public static String getSalt() {
		String strSalt = null;
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[16];
			sr.nextBytes(salt);
			strSalt = salt.toString();
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return strSalt;
	}
	
	// encrypt password
	//taken from: https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
	public static String encrypt(String password, String salt) {
        String generatedPass = null;
		try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
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
	public static boolean signIn(String email, String password) {
		
		String sql = "SELECT EncryptedPassword, Salt FROM Host WHERE Email = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, sql);
			ResultSet rs = ps.executeQuery(sql);
			ps.close();
			
			
			if(rs.next()) {
				String storedPass = rs.getString("EncryptedPassword");
				String salt = rs.getString("Salt");
				
				String encrypted = encrypt(password, salt);
				
				return encrypted.equals(storedPass);
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}

	/*
	 * Find host’s table and check if it there’s currently a session If so, return
	 * an error Else Create a SessionID Store SessionID in Host’s table Return
	 * SessionID
	 * 
	 */
	public static boolean sessionGeneration(String hostEmail) {

		

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			String sql = "SELECT TABLE Hosts WHERE Email = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery(sql);
			
			if (rs.next()) {
				sql = "UPDATE Hosts SET InSession = ? WHERE Email = ?";
				ps = conn.prepareStatement(sql);
				ps.setString(1, "True");
				ps.setString(2, hostEmail);
				ps.executeUpdate(sql);
				
				ps.close();
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}
	
	/*
	 * Find email If email not found, return an error message saying so Else send
	 * reset password link to found email Return message saying an email has been
	 * sent
	 * 
	 */
	public static boolean resetPasswordRequest(String email) {

		String sql = "SELECT Email FROM Hosts WHERE Email = " + email;
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);) {
			if (rs.next()) {

				// send link to email
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Find table using email Encrypt email with new password Store encrypted
	 * password in table Send message back to indicate successful reset
	 * 
	 */
	public static boolean resetPassword(String hostEmail, String password) {


		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			
			String sql = "SELECT Salt FROM Hosts WHERE Email = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			String salt = ps.executeQuery().getString("Salt");
			
			String newEncryptedPass = encrypt(password, salt);
			
			sql = "UPDATE Hosts SET EncryptedPassword = ? WHERE Email = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, newEncryptedPass);
			ps.setString(2, hostEmail);
			ps.executeUpdate(sql);
			
			ps.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// fullname vs first and last name?
	// split into first last
	/*
	 * Find table using email Update name in Host/Guest table Send message back to
	 * indicate successful update
	 */

	public static boolean updateName(String email, String fName, String lName, boolean host) {

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			
			String sql = "UPDATE ? SET fName = ?, lName = ? WHERE Email = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			
			if (host) {
				ps.setString(1, "Hosts");
			} else {
				ps.setString(1,  "Guests");
			}
			
			ps.setString(2, fName);
			ps.setString(3, lName);
			ps.setString(4, email);
			ps.executeUpdate(sql);
			ps.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}

	/*
	 * Find table using email Update name in Host/Guest table Send message back to
	 * indicate successful update
	 * 
	 */
	public static boolean updatePronouns(String email, String pronouns, boolean host) {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			
			String sql = "UPDATE ? SET Pronouns = ? WHERE Email = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			
			if (host) {
				ps.setString(1, "Hosts");
			} else {
				ps.setString(1,  "Guests");
			}
			
			ps.setString(2, pronouns);
			ps.setString(3, email);
			ps.executeUpdate(sql);
			ps.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}

	public static boolean joinRoom(String guestEmail, String hostEmail) {
		/**
		 * Check to ensure SessionID is valid If SessionID invalid: send back a message
		 * saying so Else Store SessionID in Guest table Return successful message
		 * 
		 */

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			String sql = "SELECT Email FROM Hosts WHERE Email = ?";
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.setString(1, hostEmail);
			ResultSet rs1 = pr.executeQuery(sql);

			// ensure valid email/session
			if (rs1.next()) {
				
				
				// ensure guest has no current session
				sql = "SELECT SessionID FROM Guests WHERE Email = ?";
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, guestEmail);
				ResultSet rs2 = ps.executeQuery(sql);

				if (rs2.getString("SessionID").equals("")) {
					// no current session
					sql = "UPDATE Guests SET SessionID = ? WHERE Email = ?";
					ps = conn.prepareStatement(sql);
					ps.setString(1, hostEmail);
					ps.setString(2, guestEmail);
					ps.executeUpdate(sql);
					
					rs1.close();
					rs2.close();
					pr.close();
					
					return true;
				}
				
				rs2.close();
			}
			
			rs1.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// guests leaving session
	public static boolean leaveSession(String guestEmail) {
		String sql = "UPDATE Guests SET SessionID = ? WHERE Email = ?";
		try(Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
			PreparedStatement pr = conn.prepareStatement(sql);) {
			
			pr.setString(1, "");
			pr.setString(2, guestEmail);
			pr.executeUpdate();
			
			return true;
				
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	

	// host ends session
	/**
	 * Delete SessionID in host table 
	 * Delete Guest instances Send message back to
	 * indicate successful session end
	 * 
	 */
	public static boolean endSession(String hostEmail) {
		
		String sql = "SELECT InSession FROM Hosts WHERE Email = ?";
		try(Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.setString(1, hostEmail);
			ResultSet rs = pr.executeQuery();
			
			//ensure the host exists
			if(rs.next()) {
				
				//ensure host is in session
				if(rs.getString("InSession").equals("True")) {
					sql = "UPDATE Hosts SET session ?";
					pr = conn.prepareStatement(sql);
					pr.setString(1, "False");
					pr.executeUpdate();
					
					sql = "UPDATE Guests SET sessionID = ? WHERE sessionID = ?";
					pr = conn.prepareStatement(sql);
					pr.setString(1,  "");
					pr.setString(2, hostEmail);
					pr.executeUpdate();
					
					rs.close();
					pr.close();
					return true;
				}
			}

				
			pr.close();
			rs.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	public static boolean validUSCEmail(String email) {
		email = email.toLowerCase();
		int index = email.indexOf("@");

		// pretty sure usc emails need to be at least 3 chars long
		if (index < 3) {
			return false;
		}

		String last = email.substring(index);
		if (!last.equals("@usc.edu")) {
			return false;
		}

		return true;
	}

	// todo: check valid email, no duplicate email
	public static boolean insertGuest(String email, String fName, String lName, String pronouns, String sessionID) {
		String sql = "INSERT INTO HOSTS (email, name, pronouns, encryptedPassword) VALUES (?,?,?,?,?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement pr = conn.prepareStatement(sql);) {
			// get and check for duplicate email

			pr.setString(1, email);
			pr.setString(2, fName);
			pr.setString(3, lName);
			pr.setString(4, pronouns);
			pr.setString(4, "");
			pr.executeUpdate();
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// todo: check valid email, no duplicate email
	public static boolean insertHost(String email, String fName, String lName, String pronouns, String password) {
		String sql = "INSERT INTO HOSTS (Email, FName, LName, Pronouns, InSession, " +
				"EncryptedPassword, Salt) VALUES (?,?,?,?,?,?,?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement pr = conn.prepareStatement(sql);) {
			// get and check for duplicate email
			String salt = getSalt();
			
			pr.setString(1, email);
			pr.setString(2, fName);
			pr.setString(3, lName);
			pr.setString(4, pronouns);
			pr.setString(5, "False");
			pr.setString(6, encrypt(email, salt));
			pr.setString(7, salt);
			pr.executeUpdate();
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void createHostTable() {
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

	public static void createGuestTable() {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();) {
			String sql = "CREATE TABLE Hosts" +
					"(Email STRING not NULL, " + 
					"FName STRING not NULL, " +
					"LName STRING, " + 
					"Pronouns STRING, " +
					"SessionID STRING not NULL, " +					"Salt not NULL, " + 
					"PRIMARY KEY (Email) )";
			
			stmt.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}