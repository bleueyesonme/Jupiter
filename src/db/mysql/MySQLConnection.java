package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

// MySQL DB operation class (various methods, coding).
public class MySQLConnection implements DBConnection {

	private Connection conn;
	   
	public MySQLConnection() {
		try {
			System.out.println("Connecting to " + MySQLDBUtil.URL);
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
	  		conn = DriverManager.getConnection(MySQLDBUtil.URL);
	  		
	  	} catch (Exception e) {
	  		e.printStackTrace();
	  	}
	}
	   
	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
	  		} catch (Exception e) {
	  			e.printStackTrace();
	  		}
	  	}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
        if (conn == null) {
        	System.err.println("DB connection failed");
        	return;
        }

		try {
			String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for(String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
        if (conn == null) {
        	System.err.println("DB connection failed");
        	return;
        }
        
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for(String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
        if (conn == null) {
        	System.err.println("DB connection failed");
//        	return new HashSet<>();
        	return new TreeSet<>();
        }
		
//		Set<String> itemIds = new HashSet<>();
		Set<String> itemIds = new TreeSet<>();
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				String itemId = rs.getString("item_id");
				itemIds.add(itemId);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return itemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
        if (conn == null) {
        	System.err.println("DB connection failed");
//        	return new HashSet<>();
        	return new TreeSet<>();
        }
        
//		Set<Item> favoriteItems = new HashSet<>();
		Set<Item> favoriteItems = new TreeSet<>(new Comparator<Item>() {
	        @Override
			public int compare(Item e1, Item e2) {
				return e1.getItemId().compareTo(e2.getItemId());
			}
		});
		
		Set<String> itemIds = getFavoriteItemIds(userId);
		
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			for(String itemId : itemIds) {
				ps.setString(1, itemId);
				
				ResultSet rs = ps.executeQuery();
				ItemBuilder builder = new ItemBuilder();
				
				while(rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getFloat("rating"));
					
					favoriteItems.add(builder.build());
				}
				rs.close();
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
        if (conn == null) {
        	System.err.println("DB connection failed");
        	return new HashSet<>();
        }
        
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, itemId);

			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				categories.add(rs.getString("category"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String keyword) {
		TicketMasterAPI api = new TicketMasterAPI();
		List<Item> items = api.search(lat, lon, keyword);
		
		for(Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
        if (conn == null) {
        	System.err.println("DB connection failed");
        	return;
        }
 		 
 		 try {
 			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getName());
			ps.setFloat(3, item.getRating());
			ps.setString(4, item.getAddress());
			ps.setString(5, item.getImageUrl());
			ps.setString(6, item.getUrl());
			ps.setDouble(7, item.getDistance());
			ps.execute();
			
			sql = "INSERT IGNORE INTO categories VALUES (?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			for(String category : item.getCategories()) {
				ps.setString(2, category);
				ps.execute();
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
        	System.err.println("DB connection failed");
			return "";
		}
		
        // sql injection
        // select * from users where username = '' AND password = '';

        // username: fakeuser ' OR 1 = 1; DROP  --
        // select * from users where username = 'fakeuser ' OR 1 = 1 --' AND password = '';
		
		String name = "";
		try {
			String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?"; // use "?" is security aware (SQL injection)
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);

			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				name = rs.getString("first_name") + " " +rs.getString("last_name");
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			//System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
        	System.err.println("DB connection failed");
			return false;
		}
		
		try {
			String sql = "SELECT user_id, password FROM users WHERE user_id = ? AND password = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			//System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		if(conn == null) {
        	System.err.println("DB connection failed");
			return false;
		}
		
		try {
			String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			ps.setString(3, firstname);
			ps.setString(4, lastname);

			boolean result = ps.executeUpdate() == 1;
			ps.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}
	
	@Override
	public boolean deleteUser(String userId, String password) {
		if(conn == null) {
        	System.err.println("DB connection failed");
			return false;
		}
		
		String sql = "DELETE FROM users WHERE user_id = ? AND password = ?";
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			
			boolean result = ps.executeUpdate() == 1;
			ps.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
