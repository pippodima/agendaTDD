package dimartinofilippo.agenda;

import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.repository.mongo.ToDoMongoRepository;
import dimartinofilippo.agenda.repository.sql.ToDoSQLRepository;
import dimartinofilippo.agenda.transaction.TransactionManager;
import dimartinofilippo.agenda.transaction.mongo.MongoTransactionManager;
import dimartinofilippo.agenda.transaction.sql.SQLTransactionManager;
import dimartinofilippo.agenda.view.swing.ToDoSwingView;

public class App {

	public static void main(String[] args) {
		String dbType = args.length > 0 ? args[0].toLowerCase() : "sql"; // default to SQL/PostgreSQL

		try {
			ToDoRepository todoRepository;
			TransactionManager transactionManager;

			if ("mongo".equals(dbType)) {
				String mongoHost = args.length > 1 ? args[1] : "localhost";
				int mongoPort = args.length > 2 ? Integer.parseInt(args[2]) : 27017;
				String mongoUri = "mongodb://" + mongoHost + ":" + mongoPort;

				MongoClient mongoClient = MongoClients.create(mongoUri);
				todoRepository = new ToDoMongoRepository(mongoClient);
				transactionManager = new MongoTransactionManager(todoRepository);
				System.out.println("Using MongoDB: " + mongoUri);

			} else {
				String jdbcUrl = args.length > 1 ? args[1] : "jdbc:postgresql://localhost:5432/agenda";
				String username = args.length > 2 ? args[2] : "postgres";
				String password = args.length > 3 ? args[3] : "password";

				String databaseName = "agenda";

				Class.forName("org.postgresql.Driver");

				createDatabaseIfNotExists(jdbcUrl, username, password, databaseName);

				DataSource dataSource = new DataSource() {
					@Override
					public Connection getConnection() throws java.sql.SQLException {
						return DriverManager.getConnection(jdbcUrl, username, password);
					}

					@Override
					public Connection getConnection(String user, String pass) throws java.sql.SQLException {
						return DriverManager.getConnection(jdbcUrl, user, pass);
					}

					@Override
					public <T> T unwrap(Class<T> iface) {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean isWrapperFor(Class<?> iface) {
						return false;
					}

					@Override
					public java.io.PrintWriter getLogWriter() {
						throw new UnsupportedOperationException();
					}

					@Override
					public void setLogWriter(java.io.PrintWriter out) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void setLoginTimeout(int seconds) {
						throw new UnsupportedOperationException();
					}

					@Override
					public int getLoginTimeout() {
						return 0;
					}

					@Override
					public java.util.logging.Logger getParentLogger() {
						throw new UnsupportedOperationException();
					}
				};

				try (Connection conn = dataSource.getConnection()) {
					createSqlSchema(conn);
				}

				todoRepository = new ToDoSQLRepository(dataSource);
				Connection transactionConnection = dataSource.getConnection();
				transactionManager = new SQLTransactionManager(todoRepository, transactionConnection);

				System.out.println("Using PostgreSQL database: " + jdbcUrl);
			}

			EventQueue.invokeLater(() -> {
				try {
					ToDoSwingView todoView = new ToDoSwingView();
					AgendaController agendaController = new AgendaController(transactionManager, todoView);
					todoView.setAgendaController(agendaController);
					todoView.setVisible(true);

					agendaController.allToDos();
				} catch (Exception e) {
					System.err.println("Failed to initialize GUI: " + e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
			});

		} catch (Exception e) {
			System.err.println("Failed to start application: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void createDatabaseIfNotExists(String jdbcUrl, String username, String password, String dbName)
			throws Exception {
		String defaultUrl = jdbcUrl.replace("/" + dbName, "/postgres");
		try (Connection conn = DriverManager.getConnection(defaultUrl, username, password);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'")) {

			if (!rs.next()) {
				stmt.executeUpdate("CREATE DATABASE " + dbName);
				System.out.println("Database '" + dbName + "' created.");
			}
		}
	}

	private static void createSqlSchema(Connection connection) {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS todos (" + "id SERIAL PRIMARY KEY,"
					+ "title VARCHAR(255) NOT NULL UNIQUE," + "done BOOLEAN NOT NULL" + ")");
		} catch (Exception e) {
			throw new RuntimeException("Failed to create SQL schema", e);
		}
	}
}
