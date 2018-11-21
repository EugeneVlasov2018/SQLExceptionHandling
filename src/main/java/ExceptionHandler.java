import java.sql.*;

public class ExceptionHandler {

    private final static String createTable = "CREATE TABLE users (id SERIAL, firstname VARCHAR(225), " +
            "secondname VARCHAR(225), password VARCHAR(225), PRIMARY KEY(id))";
    private final static String insertValuesIntoTable = "INSERT INTO users (firstname,secondname,password) " +
            "VALUES ('Ivan', 'Pupkin', '123')";
    private final static String getValuesfromWrongTable = "SELECT * FROM wrongDB";
    private final static String getWrongValFromTrueTable = "SELECT * FROM users WHERE wrongParameter = '123'";
    private final static String getWrongValFromWrongTable = "SELECT * FROM wrongTable WHERE wrongParameter = '123'";
    private final static String nowAllRight = "SELECT * FROM users";
    private final static String dropTable = "DROP TABLE users";
    private static Connection connectionToDB;


    public static void main(String[] args) {
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        //выскакивает NullPointerException, т.к. сначала передаем пустой коннекшен
        exceptionHandler.sendRequest(connectionToDB, nowAllRight);
        exceptionHandler.connectToDB();
        exceptionHandler.CreateTableAndInsertData(connectionToDB, createTable, insertValuesIntoTable);
        exceptionHandler.sendRequest(connectionToDB, getValuesfromWrongTable);
        exceptionHandler.sendRequest(connectionToDB, getWrongValFromTrueTable);
        //тут перехватиться только первфй код ошибки, - неправильное имя таблицы.
        // Все, что будет неправильно после нее будет игнорироваться
        exceptionHandler.sendRequest(connectionToDB, getWrongValFromWrongTable);
        exceptionHandler.sendRequest(connectionToDB, nowAllRight);
        exceptionHandler.sendRequest(connectionToDB,dropTable);
    }

    private void CreateTableAndInsertData(Connection connectionToDB, String createTable, String insertValues) {
        try (Statement statement = connectionToDB.createStatement()) {
            statement.executeUpdate(createTable);
            statement.executeUpdate(insertValues);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(Connection connectToDB, String sqlResponce) {
        try (Statement statement = connectToDB.createStatement();
             ResultSet resultSet = statement.executeQuery(sqlResponce)) {
            System.out.println("Результат:");
            while (resultSet.next()) {
                System.out.println("id - " + resultSet.getString(1));
                System.out.println("firstName - " + resultSet.getString(2));
                System.out.println("secondName - " + resultSet.getString(3));
                System.out.println("password - " + resultSet.getString(4));
            }

        } catch (SQLException a) {
            if(a.getSQLState().equals("42P01")) {
                System.err.println(a.getSQLState());
                System.err.println("Таблицы с таким именем не существует");
            }
            else if(a.getSQLState().equals("42703")) {
                System.err.println(a.getSQLState());
                System.err.println("Колонки с таким именем не существует");
            }
            else if(a.getSQLState().equals("02000")) {
                System.err.println(a.getSQLState());
                System.err.println("Таблица users удалена");
            }
            else System.err.println("Неизвестная ошибка. Код ошибки: "+a.getSQLState());
        } catch (NullPointerException b){
            System.err.println("Нет коннекшена к базе, - сначала приконнетитесь");
        }
    }

    private void connectToDB() {
        String url = "jdbc:postgresql://localhost:5432/test";
        String user = "postgres";
        String password = "root";
        String jdbcDriver = "org.postgresql.Driver";
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            System.err.println("Проверь, подтянулся ли джарник в либу или есть ли ссыль на него в депенденсах pom.xml " +
                    "Иначе никак(((");
        }
        try {
            connectionToDB = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
