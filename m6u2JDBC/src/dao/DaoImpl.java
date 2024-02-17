package dao;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Card;
import model.Player;


public class DaoImpl {
	
	private Connection conexion;	
	
	public static final String SCHEMA_NAME = "Uno";
	public static final String CONNECTION = "jdbc:mysql://localhost:3306/" + SCHEMA_NAME;
	public static final String USER_CONNECTION = "root";
	public static final String PASS_CONNECTION = "";
	
	public void connect() throws SQLException {
		String url = CONNECTION;
        String user = USER_CONNECTION;
        String pass = PASS_CONNECTION;
        conexion = DriverManager.getConnection(url, user, pass);
	}

	public void disconnect() throws SQLException {
		if (conexion != null) {
            conexion.close();
        }
	}	
	
	public int getLastIdCard(int playerId) throws SQLException{
		int id = 0;
		String query = "SELECT ifnull (max(id), 0) + 1 as id FROM card WHERE id_player = ?";
		try(PreparedStatement ps = conexion.prepareStatement(query)){
			ps.setInt(1, playerId);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					id = rs.getInt("id");
				}
			}
		}
		return id;
	}
	
	/**
	 * get object last Card played from game join card table
	 * @return last card played 
	 * @throws SQLException
	 */
	public Card getLastCard() throws SQLException{
		Card card = null;
		String queryIdCard = "SELECT id_card FROM game WHERE id = (SELECT MAX(id) FROM game) ";
		String queryCard = "SELECT * FROM card WHERE id = ?";
		try(PreparedStatement ps = conexion.prepareStatement(queryIdCard)){
			try (ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
				int id_card = rs.getInt("id_card");
				try(PreparedStatement ps2 = conexion.prepareStatement(queryCard)){
					ps2.setInt(1, id_card);
					try(ResultSet rs2 = ps2.executeQuery()){
						if(rs.next()) {
							int id = rs.getInt("id");
							int idPlayer = rs.getInt("id_player");
							String number = rs.getString("number");
							String color = rs.getString("color");
							card = new Card(id, number, color, idPlayer);
							
						}
					}
				}
				}
			}
		}
		return card;
	}
	
	/**
	 * get object Player by user and password from player table
	 * @param user
	 * @param pass
	 * @return
	 * @throws SQLException
	 */
	public Player getPlayer(String user, String pass) throws SQLException{
		String query = "SELECT * FROM player where user = '" + user + "' and password = '" + pass + "'";
		Player player = null;
		
		
		try(PreparedStatement ps = conexion.prepareStatement(query)) {
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					int id = rs.getInt("id");
					String name = rs.getString("name");
					int games = rs.getInt("games");
					int victories = rs.getInt("victories");
					player = new Player(id, name, games, victories);
					System.out.println(player.getName());
				}
			}
		}
		
		return player;

	}
	
	/**
	 * @param id player
	 * @return list of hand cards, they are in card table but they are not in game table(played)
	 * @throws SQLException
	 */
	public ArrayList<Card> getCards(int playerId) throws SQLException{
		ArrayList<Card> cards = new ArrayList<Card>();
		String query = "SELECT * FROM card LEFT JOIN GAME ON card.id = game.id_card WHERE id_player =" + playerId + " and game.id = " + null;
		
		try(PreparedStatement ps = conexion.prepareStatement(query)) {
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					Card card = new Card(playerId, query, query, playerId);
					cards.add(card);
				}
			}
		}
		return cards;
	}
	
	/**
	 * @param id card
	 * @return object card from card table by id_card
	 * @throws SQLException
	 */
	public Card getCard(int cardId) throws SQLException{
		String query = "SELECT * FROM card WHERE id = " + cardId;
		
		try(PreparedStatement ps = conexion.prepareStatement(query)){
			try(ResultSet rs = ps.executeQuery()){
				int id = rs.getInt("id");
				int idPlayer = rs.getInt("id_player");
				String number = rs.getString("number");
				String color = rs.getString("color");
				Card card = new Card(id, number, color, idPlayer);
				return card;
			}
		}
	}
	
	/**
	 * insert new game with cardId
	 * @param card
	 * @throws SQLException
	 */
	public void saveGame(Card card) throws SQLException{
		String query = "INSERT INTO game (id_card) VALUES (?)";
		
		try(PreparedStatement ps = conexion.prepareStatement(query)){
			ps.setInt(1, card.getId());
			ps.executeUpdate();
		}
	}
	
	/**
	 * insert new card with card fields
	 * @param card
	 * @throws SQLException
	 */
	public void saveCard(Card card) throws SQLException{
		String query = "INSERT INTO card (id_player, number, color) values (?,?,?)";
		try(PreparedStatement ps = conexion.prepareStatement(query)){
			ps.setInt(1, card.getId());
			ps.setString(2, card.getNumber());
			ps.setString(3, card.getColor());
			ps.executeUpdate();
		}
	}
	
	/**
	 * delete last card from game table if it was a end game card(change side or skip)
	 * @param card
	 * @throws SQLException
	 */
	public void deleteCard(Card card) throws SQLException{
		String query = "DELETE FROM game WHERE id_card = ?";
		try(PreparedStatement ps = conexion.prepareStatement(query)){
			ps.setInt(1, card.getId());
			ps.executeUpdate();
		}
		
	}
	
	/**
	 * delete all records from card and game tables
	 * @throws SQLException
	 */
	public void clearDeck(int playerId) throws SQLException{
		String query = "DELETE FROM card WHERE id = ?";
		try(PreparedStatement ps = conexion.prepareStatement(query)){
			ps.setInt(1, playerId);
			ps.executeUpdate();
		}
	}
	
	/**
	 * update victories field if the game ends successfully using player id
	 * @param playerId
	 * @throws SQLException
	 */
	public void addVictories(int playerId) throws SQLException{
		String query = "UPDATE player SET victories = vistories + 1 WHERE id = ?";
		try(PreparedStatement ps = conexion.prepareStatement(query)){
			ps.setInt(1, playerId);
			ps.executeUpdate();
		}
	}
	
	/**
	 * update games field if the game ends using player id
	 * @param playerId
	 * @throws SQLException
	 */
	public void addGames(int playerId) throws SQLException{
		String query = "UPDATE player SET games = games + 1 WHERE id = ?";
		try(PreparedStatement ps = conexion.prepareStatement(query)){
			ps.setInt(1, playerId);
			ps.executeUpdate();
		}
	}


}
