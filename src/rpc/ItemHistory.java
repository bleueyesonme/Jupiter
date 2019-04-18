package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet(name = "history", urlPatterns = { "/history" })
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			String userId = session.getAttribute("user_id").toString();
			// String userId = request.getParameter("user_id");
			Set<Item> items = connection.getFavoriteItems(userId);
			JSONArray array = new JSONArray();
			for(Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.append("favorite", true);
				array.put(obj);
			}
			RpcHelper.writeJsonArray(response, array);
			
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
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
			String userId = session.getAttribute("user_id").toString();
			// String userId = input.getString("user_id");
			JSONObject input = RpcHelper.readJSONObject(request);
			JSONArray array = input.getJSONArray("favorite");
			List<String> itemIds = new ArrayList<>();
			for(int i = 0; i < array.length(); i++) {
				itemIds.add(array.getString(i));
			}
			connection.setFavoriteItems(userId, itemIds);
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
			
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
			 String userId = session.getAttribute("user_id").toString();
	  		 // String userId = input.getString("user_id");
	  		 JSONObject input = RpcHelper.readJSONObject(request);
			 JSONArray array = input.getJSONArray("favorite");
	  		 List<String> itemIds = new ArrayList<>();
	  		 for(int i = 0; i < array.length(); i++) {
	  			 itemIds.add(array.getString(i));
	  		 }
	  		 connection.unsetFavoriteItems(userId, itemIds);
	  		 RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
	  		 
	  	 } catch (Exception e) {
	  		 e.printStackTrace();
	  	 } finally {
	  		 connection.close();
	  	 }
	}

}
