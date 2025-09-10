package dimartinofilippo.agenda;

import java.awt.EventQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import dimartinofilippo.agenda.controller.AgendaController;
import dimartinofilippo.agenda.repository.ToDoRepository;
import dimartinofilippo.agenda.repository.mongo.ToDoMongoRepository;
import dimartinofilippo.agenda.repository.sql.ToDoSQLRepository;
import dimartinofilippo.agenda.transaction.TransactionManager;
import dimartinofilippo.agenda.transaction.mongo.MongoTransactionManager;
import dimartinofilippo.agenda.transaction.sql.SQLTransactionManager;
import dimartinofilippo.agenda.view.swing.ToDoSwingView;

public class App {
	
    private static final Logger logger = LoggerFactory.getLogger(App.class);


    public static void main(String[] args) {
        String dbType = args.length > 0 ? args[0].toLowerCase() : "sql"; // default to SQL/PostgreSQL

        try {
            ToDoRepository todoRepository;
            TransactionManager transactionManager;

            if ("mongo".equals(dbType)) {
                var mongoSetup = setupMongoDB(args);
                todoRepository = mongoSetup.todoRepository();
                transactionManager = mongoSetup.transactionManager();
            } else {
                var sqlSetup = setupPostgreSQL(args);
                todoRepository = sqlSetup.todoRepository();
                transactionManager = sqlSetup.transactionManager();
            }

            initializeGUI(todoRepository, transactionManager);

        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    private static MongoSetup setupMongoDB(String[] args) {
        String mongoHost = args.length > 1 ? args[1] : "localhost";
        int mongoPort = args.length > 2 ? Integer.parseInt(args[2]) : 27017;
        String mongoUri = "mongodb://" + mongoHost + ":" + mongoPort;

        MongoClient mongoClient = MongoClients.create(mongoUri);
        ToDoRepository todoRepository = new ToDoMongoRepository(mongoClient);
        TransactionManager transactionManager = new MongoTransactionManager(todoRepository);
        
        logger.info("Using MongoDB: " + mongoUri);
        return new MongoSetup(todoRepository, transactionManager);
    }

    private static SqlSetup setupPostgreSQL(String[] args) throws Exception {
        String jdbcUrl = args.length > 1 ? args[1] : "jdbc:postgresql://localhost:5432/agenda";
        String username = args.length > 2 ? args[2] : "postgres";
        String password = args.length > 3 ? args[3] : "password";
        String databaseName = "agenda";

        Class.forName("org.postgresql.Driver");
        createDatabaseIfNotExists(jdbcUrl, username, password, databaseName);

        DataSource dataSource = createDataSource(jdbcUrl, username, password);
        initializeSqlSchema(dataSource);

        ToDoRepository todoRepository = new ToDoSQLRepository(dataSource);
        Connection transactionConnection = dataSource.getConnection();
        TransactionManager transactionManager = new SQLTransactionManager(todoRepository, transactionConnection);

        logger.info("Using PostgreSQL database: " + jdbcUrl);
        return new SqlSetup(todoRepository, transactionManager);
    }

    private static DataSource createDataSource(String jdbcUrl, String username, String password) {
        return new DataSource() {
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
    }

    private static void initializeSqlSchema(DataSource dataSource) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            createSqlSchema(conn);
        }
    }

    private static void initializeGUI(ToDoRepository todoRepository, TransactionManager transactionManager) {
        EventQueue.invokeLater(() -> {
            try {
                ToDoSwingView todoView = new ToDoSwingView();
                AgendaController agendaController = new AgendaController(transactionManager, todoView);
                todoView.setAgendaController(agendaController);
                todoView.setVisible(true);

                agendaController.allToDos();
            } catch (Exception e) {
                handleGuiInitializationError(e);
            }
        });
    }

    private static void handleStartupError(Exception e) {
        logger.error("Failed to start application: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }

    private static void handleGuiInitializationError(Exception e) {
    	logger.error("Failed to initialize GUI: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }

    private static void createDatabaseIfNotExists(String jdbcUrl, String username, String password, String dbName)
            throws Exception {
        String defaultUrl = jdbcUrl.replace("/" + dbName, "/postgres");
        try (Connection conn = DriverManager.getConnection(defaultUrl, username, password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'")) {

            if (!rs.next()) {
                stmt.executeUpdate("CREATE DATABASE " + dbName);
                logger.info("Database '" + dbName + "' created.");
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

    private record MongoSetup(ToDoRepository todoRepository, TransactionManager transactionManager) {}
    private record SqlSetup(ToDoRepository todoRepository, TransactionManager transactionManager) {}
}