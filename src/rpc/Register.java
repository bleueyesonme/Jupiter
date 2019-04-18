package rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Register
 */
@WebServlet(name = "register", urlPatterns = { "/register" })
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			String firstname = input.getString("first_name");
			String lastname = input.getString("last_name");
			
			JSONObject code = new JSONObject();
			if(connection.registerUser(userId, password, firstname, lastname)) {
				code.put("status", "OK");
			} else {
				code.put("status", "User Already Exists");
			}
			
			RpcHelper.writeJsonObject(response, code);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if(session == null) {
			try {
				RpcHelper.writeJsonObject(response, new JSONObject().put("status", "Session Invalid"));
				response.setStatus(403);				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return;
		}
		
	  	 DBConnection connection = DBConnectionFactory.getConnection();
	  	 try {
	  		 //String userId = session.getAttribute("user_id").toString();
	  		 JSONObject input = RpcHelper.readJSONObject(request);
	  		 String userId = input.getString("user_id");
	  		 String password = input.getString("password");
	  		 
	  		 JSONObject code = new JSONObject();
  			 String name = connection.getFullname(userId);
	  		 if(connection.deleteUser(userId, password)) {
	  			 session.invalidate();	  				 
  				 code.put("result", "User Has Been Deleted Successful");
	  		 } else {
	  			 code.put("result", "User OR Password is Invalid"); 				 
	  		 }
	  		 code.put("user_id", userId).put("name", name);
	  		 RpcHelper.writeJsonObject(response, code);
	  	 } catch (Exception e) {
	  		 e.printStackTrace();
	  	 } finally {
	  		 connection.close();
	  	 }
	}
	
}
