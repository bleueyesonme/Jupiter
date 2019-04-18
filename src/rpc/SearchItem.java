package rpc;

import java.io.IOException;
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
 * Servlet implementation class SearchItem
 */
@WebServlet(name = "search", urlPatterns = { "/search" })
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
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
		
		String userId = session.getAttribute("user_id").toString();
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		String keyword = request.getParameter("keyword");
		
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			List<Item> items = connection.searchItems(lat, lon, keyword);
			Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
			
			JSONArray array = new JSONArray();
			for(Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
				array.put(obj);
			}
			RpcHelper.writeJsonArray(response, array);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
		
//		String username = request.getParameter("username");
//		if(username != null) {
//			JSONArray array = new JSONArray();
//			try {
//				array.put(new JSONObject().put("title", "Hello")).put(new JSONObject().put("username", username));
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			RpcHelper.writeJsonArray(response, array);
//		}
//		else {
//			PrintWriter writer = response.getWriter();
//			response.setContentType("text/html");
//			writer.println("<html><body>");
//			writer.println("<h1>Hello World</h1>");
//			writer.println("</body></html>");
//			writer.close();
//		}
//
//		if(request.getParameter("username") != null) {
//			String username = request.getParameter("username");
//			
//			JSONObject obj = new JSONObject();
//			try {
//				obj.put("title", "Hello");
//				obj.put("username", username);
//			} catch (JSONException e1) {
//				e1.printStackTrace();
//			}
//			writer.print(obj);	
//		}
//		else {
//			writer.println("<html><body>");
//			writer.println("<h1>Hello World</h1>");
//			writer.println("</body></html>");	
//		}
		
	}

}
