import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CarRacingGame extends JFrame {

    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;

    // DATABASE

    private static final String DB_URL =
            "jdbc:mariadb://localhost:3306/carracing";

    private static final String DB_USER = "carracing";

    private static final String DB_PASSWORD = "CarRace123!";

    // PLAYER DATA


    private String currentUser = "";
    private int currentCoins = 0;
    private int selectedCarId = 1;

    // COLORS - DARK BLUE GAME UI

    private static final Color BG_DARK =
            new Color(12, 16, 21);

    private static final Color BG_PANEL =
            new Color(20, 25, 31);

    private static final Color BG_CARD =
            new Color(27, 33, 40);

    private static final Color BLUE =
            new Color(30, 144, 255);

    private static final Color BLUE_LIGHT =
            new Color(40, 170, 255);

    private static final Color YELLOW =
            new Color(255, 220, 0);

    private static final Color WHITE =
            Color.WHITE;

    private static final Color GRAY_TEXT =
            new Color(190, 195, 200);


    // CONSTRUCTOR

    public CarRacingGame() {

        setTitle("CAR RACING GAME");

        setSize(
                SCREEN_WIDTH,
                SCREEN_HEIGHT
        );

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        setResizable(false);

        initializeDatabase();

        showLoginMenu();
    }

    
    // DATABASE CONNECTION
    

    private Connection connectDB() throws SQLException {

        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD
        );
    }

    
    // DATABASE INITIALIZATION


    private void initializeDatabase() {

        try (
                Connection connection = connectDB();
                Statement st = connection.createStatement()
        ) {

            // Add coins column
            try {

                st.executeUpdate(
                        "ALTER TABLE users " +
                        "ADD COLUMN coins INT NOT NULL DEFAULT 0"
                );

            } catch (SQLException ignored) {
                // Already exists
            }

            // Add selected car column
            try {

                st.executeUpdate(
                        "ALTER TABLE users " +
                        "ADD COLUMN selected_car INT NOT NULL DEFAULT 1"
                );

            } catch (SQLException ignored) {
                // Already exists
            }

            
            // PLAY DATE
            

            try {

                st.executeUpdate(
                        "ALTER TABLE scores " +
                        "ADD COLUMN play_date DATE"
                );

            } catch (SQLException ignored) {
                // Already exists
            }

            
            // PLAY TIME
            

            try {

                st.executeUpdate(
                        "ALTER TABLE scores " +
                        "ADD COLUMN play_time VARCHAR(20)"
                );

            } catch (SQLException ignored) {
                // Already exists
            }

            try {

                st.executeUpdate(
                        "ALTER TABLE scores " +
                        "MODIFY COLUMN play_time VARCHAR(20)"
                );

            } catch (SQLException ignored) {
                // Already correct
            }


            // GAME MODE

            try {

                st.executeUpdate(
                        "ALTER TABLE scores " +
                        "ADD COLUMN mode VARCHAR(20)"
                );

            } catch (SQLException ignored) {
                // Already exists
            }

        
            // CARS TABLE
            

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS cars (" +
                    "id INT PRIMARY KEY, " +
                    "car_name VARCHAR(50) NOT NULL, " +
                    "car_color VARCHAR(20) NOT NULL, " +
                    "price INT NOT NULL" +
                    ")"
            );

            
            // PLAYER CARS
        

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_cars (" +
                    "username VARCHAR(100) NOT NULL, " +
                    "car_id INT NOT NULL, " +
                    "PRIMARY KEY(username, car_id), " +
                    "FOREIGN KEY(car_id) REFERENCES cars(id) " +
                    "ON DELETE CASCADE" +
                    ")"
            );

            
            // DEFAULT CARS
        

            st.executeUpdate(
                    "INSERT IGNORE INTO cars " +
                    "(id, car_name, car_color, price) VALUES " +
                    "(1, 'Red Racer', 'RED', 0), " +
                    "(2, 'Blue Racer', 'BLUE', 100), " +
                    "(3, 'Green Racer', 'GREEN', 250), " +
                    "(4, 'Yellow Racer', 'YELLOW', 500)"
            );

        } catch (SQLException e) {

            System.err.println(
                    "Database initialization error: "
                            + e.getMessage()
            );
        }
    }


    // LOGIN MENU
    

    private void showLoginMenu() {

        JPanel panel = new JPanel(
                new GridBagLayout()
        );

        panel.setBackground(BG_DARK);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(10, 10, 10, 10);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        JLabel title =
                new JLabel(
                        "CAR RACING GAME",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        32
                )
        );

        title.setForeground(YELLOW);

        JLabel subtitle =
                new JLabel(
                        "2D Racing Championship",
                        SwingConstants.CENTER
                );

        subtitle.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );

        subtitle.setForeground(
                GRAY_TEXT
        );

        JButton loginBtn =
                createStyledButton("LOGIN");

        JButton signUpBtn =
                createStyledButton("SIGN UP");

        JButton exitBtn =
                createStyledButton("EXIT");

        loginBtn.addActionListener(
                e -> showAuthForm(false)
        );

        signUpBtn.addActionListener(
                e -> showAuthForm(true)
        );

        exitBtn.addActionListener(
                e -> System.exit(0)
        );

        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(title, gbc);

        gbc.gridy = 1;

        panel.add(subtitle, gbc);

        gbc.gridy = 2;

        panel.add(loginBtn, gbc);

        gbc.gridy = 3;

        panel.add(signUpBtn, gbc);

        gbc.gridy = 4;

        panel.add(exitBtn, gbc);

        setContentPane(panel);

        revalidate();

        repaint();
    }


    // LOGIN / SIGNUP FORM


    private void showAuthForm(boolean isSignUp) {

        JPanel panel =
                new JPanel(
                        new GridBagLayout()
                );

        panel.setBackground(BG_DARK);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(8, 8, 8, 8);

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        JLabel title =
                new JLabel(
                        isSignUp
                                ? "CREATE ACCOUNT"
                                : "LOGIN",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        25
                )
        );

        title.setForeground(YELLOW);

        JTextField userField =
                new JTextField(15);

        JPasswordField passField =
                new JPasswordField(15);

        JCheckBox showPassword =
                new JCheckBox("Show Password");

        showPassword.setBackground(
                BG_DARK
        );

        showPassword.setForeground(
                WHITE
        );

        showPassword.setFocusPainted(false);

        showPassword.addActionListener(e -> {

            if (showPassword.isSelected()) {

                passField.setEchoChar(
                        (char) 0
                );

                showPassword.setText(
                        "Hide Password"
                );

            } else {

                passField.setEchoChar(
                        '•'
                );

                showPassword.setText(
                        "Show Password"
                );
            }
        });

        JLabel msgLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        msgLabel.setForeground(
                Color.RED
        );

        JButton submitBtn =
                createStyledButton(
                        isSignUp
                                ? "REGISTER"
                                : "LOGIN"
                );

        JButton backBtn =
                createStyledButton("BACK");

        submitBtn.addActionListener(e -> {

            String username =
                    userField.getText().trim();

            String password =
                    new String(
                            passField.getPassword()
                    ).trim();

            if (
                    username.isEmpty()
                            || password.isEmpty()
            ) {

                msgLabel.setText(
                        "Fields cannot be empty!"
                );

                return;
            }

            if (isSignUp) {

                if (userExists(username)) {

                    msgLabel.setText(
                            "Username already exists!"
                    );

                } else if (
                        registerUser(
                                username,
                                password
                        )
                ) {

                    currentUser = username;

                    loadPlayerData();

                    JOptionPane.showMessageDialog(
                            this,
                            "Registration successful!"
                    );

                    showModeSelection();

                } else {

                    msgLabel.setText(
                            "Error saving user!"
                    );
                }

            } else {

                if (
                        validateUser(
                                username,
                                password
                        )
                ) {

                    currentUser = username;

                    loadPlayerData();

                    showModeSelection();

                } else {

                    msgLabel.setText(
                            "Invalid username or password!"
                    );
                }
            }
        });

        backBtn.addActionListener(
                e -> showLoginMenu()
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;

        JLabel userLabel =
                new JLabel("Username:");

        userLabel.setForeground(WHITE);

        panel.add(
                userLabel,
                gbc
        );

        gbc.gridx = 1;

        panel.add(
                userField,
                gbc
        );

        gbc.gridx = 0;
        gbc.gridy = 2;

        JLabel passLabel =
                new JLabel("Password:");

        passLabel.setForeground(WHITE);

        panel.add(
                passLabel,
                gbc
        );

        gbc.gridx = 1;

        panel.add(
                passField,
                gbc
        );

        gbc.gridx = 1;
        gbc.gridy = 3;

        panel.add(
                showPassword,
                gbc
        );

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;

        panel.add(
                msgLabel,
                gbc
        );

        gbc.gridy = 5;
        gbc.gridwidth = 1;

        gbc.gridx = 0;

        panel.add(
                submitBtn,
                gbc
        );

        gbc.gridx = 1;

        panel.add(
                backBtn,
                gbc
        );

        setContentPane(panel);

        revalidate();

        repaint();
    }

    
    // MAIN MODE SELECTION
    

    private void showModeSelection() {

        loadPlayerData();

        JPanel mainPanel =
                new JPanel(
                        new BorderLayout(
                                12,
                                12
                        )
                );

        mainPanel.setBackground(
                BG_DARK
        );

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );


        // LEFT SIDE - LEADERBOARD


        JPanel leaderboardPanel =
                new JPanel(
                        new BorderLayout()
                );

        leaderboardPanel.setBackground(
                BG_PANEL
        );

        leaderboardPanel.setBorder(
                new LineBorder(
                        BLUE,
                        2
                )
        );

        JLabel leaderboardTitle =
                new JLabel(
                        "TOP 10 LEADERBOARD",
                        SwingConstants.CENTER
                );

        leaderboardTitle.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        21
                )
        );

        leaderboardTitle.setForeground(
                YELLOW
        );

        leaderboardTitle.setOpaque(
                true
        );

        leaderboardTitle.setBackground(
                new Color(
                        15,
                        100,
                        180
                )
        );

        leaderboardTitle.setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        5,
                        12,
                        5
                )
        );

        leaderboardPanel.add(
                leaderboardTitle,
                BorderLayout.NORTH
        );

        // LEADERBOARD - UNCHANGED
        

        String[] columns = {
                "RANK",
                "PLAYER",
                "SCORE"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        String sql =
                "SELECT username, " +
                "MAX(score) AS high_score " +
                "FROM scores " +
                "GROUP BY username " +
                "ORDER BY high_score DESC, " +
                "username ASC " +
                "LIMIT 10";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            ResultSet rs =
                    statement.executeQuery();

            int rank = 1;

            while (rs.next()) {

                model.addRow(
                        new Object[]{
                                rank++,
                                rs.getString(
                                        "username"
                                ),
                                rs.getInt(
                                        "high_score"
                                )
                        }
                );
            }

        } catch (SQLException e) {

            model.addRow(
                    new Object[]{
                            "-",
                            "Database Error",
                            "-"
                    }
            );

            System.err.println(
                    "Leaderboard error: "
                            + e.getMessage()
            );
        }

        if (model.getRowCount() == 0) {

            model.addRow(
                    new Object[]{
                            "-",
                            "No scores yet",
                            "-"
                    }
            );
        }

        JTable table =
                new JTable(model);

        table.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        table.setRowHeight(32);

        table.setForeground(
                WHITE
        );

        table.setBackground(
                new Color(
                        25,
                        30,
                        36
                )
        );

        table.setGridColor(
                new Color(
                        55,
                        65,
                        75
                )
        );

        table.setSelectionBackground(
                new Color(
                        25,
                        110,
                        190
                )
        );

        table.setSelectionForeground(
                WHITE
        );

        table.setFillsViewportHeight(
                true
        );

        table.setShowGrid(true);

        table.getTableHeader()
                .setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                14
                        )
                );

        table.getTableHeader()
                .setForeground(
                        WHITE
                );

        table.getTableHeader()
                .setBackground(
                        new Color(
                                30,
                                40,
                                50
                        )
                );

        table.getTableHeader()
                .setPreferredSize(
                        new Dimension(
                                0,
                                35
                        )
                );

        table.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(55);

        table.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(125);

        table.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(80);

        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        table.getColumnModel()
                .getColumn(0)
                .setCellRenderer(
                        centerRenderer
                );

        table.getColumnModel()
                .getColumn(2)
                .setCellRenderer(
                        centerRenderer
                );

        JScrollPane scrollPane =
                new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollPane.getViewport()
                .setBackground(
                        new Color(
                                25,
                                30,
                                36
                        )
                );

        leaderboardPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // RIGHT SIDE - MENU
        
        JPanel menuPanel =
                new JPanel(
                        new GridBagLayout()
                );

        menuPanel.setBackground(
                BG_PANEL
        );

        menuPanel.setBorder(
                new LineBorder(
                        BLUE,
                        2
                )
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        6,
                        18,
                        6,
                        18
                );

        gbc.fill =
                GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1.0;

        JLabel welcome =
                new JLabel(
                        "Welcome " +
                                currentUser +
                                "!",
                        SwingConstants.CENTER
                );

        welcome.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        welcome.setForeground(
                BLUE_LIGHT
        );

        gbc.gridx = 0;
        gbc.gridy = 0;

        menuPanel.add(
                welcome,
                gbc
        );

        JLabel coinsLabel =
                new JLabel(
                        "Coins: " +
                                currentCoins,
                        SwingConstants.CENTER
                );

        coinsLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        19
                )
        );

        coinsLabel.setForeground(
                YELLOW
        );

        gbc.gridy++;

        menuPanel.add(
                coinsLabel,
                gbc
        );

        JSeparator separator =
                new JSeparator();

        separator.setForeground(
                BLUE
        );

        gbc.gridy++;

        menuPanel.add(
                separator,
                gbc
        );

        JButton easyBtn =
                createMenuButton(
                        "EASY MODE"
                );

        JButton mediumBtn =
                createMenuButton(
                        "MEDIUM MODE"
                );

        JButton hardBtn =
                createMenuButton(
                        "HARD MODE"
                );

        JButton shopBtn =
                createMenuButton(
                        "CAR SHOP"
                );

        JButton scoresBtn =
                createMenuButton(
                        "MY SCORES"
                );

        JButton logoutBtn =
                createMenuButton(
                        "LOGOUT"
                );

        easyBtn.addActionListener(
                e -> startGame(5, 1)
        );

        mediumBtn.addActionListener(
                e -> startGame(8, 2)
        );

        hardBtn.addActionListener(
                e -> startGame(12, 3)
        );

        shopBtn.addActionListener(
                e -> showCarShop()
        );

        scoresBtn.addActionListener(
                e -> showPreviousScores()
        );

        logoutBtn.addActionListener(e -> {

            currentUser = "";

            currentCoins = 0;

            selectedCarId = 1;

            showLoginMenu();
        });

        gbc.gridy++;
        menuPanel.add(
                easyBtn,
                gbc
        );

        gbc.gridy++;
        menuPanel.add(
                mediumBtn,
                gbc
        );

        gbc.gridy++;
        menuPanel.add(
                hardBtn,
                gbc
        );

        gbc.gridy++;
        menuPanel.add(
                shopBtn,
                gbc
        );

        gbc.gridy++;
        menuPanel.add(
                scoresBtn,
                gbc
        );

        gbc.gridy++;
        menuPanel.add(
                logoutBtn,
                gbc
        );

        mainPanel.add(
                leaderboardPanel,
                BorderLayout.WEST
        );

        mainPanel.add(
                menuPanel,
                BorderLayout.CENTER
        );

        setContentPane(
                mainPanel
        );

        revalidate();

        repaint();
    }

    // START GAME
    

    private void startGame(
            int gameSpeed,
            int scoreMultiplier
    ) {

        loadPlayerData();

        GamePanel gamePanel =
                new GamePanel(
                        gameSpeed,
                        scoreMultiplier
                );

        setContentPane(
                gamePanel
        );

        revalidate();

        repaint();

        gamePanel.requestFocusInWindow();

        gamePanel.startGameThread();
    }

    // PLAYER DATABASE
    

    private void loadPlayerData() {

        if (currentUser.isEmpty()) {
            return;
        }

        String sql =
                "SELECT coins, selected_car " +
                "FROM users " +
                "WHERE username = ?";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    currentUser
            );

            ResultSet result =
                    statement.executeQuery();

            if (result.next()) {

                currentCoins =
                        result.getInt(
                                "coins"
                        );

                selectedCarId =
                        result.getInt(
                                "selected_car"
                        );

                if (
                        selectedCarId < 1
                                || selectedCarId > 4
                ) {

                    selectedCarId = 1;
                }

                ensureStarterCar();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Player data error: "
                            + e.getMessage()
            );
        }
    }

    private void ensureStarterCar() {

        if (currentUser.isEmpty()) {
            return;
        }

        String sql =
                "INSERT IGNORE INTO player_cars " +
                "(username, car_id) VALUES (?, 1)";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    currentUser
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                    "Starter car error: "
                            + e.getMessage()
            );
        }
    }

    private void addCoinsToDatabase(
            int amount
    ) {

        if (
                currentUser.isEmpty()
                        || amount <= 0
        ) {
            return;
        }

        String sql =
                "UPDATE users " +
                "SET coins = coins + ? " +
                "WHERE username = ?";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setInt(
                    1,
                    amount
            );

            statement.setString(
                    2,
                    currentUser
            );

            statement.executeUpdate();

            currentCoins += amount;

        } catch (SQLException e) {

            System.err.println(
                    "Coin save error: "
                            + e.getMessage()
            );
        }
    }

    // CAR SHOP
    

    private void showCarShop() {

        loadPlayerData();

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        panel.setBackground(
                BG_DARK
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        JLabel title =
                new JLabel(
                        "CAR SHOP   |   COINS: "
                                + currentCoins,
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );

        title.setForeground(
                YELLOW
        );

        title.setOpaque(true);

        title.setBackground(
                new Color(
                        20,
                        100,
                        180
                )
        );

        title.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        5,
                        10,
                        5
                )
        );

        JPanel carsPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                2,
                                15,
                                15
                        )
                );

        carsPanel.setBackground(
                BG_DARK
        );

        carsPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT id, car_name, " +
                                "car_color, price " +
                                "FROM cars ORDER BY id"
                        )
        ) {

            ResultSet rs =
                    statement.executeQuery();

            while (rs.next()) {

                int carId =
                        rs.getInt("id");

                String carName =
                        rs.getString(
                                "car_name"
                        );

                String colorName =
                        rs.getString(
                                "car_color"
                        );

                int price =
                        rs.getInt("price");

                JPanel card =
                        new JPanel(
                                new BorderLayout(
                                        5,
                                        5
                                )
                        );

                card.setBackground(
                        BG_CARD
                );

                card.setBorder(
                        new LineBorder(
                                BLUE,
                                1
                        )
                );

                CarPreview preview =
                        new CarPreview(
                                carId,
                                colorName
                        );

                card.add(
                        preview,
                        BorderLayout.CENTER
                );

                JLabel info =
                        new JLabel(
                                "<html><center>"
                                        + carName
                                        + "<br>Color: "
                                        + colorName
                                        + "<br>Price: "
                                        + price
                                        + " coins"
                                        + "</center></html>",
                                SwingConstants.CENTER
                        );

                info.setForeground(
                        WHITE
                );

                JButton actionBtn =
                        createStyledButton("");

                boolean owned =
                        isCarOwned(carId);

                if (
                        owned
                                && selectedCarId == carId
                ) {

                    actionBtn.setText(
                            "SELECTED"
                    );

                    actionBtn.setEnabled(
                            false
                    );

                } else if (owned) {

                    actionBtn.setText(
                            "SELECT"
                    );

                    actionBtn.addActionListener(
                            e -> {

                                selectCar(carId);

                                showCarShop();
                            }
                    );

                } else {

                    actionBtn.setText(
                            "BUY ("
                                    + price
                                    + ")"
                    );

                    actionBtn.addActionListener(
                            e -> buyCar(
                                    carId,
                                    price
                            )
                    );
                }

                card.add(
                        info,
                        BorderLayout.NORTH
                );

                card.add(
                        actionBtn,
                        BorderLayout.SOUTH
                );

                carsPanel.add(card);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load car shop:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        JButton backBtn =
                createStyledButton(
                        "BACK"
                );

        backBtn.addActionListener(
                e -> showModeSelection()
        );

        panel.add(
                title,
                BorderLayout.NORTH
        );

        panel.add(
                carsPanel,
                BorderLayout.CENTER
        );

        panel.add(
                backBtn,
                BorderLayout.SOUTH
        );

        setContentPane(panel);

        revalidate();

        repaint();
    }

    private boolean isCarOwned(
            int carId
    ) {

        String sql =
                "SELECT 1 FROM player_cars " +
                "WHERE username = ? " +
                "AND car_id = ?";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    currentUser
            );

            statement.setInt(
                    2,
                    carId
            );

            ResultSet rs =
                    statement.executeQuery();

            return rs.next();

        } catch (SQLException e) {

            System.err.println(
                    "Car ownership error: "
                            + e.getMessage()
            );

            return false;
        }
    }

    private void buyCar(
            int carId,
            int price
    ) {

        if (isCarOwned(carId)) {

            JOptionPane.showMessageDialog(
                    this,
                    "You already own this car!"
            );

            showCarShop();

            return;
        }

        loadPlayerData();

        if (currentCoins < price) {

            JOptionPane.showMessageDialog(
                    this,
                    "Not enough coins!\n"
                            + "Required: "
                            + price
                            + "\nYour coins: "
                            + currentCoins
            );

            return;
        }

        String updateCoins =
                "UPDATE users " +
                "SET coins = coins - ? " +
                "WHERE username = ?";

        String insertCar =
                "INSERT INTO player_cars " +
                "(username, car_id) " +
                "VALUES (?, ?)";

        try (
                Connection connection =
                        connectDB()
        ) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement coinStmt =
                            connection.prepareStatement(
                                    updateCoins
                            );

                    PreparedStatement carStmt =
                            connection.prepareStatement(
                                    insertCar
                            )
            ) {

                coinStmt.setInt(
                        1,
                        price
                );

                coinStmt.setString(
                        2,
                        currentUser
                );

                coinStmt.executeUpdate();

                carStmt.setString(
                        1,
                        currentUser
                );

                carStmt.setInt(
                        2,
                        carId
                );

                carStmt.executeUpdate();

                connection.commit();

                currentCoins -= price;

                JOptionPane.showMessageDialog(
                        this,
                        "Car purchased successfully!"
                );

                showCarShop();

            } catch (SQLException e) {

                connection.rollback();

                throw e;
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Purchase failed:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void selectCar(
            int carId
    ) {

        if (!isCarOwned(carId)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Buy the car first!"
            );

            return;
        }

        String sql =
                "UPDATE users " +
                "SET selected_car = ? " +
                "WHERE username = ?";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setInt(
                    1,
                    carId
            );

            statement.setString(
                    2,
                    currentUser
            );

            statement.executeUpdate();

            selectedCarId = carId;

            JOptionPane.showMessageDialog(
                    this,
                    "Car selected!"
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not select car:\n"
                            + e.getMessage()
            );
        }
    }

    private String getCarColor(
            int carId
    ) {

        String sql =
                "SELECT car_color " +
                "FROM cars WHERE id = ?";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setInt(
                    1,
                    carId
            );

            ResultSet rs =
                    statement.executeQuery();

            if (rs.next()) {

                return rs.getString(
                        "car_color"
                );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Car color error: "
                            + e.getMessage()
            );
        }

        return "RED";
    }

    // MY SCORES
    

    private void showPreviousScores() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        panel.setBackground(
                BG_DARK
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        JLabel title =
                new JLabel(
                        "MY SCORES - "
                                + currentUser,
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );

        title.setForeground(
                YELLOW
        );

        title.setOpaque(true);

        title.setBackground(
                new Color(
                        20,
                        100,
                        180
                )
        );

        DefaultListModel<String>
                listModel =
                new DefaultListModel<>();

        // DATE + TIME + MODE
        

        String sql =
                "SELECT score, speed, " +
                "play_date, play_time, mode " +
                "FROM scores " +
                "WHERE username = ? " +
                "ORDER BY play_date DESC, " +
                "play_time DESC";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    currentUser
            );

            ResultSet result =
                    statement.executeQuery();

            while (result.next()) {

                int score =
                        result.getInt(
                                "score"
                        );

                String playDate =
                        result.getString(
                                "play_date"
                        );

                String playTime =
                        result.getString(
                                "play_time"
                        );

                String mode =
                        result.getString(
                                "mode"
                        );

                /*
                 * For old records, mode/date/time can be NULL.
                 * This prevents displaying "null".
                 */
                if (playDate == null) {
                    playDate = "N/A";
                }

                if (playTime == null) {
                    playTime = "N/A";
                }

                if (mode == null) {

                    int speed =
                            result.getInt(
                                    "speed"
                            );

                    if (speed == 5) {
                        mode = "Easy";
                    } else if (speed == 8) {
                        mode = "Medium";
                    } else {
                        mode = "Hard";
                    }
                }

                listModel.addElement(
                        "PLAYER: "
                                + currentUser
                                + "   |   DATE: "
                                + playDate
                                + "   |   TIME: "
                                + playTime
                                + "   |   SCORE: "
                                + score
                                + "   |   MODE: "
                                + mode
                );
            }

            if (listModel.isEmpty()) {

                listModel.addElement(
                        "No score history found."
                );
            }

        } catch (SQLException e) {

            listModel.addElement(
                    "Database connection error!"
            );

            System.err.println(
                    "Previous scores error: "
                            + e.getMessage()
            );
        }

        JList<String> scoreList =
                new JList<>(
                        listModel
                );

        scoreList.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        15
                )
        );

        scoreList.setBackground(
                BG_PANEL
        );

        scoreList.setForeground(
                WHITE
        );

        JButton backBtn =
                createStyledButton(
                        "BACK"
                );

        backBtn.addActionListener(
                e -> showModeSelection()
        );

        panel.add(
                title,
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(
                        scoreList
                ),
                BorderLayout.CENTER
        );

        panel.add(
                backBtn,
                BorderLayout.SOUTH
        );

        setContentPane(panel);

        revalidate();

        repaint();
    }

    // REGISTER
    

    private boolean registerUser(
            String username,
            String password
    ) {

        String sql =
                "INSERT INTO users " +
                "(username, password) " +
                "VALUES (?, ?)";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    username
            );

            statement.setString(
                    2,
                    password
            );

            boolean success =
                    statement.executeUpdate() > 0;

            if (success) {

                try (
                        PreparedStatement carStmt =
                                connection.prepareStatement(
                                        "INSERT IGNORE INTO " +
                                        "player_cars " +
                                        "(username, car_id) " +
                                        "VALUES (?, 1)"
                                )
                ) {

                    carStmt.setString(
                            1,
                            username
                    );

                    carStmt.executeUpdate();
                }
            }

            return success;

        } catch (SQLException e) {

            System.err.println(
                    "Registration error: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // LOGIN VALIDATION
    

    private boolean validateUser(
            String username,
            String password
    ) {

        String sql =
                "SELECT id FROM users " +
                "WHERE username = ? " +
                "AND password = ?";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    username
            );

            statement.setString(
                    2,
                    password
            );

            ResultSet result =
                    statement.executeQuery();

            return result.next();

        } catch (SQLException e) {

            System.err.println(
                    "Login error: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // USER EXISTS
    

    private boolean userExists(
            String username
    ) {

        String sql =
                "SELECT id FROM users " +
                "WHERE username = ?";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    username
            );

            ResultSet result =
                    statement.executeQuery();

            return result.next();

        } catch (SQLException e) {

            System.err.println(
                    "User check error: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // SAVE SCORE
    

    private void saveScore(
            int score,
            int speed
    ) {

        if (currentUser.isEmpty()) {
            return;
        }

       
        // DETERMINE MODE
        

        String mode;

        if (speed == 5) {

            mode = "Easy";

        } else if (speed == 8) {

            mode = "Medium";

        } else {

            mode = "Hard";
        }

        // NEPAL DATE AND TIME
        

        ZoneId nepalZone =
                ZoneId.of("Asia/Kathmandu");

        ZonedDateTime nepalTime =
                ZonedDateTime.now(
                        nepalZone
                );

        String playDate =
                nepalTime.format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd"
                        )
                );

        String playTime =
                nepalTime.format(
                        DateTimeFormatter.ofPattern(
                                "hh:mm:ss a"
                        )
                );

        // SAVE SCORE + NEPAL DATE + NEPAL TIME + MODE
        

        String sql =
                "INSERT INTO scores " +
                "(username, score, speed, " +
                "play_date, play_time, mode) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection connection =
                        connectDB();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    currentUser
            );

            statement.setInt(
                    2,
                    score
            );

            statement.setInt(
                    3,
                    speed
            );

            statement.setString(
                    4,
                    playDate
            );

            statement.setString(
                    5,
                    playTime
            );

            statement.setString(
                    6,
                    mode
            );

            statement.executeUpdate();

            System.out.println(
                    "Score saved!"
            );

            System.out.println(
                    "Nepal Date: "
                            + playDate
            );

            System.out.println(
                    "Nepal Time: "
                            + playTime
            );

            System.out.println(
                    "Mode: "
                            + mode
            );

        } catch (SQLException e) {

            System.err.println(
                    "Score save error: "
                            + e.getMessage()
            );
        }
    }

    // GAME PANEL

    private class GamePanel
            extends JPanel
            implements Runnable {

        private static final int ROAD_WIDTH = 300;

        private static final int ROAD_X =
                (800 - ROAD_WIDTH) / 2;

        private Thread gameThread;

        private boolean isRunning = false;

        private boolean isGameOver = false;

        private boolean scoreSaved = false;

        private final int speed;

        private final int scoreMultiplier;

        private int score = 0;

        private int gameCoins = 0;

        private final PlayerCar playerCar;

        private final List<EnemyCar>
                enemies =
                new ArrayList<>();

        private final List<Coin>
                coins =
                new ArrayList<>();

        private final Random random =
                new Random();

        private JButton restartButton;

        private JButton menuButton;

        // GAME CONSTRUCTOR
        

        public GamePanel(
                int speed,
                int scoreMultiplier
        ) {

            this.speed = speed;

            this.scoreMultiplier =
                    scoreMultiplier;

            String carColor =
                    getCarColor(
                            selectedCarId
                    );

            playerCar =
                    new PlayerCar(
                            ROAD_X
                                    + (ROAD_WIDTH / 2)
                                    - 20,
                            480,
                            carColor
                    );

            setBackground(
                    new Color(
                            34,
                            139,
                            34
                    )
            );

            for (int i = 0; i < 3; i++) {

                int spawnX =
                        ROAD_X
                                + random.nextInt(
                                ROAD_WIDTH - 40
                        );

                enemies.add(
                        new EnemyCar(
                                spawnX,
                                -180 * (i + 1)
                        )
                );
            }

            for (int i = 0; i < 3; i++) {

                spawnCoin(
                        -120 - (i * 220)
                );
            }

            setLayout(null);

            setupControls();

            setupGameOverButtons();
        }

        // GAME OVER BUTTONS
        

        private void setupGameOverButtons() {

            restartButton =
                    new JButton(
                            "RESTART"
                    );

            restartButton.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            18
                    )
            );

            restartButton.setForeground(
                    WHITE
            );

            restartButton.setBackground(
                    BLUE
            );

            restartButton.setFocusPainted(
                    false
            );

            restartButton.setBorder(
                    new LineBorder(
                            BLUE_LIGHT,
                            2
                    )
            );

            restartButton.setBounds(
                    SCREEN_WIDTH / 2 - 100,
                    SCREEN_HEIGHT / 2 + 110,
                    200,
                    45
            );

            restartButton.setVisible(
                    false
            );

            restartButton.addActionListener(
                    e -> restartGame()
            );

            add(
                    restartButton
            );

            menuButton =
                    new JButton(
                            "MAIN MENU"
                    );

            menuButton.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            18
                    )
            );

            menuButton.setForeground(
                    WHITE
            );

            menuButton.setBackground(
                    new Color(
                            55,
                            65,
                            75
                    )
            );

            menuButton.setFocusPainted(
                    false
            );

            menuButton.setBorder(
                    new LineBorder(
                            new Color(
                                    100,
                                    110,
                                    120
                            ),
                            2
                    )
            );

            menuButton.setBounds(
                    SCREEN_WIDTH / 2 - 100,
                    SCREEN_HEIGHT / 2 + 165,
                    200,
                    45
            );

            menuButton.setVisible(
                    false
            );

            menuButton.addActionListener(
                    e -> {

                        stopGame();

                        showModeSelection();
                    }
            );

            add(
                    menuButton
            );
        }

        // CONTROLS
        

        private void setupControls() {

            InputMap inputMap =
                    getInputMap(
                            JComponent
                                    .WHEN_IN_FOCUSED_WINDOW
                    );

            ActionMap actionMap =
                    getActionMap();

            inputMap.put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_LEFT,
                            0,
                            false
                    ),
                    "left_pressed"
            );

            inputMap.put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_LEFT,
                            0,
                            true
                    ),
                    "left_released"
            );

            inputMap.put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_RIGHT,
                            0,
                            false
                    ),
                    "right_pressed"
            );

            inputMap.put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_RIGHT,
                            0,
                            true
                    ),
                    "right_released"
            );

            inputMap.put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_ESCAPE,
                            0
                    ),
                    "esc"
            );

            actionMap.put(
                    "left_pressed",
                    new AbstractAction() {

                        @Override
                        public void actionPerformed(
                                ActionEvent e
                        ) {

                            playerCar
                                    .setMovingLeft(
                                            true
                                    );
                        }
                    }
            );

            actionMap.put(
                    "left_released",
                    new AbstractAction() {

                        @Override
                        public void actionPerformed(
                                ActionEvent e
                        ) {

                            playerCar
                                    .setMovingLeft(
                                            false
                                    );
                        }
                    }
            );

            actionMap.put(
                    "right_pressed",
                    new AbstractAction() {

                        @Override
                        public void actionPerformed(
                                ActionEvent e
                        ) {

                            playerCar
                                    .setMovingRight(
                                            true
                                    );
                        }
                    }
            );

            actionMap.put(
                    "right_released",
                    new AbstractAction() {

                        @Override
                        public void actionPerformed(
                                ActionEvent e
                        ) {

                            playerCar
                                    .setMovingRight(
                                            false
                                    );
                        }
                    }
            );

            actionMap.put(
                    "esc",
                    new AbstractAction() {

                        @Override
                        public void actionPerformed(
                                ActionEvent e
                        ) {

                            if (isGameOver) {

                                stopGame();

                                showModeSelection();
                            }
                        }
                    }
            );
        }

        // START THREAD
        

        public void startGameThread() {

            if (isRunning) {
                return;
            }

            isRunning = true;

            gameThread =
                    new Thread(this);

            gameThread.start();
        }

        
        // STOP

        public void stopGame() {

            isRunning = false;
        }
        // RESTART
        

        private void restartGame() {

            score = 0;

            gameCoins = 0;

            isGameOver = false;

            scoreSaved = false;

            playerCar.setX(
                    ROAD_X
                            + (ROAD_WIDTH / 2)
                            - 20
            );

            playerCar.setY(480);

            playerCar.setMovingLeft(
                    false
            );

            playerCar.setMovingRight(
                    false
            );

            for (
                    int i = 0;
                    i < enemies.size();
                    i++
            ) {

                EnemyCar enemy =
                        enemies.get(i);

                enemy.setX(
                        ROAD_X
                                + random.nextInt(
                                ROAD_WIDTH
                                        - enemy.getWidth()
                        )
                );

                enemy.setY(
                        -200 - (i * 220)
                );
            }

            for (
                    int i = 0;
                    i < coins.size();
                    i++
            ) {

                Coin coin =
                        coins.get(i);

                coin.setX(
                        ROAD_X
                                + 20
                                + random.nextInt(
                                ROAD_WIDTH - 60
                        )
                );

                coin.setY(
                        -120 - (i * 220)
                );

                coin.setCollected(
                        false
                );
            }

            restartButton.setVisible(
                    false
            );

            menuButton.setVisible(
                    false
            );

            requestFocusInWindow();

            startGameThread();

            repaint();
        }

        // GAME LOOP
     

        @Override
        public void run() {

            double fpsLimit = 60.0;

            double drawInterval =
                    1000000000 / fpsLimit;

            double delta = 0;

            long lastTime =
                    System.nanoTime();

            while (isRunning) {

                long currentTime =
                        System.nanoTime();

                delta +=
                        (currentTime - lastTime)
                                / drawInterval;

                lastTime =
                        currentTime;

                if (delta >= 1) {

                    updateGame();

                    repaint();

                    delta--;
                }
            }
        }

        // UPDATE GAME
        

        private void updateGame() {

            if (isGameOver) {
                return;
            }

            playerCar.update(
                    ROAD_X,
                    ROAD_WIDTH
            );

            for (
                    EnemyCar enemy :
                    enemies
            ) {

                enemy.update(
                        speed
                );

                if (
                        enemy.getY()
                                > SCREEN_HEIGHT
                ) {

                    enemy.setY(
                            -200
                    );

                    int newX;

                    boolean validPosition;

                    int attempts = 0;

                    do {

                        validPosition =
                                true;

                        newX =
                                ROAD_X
                                        + random.nextInt(
                                        ROAD_WIDTH
                                                - enemy.getWidth()
                                );

                        for (
                                EnemyCar other :
                                enemies
                        ) {

                            if (
                                    other != enemy
                                            &&
                                            Math.abs(
                                                    other.getY()
                                                            - enemy.getY()
                                            ) < 150
                            ) {

                                if (
                                        Math.abs(
                                                other.getX()
                                                        - newX
                                        ) < 50
                                ) {

                                    validPosition =
                                            false;

                                    break;
                                }
                            }
                        }

                        attempts++;

                    } while (
                            !validPosition
                                    && attempts < 10
                    );

                    enemy.setX(
                            newX
                    );

                    score +=
                            scoreMultiplier;
                }

                if (
                        playerCar
                                .getBounds()
                                .intersects(
                                        enemy.getBounds()
                                )
                ) {

                    endGame();

                    return;
                }
            }

            for (
                    Coin coin :
                    coins
            ) {

                coin.update(
                        speed
                );

                if (
                        coin.getY()
                                > SCREEN_HEIGHT
                ) {

                    respawnCoin(
                            coin
                    );
                }

                if (
                        playerCar
                                .getBounds()
                                .intersects(
                                        coin.getBounds()
                                )
                ) {

                    coin.setCollected(
                            true
                    );

                    gameCoins++;

                    addCoinsToDatabase(
                            1
                    );

                    respawnCoin(
                            coin
                    );
                }
            }
        }

        // GAME OVER
        

        private void endGame() {

            isGameOver = true;

            if (!scoreSaved) {

                saveScore(
                        score,
                        speed
                );

                scoreSaved = true;
            }

            SwingUtilities.invokeLater(
                    () -> {

                        restartButton
                                .setVisible(
                                        true
                                );

                        menuButton
                                .setVisible(
                                        true
                                );

                        requestFocusInWindow();

                        repaint();
                    }
            );
        }

        // SPAWN COIN

        private void spawnCoin(
                int y
        ) {

            int x =
                    ROAD_X
                            + 20
                            + random.nextInt(
                            ROAD_WIDTH - 60
                    );

            coins.add(
                    new Coin(
                            x,
                            y
                    )
            );
        }

        // RESPAWN COIN
        private void respawnCoin(
                Coin coin
        ) {

            coin.setX(
                    ROAD_X
                            + 20
                            + random.nextInt(
                            ROAD_WIDTH - 60
                    )
            );

            coin.setY(
                    -100
                            - random.nextInt(
                            400
                    )
            );

            coin.setCollected(
                    false
            );
        }

        // DRAW
        
        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            Graphics2D g2d =
                    (Graphics2D) g;

            g2d.setColor(
                    Color.DARK_GRAY
            );

            g2d.fillRect(
                    ROAD_X,
                    0,
                    ROAD_WIDTH,
                    SCREEN_HEIGHT
            );

            g2d.setColor(
                    Color.WHITE
            );

            g2d.fillRect(
                    ROAD_X,
                    0,
                    4,
                    SCREEN_HEIGHT
            );

            g2d.fillRect(
                    ROAD_X
                            + ROAD_WIDTH
                            - 4,
                    0,
                    4,
                    SCREEN_HEIGHT
            );

            for (
                    int y = 0;
                    y < SCREEN_HEIGHT;
                    y += 40
            ) {

                g2d.fillRect(
                        ROAD_X
                                + ROAD_WIDTH / 2
                                - 2,
                        y,
                        4,
                        20
                );
            }

            for (
                    Coin coin :
                    coins
            ) {

                coin.draw(g2d);
            }

            playerCar.draw(g2d);

            for (
                    EnemyCar enemy :
                    enemies
            ) {

                enemy.draw(g2d);
            }

            // HUD
            

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            20
                    )
            );

            g2d.setColor(
                    WHITE
            );

            g2d.drawString(
                    "Score: " + score,
                    20,
                    30
            );

            g2d.setColor(
                    YELLOW
            );

            g2d.drawString(
                    "Coins: " + currentCoins,
                    20,
                    60
            );

            g2d.setColor(
                    WHITE
            );

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.PLAIN,
                            15
                    )
            );

            g2d.drawString(
                    "Player: "
                            + currentUser,
                    20,
                    88
            );

            // GAME OVER SCREEN
           

            if (isGameOver) {

                g2d.setColor(
                        new Color(
                                0,
                                0,
                                0,
                                190
                        )
                );

                g2d.fillRect(
                        0,
                        0,
                        SCREEN_WIDTH,
                        SCREEN_HEIGHT
                );

                String gameOverText =
                        "GAME OVER!";

                g2d.setColor(
                        Color.RED
                );

                g2d.setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                38
                        )
                );

                FontMetrics fm =
                        g2d.getFontMetrics();

                int gameOverX =
                        (
                                SCREEN_WIDTH
                                        - fm.stringWidth(
                                        gameOverText
                                )
                        ) / 2;

                g2d.drawString(
                        gameOverText,
                        gameOverX,
                        SCREEN_HEIGHT / 2 - 70
                );

                String scoreText =
                        "Score: " + score;

                g2d.setColor(
                        WHITE
                );

                g2d.setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                22
                        )
                );

                fm =
                        g2d.getFontMetrics();

                g2d.drawString(
                        scoreText,
                        (
                                SCREEN_WIDTH
                                        - fm.stringWidth(
                                        scoreText
                                )
                        ) / 2,
                        SCREEN_HEIGHT / 2 - 20
                );

                String coinText =
                        "Coins Collected: "
                                + gameCoins;

                g2d.setColor(
                        YELLOW
                );

                g2d.setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                20
                        )
                );

                fm =
                        g2d.getFontMetrics();

                g2d.drawString(
                        coinText,
                        (
                                SCREEN_WIDTH
                                        - fm.stringWidth(
                                        coinText
                                )
                        ) / 2,
                        SCREEN_HEIGHT / 2 + 20
                );
            }
        }
    }

    // GAME OBJECT
   

    private static abstract class GameObject {

        protected int x;
        protected int y;
        protected int width;
        protected int height;

        public GameObject(
                int x,
                int y,
                int width,
                int height
        ) {

            this.x = x;

            this.y = y;

            this.width = width;

            this.height = height;
        }

        public Rectangle getBounds() {

            return new Rectangle(
                    x,
                    y,
                    width,
                    height
            );
        }

        public abstract void draw(
                Graphics2D g2d
        );

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }
    }

  
    // PLAYER CAR
    

    private static class PlayerCar
            extends GameObject {

        private boolean movingLeft =
                false;

        private boolean movingRight =
                false;

        private final String colorName;

        public PlayerCar(
                int x,
                int y,
                String colorName
        ) {

            super(
                    x,
                    y,
                    40,
                    60
            );

            this.colorName =
                    colorName;
        }

        public void setMovingLeft(
                boolean value
        ) {

            movingLeft = value;
        }

        public void setMovingRight(
                boolean value
        ) {

            movingRight = value;
        }

        public void update(
                int roadX,
                int roadWidth
        ) {

            if (movingLeft) {
                x -= 6;
            }

            if (movingRight) {
                x += 6;
            }

            if (x < roadX) {
                x = roadX;
            }

            if (
                    x + width
                            > roadX + roadWidth
            ) {

                x =
                        roadX
                                + roadWidth
                                - width;
            }
        }

        private Color getMainColor() {

            if (
                    "BLUE".equalsIgnoreCase(
                            colorName
                    )
            ) {

                return Color.BLUE;
            }

            if (
                    "GREEN".equalsIgnoreCase(
                            colorName
                    )
            ) {

                return Color.GREEN;
            }

            if (
                    "YELLOW".equalsIgnoreCase(
                            colorName
                    )
            ) {

                return Color.YELLOW;
            }

            return Color.RED;
        }

        private Color getDarkColor() {

            if (
                    "BLUE".equalsIgnoreCase(
                            colorName
                    )
            ) {

                return new Color(
                        0,
                        0,
                        139
                );
            }

            if (
                    "GREEN".equalsIgnoreCase(
                            colorName
                    )
            ) {

                return new Color(
                        0,
                        100,
                        0
                );
            }

            if (
                    "YELLOW".equalsIgnoreCase(
                            colorName
                    )
            ) {

                return new Color(
                        180,
                        150,
                        0
                );
            }

            return new Color(
                    139,
                    0,
                    0
            );
        }

        @Override
        public void draw(
                Graphics2D g2d
        ) {

            g2d.setColor(
                    getMainColor()
            );

            g2d.fillRoundRect(
                    x,
                    y,
                    width,
                    height,
                    10,
                    10
            );

            g2d.setColor(
                    getDarkColor()
            );

            g2d.fillRoundRect(
                    x + 8,
                    y + 8,
                    24,
                    25,
                    5,
                    5
            );

            g2d.setColor(
                    Color.CYAN
            );

            g2d.fillPolygon(
                    new int[]{
                            x + 8,
                            x + 32,
                            x + 20
                    },
                    new int[]{
                            y + 8,
                            y + 8,
                            y + 33
                    },
                    3
            );

            g2d.setColor(
                    Color.YELLOW
            );

            g2d.fillOval(
                    x + 8,
                    y + 52,
                    6,
                    6
            );

            g2d.fillOval(
                    x + 26,
                    y + 52,
                    6,
                    6
            );
        }
    }

    // ENEMY CAR
    

    private static class EnemyCar
            extends GameObject {

        public EnemyCar(
                int x,
                int y
        ) {

            super(
                    x,
                    y,
                    40,
                    60
            );
        }

        public void update(
                int speed
        ) {

            y += speed;
        }

        @Override
        public void draw(
                Graphics2D g2d
        ) {

            g2d.setColor(
                    Color.BLUE
            );

            g2d.fillRoundRect(
                    x,
                    y,
                    width,
                    height,
                    10,
                    10
            );

            g2d.setColor(
                    new Color(
                            0,
                            0,
                            139
                    )
            );

            g2d.fillRoundRect(
                    x + 8,
                    y + 8,
                    24,
                    25,
                    5,
                    5
            );

            g2d.setColor(
                    new Color(
                            173,
                            216,
                            230
                    )
            );

            g2d.fillPolygon(
                    new int[]{
                            x + 8,
                            x + 32,
                            x + 20
                    },
                    new int[]{
                            y + 8,
                            y + 8,
                            y + 33
                    },
                    3
            );

            g2d.setColor(
                    Color.YELLOW
            );

            g2d.fillOval(
                    x + 8,
                    y + 52,
                    6,
                    6
            );

            g2d.fillOval(
                    x + 26,
                    y + 52,
                    6,
                    6
            );
        }
    }

    // COIN
  

    private static class Coin
            extends GameObject {

        private boolean collected =
                false;

        public Coin(
                int x,
                int y
        ) {

            super(
                    x,
                    y,
                    22,
                    22
            );
        }

        public void update(
                int speed
        ) {

            y += speed;
        }

        public void setCollected(
                boolean value
        ) {

            collected = value;
        }

        @Override
        public void draw(
                Graphics2D g2d
        ) {

            if (collected) {
                return;
            }

            g2d.setColor(
                    Color.YELLOW
            );

            g2d.fillOval(
                    x,
                    y,
                    width,
                    height
            );

            g2d.setColor(
                    new Color(
                            180,
                            140,
                            0
                    )
            );

            g2d.drawOval(
                    x,
                    y,
                    width,
                    height
            );

            g2d.setColor(
                    Color.BLACK
            );

            g2d.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            13
                    )
            );

            g2d.drawString(
                    "$",
                    x + 7,
                    y + 16
            );
        }
    }

    
    // CAR PREVIEW
    
    private static class CarPreview
            extends JPanel {

        private final int carId;

        private final String colorName;

        public CarPreview(
                int carId,
                String colorName
        ) {

            this.carId =
                    carId;

            this.colorName =
                    colorName;

            setPreferredSize(
                    new Dimension(
                            150,
                            120
                    )
            );

            setBackground(
                    new Color(
                            35,
                            40,
                            46
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            Graphics2D g2d =
                    (Graphics2D) g;

            int x =
                    getWidth() / 2 - 20;

            int y = 25;

            Color main;

            if (
                    "BLUE".equalsIgnoreCase(
                            colorName
                    )
            ) {

                main = Color.BLUE;

            } else if (
                    "GREEN".equalsIgnoreCase(
                            colorName
                    )
            ) {

                main = Color.GREEN;

            } else if (
                    "YELLOW".equalsIgnoreCase(
                            colorName
                    )
            ) {

                main = Color.YELLOW;

            } else {

                main = Color.RED;
            }

            g2d.setColor(main);

            g2d.fillRoundRect(
                    x,
                    y,
                    40,
                    60,
                    10,
                    10
            );

            g2d.setColor(
                    Color.CYAN
            );

            g2d.fillRoundRect(
                    x + 8,
                    y + 8,
                    24,
                    25,
                    5,
                    5
            );

            g2d.setColor(
                    Color.WHITE
            );

            g2d.drawString(
                    "CAR " + carId,
                    x - 5,
                    y + 85
            );
        }
    }

    // NORMAL BUTTON
    

    private JButton createStyledButton(
            String text
    ) {

        JButton btn =
                new JButton(text);

        btn.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        btn.setFocusPainted(
                false
        );

        btn.setBackground(
                new Color(
                        35,
                        130,
                        210
                )
        );

        btn.setForeground(
                WHITE
        );

        btn.setBorder(
                new LineBorder(
                        BLUE_LIGHT,
                        1
                )
        );

        return btn;
    }

    // MAIN MENU BUTTON
    

    private JButton createMenuButton(
            String text
    ) {

        JButton btn =
                new JButton(text);

        btn.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        btn.setFocusPainted(
                false
        );

        btn.setForeground(
                WHITE
        );

        btn.setBackground(
                new Color(
                        35,
                        135,
                        215
                )
        );

        btn.setBorder(
                new LineBorder(
                        new Color(
                                55,
                                170,
                                245
                        ),
                        1
                )
        );

        btn.setPreferredSize(
                new Dimension(
                        220,
                        42
                )
        );

        return btn;
    }

    
    // MAIN
    

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(
                () -> {

                    CarRacingGame game =
                            new CarRacingGame();

                    game.setVisible(
                            true
                    );
                }
        );
    }
}