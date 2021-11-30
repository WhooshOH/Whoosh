import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Host extends Guest {
	// consider returning string instead of boolean for error reports
	// or if going hard returning specific errors...
	
	//consider: adding array of guests here 
	
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
	private String logIn(String email, String password) {
		
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
				
				if(encrypted.equals(storedPass)) return "";
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Username/Password Incorrect";

	}
	

	/*
	 * Find host’s table and check if it there’s currently a session If so, return
	 * an error Else Create a SessionID Store SessionID in Host’s table Return
	 * SessionID
	 * 
	 */
	private String sessionGeneration(String hostEmail) {

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
				return "";
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could Not Generate Session";

	}
	
	private int getNumGuestsInSession(String hostEmail) {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			String sql = "SELECT COUNT(*) FROM Guests WHERE Email = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, hostEmail);
			ResultSet rs = ps.executeQuery(sql);
			
			//0 is the column index...I think. SQL indexes generally start with 1
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;

	}
	
	
	/*
	 * Find email If email not found, return an error message saying so Else send
	 * reset password link to found email Return message saying an email has been
	 * sent
	 * 
	 */
	private boolean resetPasswordRequest(String email) {

		String sql = "SELECT Email FROM Hosts WHERE Email = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			
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
	private boolean resetPassword(String hostEmail, String password) {


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
	



	// host ends session
	/**
	 * Delete SessionID in host table 
	 * Delete Guest instances Send message back to
	 * indicate successful session end
	 * 
	 */
	public boolean endSession(String hostEmail) {
		
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


	

	
	
	
	
}
