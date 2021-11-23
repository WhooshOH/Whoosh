package wooshBackend;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.sql.*;
import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.Session;
import javax.mail.Transport;

public class Tables {
	static final String DB_URL = "jdbc:mysql://localhost/";
	static final String USER = "root";
	static final String PW = "root";

	public static void main(String[] args) {

		// need to create db/server and connect?
		createHostTable();
		createGuestTable();

		// etc.

	}
	// consider returning string instead of boolean for error reports
	// or if going hard returning specific errors...

	
	/**
	 * SHA salt and hash ok Use password to encrypt email and check it against all
	 * stored emails Send back message to indicate successful login/unsuccessful
	 * login
	 * 
	 */
	public boolean signIn(String email, String password) {

		// encrypt password
		String sql = "SELECT EncryptedEmail FROM Guest WHERE Email = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, sql);
			ps.executeUpdate(sql);
			ps.close();
			return true;

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
	public boolean sessionGeneration(String hostEmail) {

		String sql = "SELECT TABLE Hosts WHERE Email = " + hostEmail;

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);) {

			if (rs.next()) {
				sql = "UPDATE Hosts SET InSession = ? WHERE Email = ?";
				PreparedStatement ps = conn.prepareStatement(sql);
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
	public boolean resetPasswordRequest(String email) {

		String sql = "SELECT Email FROM Hosts WHERE Email = " + email;
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);) {
			if (rs.next()) {
				String link = "";
				// send link to email
				sendEmail(email, link);
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void sendEmail(String email, String link) {
		  //email ID of user.
	      String recipient = link;
	      // email ID of  Sender.
	      String sender = "woosh@gmail.com";
	      // using host as localhost
	      String host = "127.0.0.1";
	      // Getting system properties
	      Properties properties = System.getProperties();
	      // Setting up mail server
	      properties.setProperty("mail.smtp.host", host);
	      // creating session object to get properties
	      Session session = Session.getDefaultInstance(properties);
	      try
	      {
	         // MimeMessage object.
	         MimeMessage message = new MimeMessage(session);
	         // Set From Field: adding senders email to from field.
	         message.setFrom(new InternetAddress(sender));
	         // Set To Field: adding recipient's email to from field.
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
	         // Set Subject: subject of the email
	         message.setSubject("WooshOH Password Reset");
	         // set body of the email.
	         message.setContent("<p>Hello,</p> <p>Here is the link to reset your password </p> <p><a href=\"link\" target=\"_blank\"></a><p>","text/html");
	         // Send email.
	         Transport.send(message);
	         //System.out.println("Mail successfully sent");
	      }
	      catch (MessagingException mex)
	      {
	         mex.printStackTrace();
	      }
	   }
		
	/*
	 * Find table using email Encrypt email with new password Store encrypted
	 * password in table Send message back to indicate successful reset
	 * 
	 */
	public boolean resetPassword(String hostEmail, String password) {

		String encryptedEmail = "";
		// salt and hash email with password

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			String sql = "UPDATE Guests SET EncryptedEmail = ? WHERE Email = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, encryptedEmail);
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

	public boolean updateName(String email, String fName, String lName, boolean host) {

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();) {
			
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
	public boolean updatePronouns(String email, String pronouns, boolean host) {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();) {
			
			String sql = "UPDATE ? SET pronouns = ? WHERE Email = ?";
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

	public boolean joinRoom(String guestEmail, String hostEmail) {
		/**
		 * Check to ensure SessionID is valid If SessionID invalid: send back a message
		 * saying so Else Store SessionID in Guest table Return successful message
		 * 
		 */

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			String sql = "SELECT email FROM Hosts WHERE email = ?";
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.setString(1, hostEmail);
			ResultSet rs1 = pr.executeQuery(sql);

			// valid email/session
			if (rs1.next()) {
				
				
				// ensure guest has no current session
				sql = "SELECT sessionID FROM Guests WHERE email = ?";
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, guestEmail);
				ResultSet rs2 = ps.executeQuery(sql);

				if (rs2.getString("sessionID").equals("")) {
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
	public boolean leaveSession(String guestEmail) {
		String sql = "UPDATE Guests SET sessionID = ? WHERE Email = ?";
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
	public boolean endSession(String hostEmail) {
		
		String sql = "SELECT InSession FROM Hosts WHERE Email = ?";
		try(Connection conn = DriverManager.getConnection(DB_URL, USER, PW)) {
			PreparedStatement pr = conn.prepareStatement(sql);
			pr.setString(1, hostEmail);
			ResultSet rs = pr.executeQuery();
			
			//ensure hosts are in session? 
			if(rs.next()) {
				if(!(rs.getString("Email").equals(""))) {
					sql = "UPDATE Hosts SET session ?";
					pr = conn.prepareStatement(sql);
					pr.setString(1, "");
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
	public static boolean insertStudent(String email, String name, String pronouns, String sessionID) {
		String sql = "INSERT INTO HOSTS (email, name, pronouns, encryptedEmail) VALUES (?,?,?,?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement stmt = conn.prepareStatement(sql);) {
			// get and check for duplicate email

			stmt.setString(1, email);
			stmt.setString(2, name);
			stmt.setString(3, pronouns);
			stmt.setString(4, "");
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// todo: check valid email, no duplicate email
	public static boolean insertHost(String email, String name, String pronouns, String encryptedEmail) {
		String sql = "INSERT INTO HOSTS (email, name, pronouns, encryptedEmail) VALUES (?,?,?,?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				PreparedStatement stmt = conn.prepareStatement(sql);) {
			// get and check for duplicate email

			stmt.setString(1, email);
			stmt.setString(2, name);
			stmt.setString(3, pronouns);
			stmt.setString(4, encryptedEmail);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void createHostTable() {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();) {
			String sql = "CREATE TABLE Hosts";
			stmt.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createGuestTable() {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PW);
				Statement stmt = conn.createStatement();) {
			String sql = "CREATE TABLE Guests";
			stmt.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

