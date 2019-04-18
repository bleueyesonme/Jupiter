package rpc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import recommendation.GeoRecommendation;

/**
 * Servlet implementation class RecommendItem
 */
@WebServlet(name = "recommendation", urlPatterns = { "/recommendation" })
public class RecommendItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecommendItem() {
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
		
		GeoRecommendation recommendation = new GeoRecommendation();
		List<Item> recommendationItems = recommendation.recommendationItems(userId, lat, lon);
		
		JSONArray array = new JSONArray();
		for(Item item : recommendationItems) {
			array.put(item.toJSONObject());
		}
		
		RpcHelper.writeJsonArray(response, array);		
		
//		try {
//			JSONObject obj1 = new JSONObject();
//			obj1.put("name", "Dufferin SenG").put("address", "San Francisco").put("time", "03/14/2019");
//			array.put(obj1);
//			array.put(new JSONObject().put("name", "Xin Liu").put("address", "Santa Clara").put("time", "03/14/2019"));
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}

	}

}
