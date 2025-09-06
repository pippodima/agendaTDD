package dimartinofilippo.agenda;

import java.awt.EventQueue;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.repository.mongo.ToDoMongoRepository;
import dimartinofilippo.agenda.repository.sql.ToDoSQLRepository;
import dimartinofilippo.agenda.view.swing.ToDoSwingView;

public class App {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					ToDoRepository todoRepository;
					
					String dbType = "mongo";
					if (args.length > 0) {
						dbType = args[0].toLowerCase();
					}
					
					if ("sql".equals(dbType) || "h2".equals(dbType)) {
						String jdbcUrl = "jdbc:h2:mem:agenda;DB_CLOSE_DELAY=-1";
						String username = "pippo";
						String password = "";
						
						if (args.length > 1) jdbcUrl = args[1];
						if (args.length > 2) username = args[2];
						if (args.length > 3) password = args[3];
						
						JdbcDataSource dataSource = new JdbcDataSource();
						dataSource.setURL(jdbcUrl);
						dataSource.setUser(username);
						dataSource.setPassword(password);
						
						createSqlSchema(dataSource);
						
						todoRepository = new ToDoSQLRepository(dataSource);
						System.out.println("Using SQL database: " + jdbcUrl);
						
					} else {
						String mongoHost = "localhost";
						int mongoPort = 27017;
						
						if (args.length > 1) mongoHost = args[1];
						if (args.length > 2) mongoPort = Integer.parseInt(args[2]);
						
						String mongoUri = "mongodb://" + mongoHost + ":" + mongoPort;
						MongoClient mongoClient = MongoClients.create(mongoUri);
						todoRepository = new ToDoMongoRepository(mongoClient);
						System.out.println("Using MongoDB: " + mongoUri);
					}
					
					ToDoSwingView todoView = new ToDoSwingView();
					AgendaController agendaController = new AgendaController(todoRepository, todoView);
					todoView.setAgendaController(agendaController);
					
					todoView.setVisible(true);
					
					agendaController.allToDos();
					
				} catch (Exception e) {
					System.err.println("Failed to start application: " + e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
			}
		});
	}
	
	private static void createSqlSchema(DataSource dataSource) {
		try (var connection = dataSource.getConnection();
			 var statement = connection.createStatement()) {
			
			statement.executeUpdate(
				"CREATE TABLE IF NOT EXISTS todos (" +
				"title VARCHAR(255) PRIMARY KEY," +
				"done BOOLEAN NOT NULL" +
				")"
			);
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to create SQL schema", e);
		}
	}

}
