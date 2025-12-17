package com.fruitfly;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FruitFlyGame extends Game {

	//input player name
    String currentPlayerName = "Player";

    @Override
    public void create() {
        DatabaseManager.init(); 
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        DatabaseManager.close();
    }

    // MAIN MENU
    private static class MainMenuScreen implements Screen {

        private final FruitFlyGame game;
        private SpriteBatch batch;
        private BitmapFont font;
        private ShapeRenderer shapeRenderer;

        private Rectangle startButton, leaderboardButton, exitButton;
        private int screenWidth, screenHeight;

        public MainMenuScreen(FruitFlyGame game) {
            this.game = game;

            batch = new SpriteBatch();
            font = new BitmapFont();
            shapeRenderer = new ShapeRenderer();

            screenWidth = Gdx.graphics.getWidth();
            screenHeight = Gdx.graphics.getHeight();

            float bw = 250f, bh = 60f;
            float cx = (screenWidth - bw) / 2f;

            startButton       = new Rectangle(cx, screenHeight / 2f + 60f, bw, bh);
            leaderboardButton = new Rectangle(cx, screenHeight / 2f - 20f, bw, bh);
            exitButton        = new Rectangle(cx, screenHeight / 2f - 100f, bw, bh);
        }

        @Override
        public void render(float delta) {
            Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            handleInput();

            // Buttons
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
            shapeRenderer.rect(startButton.x, startButton.y, startButton.width, startButton.height);
            shapeRenderer.rect(leaderboardButton.x, leaderboardButton.y, leaderboardButton.width, leaderboardButton.height);
            shapeRenderer.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
            shapeRenderer.end();

            // Text
            batch.begin();
            font.setColor(Color.WHITE);

            font.getData().setScale(2f);
            font.draw(batch, "Fruit Fly", (screenWidth / 2f) - 75f, screenHeight - 80f);
            font.getData().setScale(1f);

            drawText(batch, font, "Start Game", startButton);
            drawText(batch, font, "View Leaderboard", leaderboardButton);
            drawText(batch, font, "Close Game", exitButton);
            batch.end();
        }

        private void handleInput() {
            if (!Gdx.input.justTouched()) return;

            float x = Gdx.input.getX();
            float y = screenHeight - Gdx.input.getY();

            if (startButton.contains(x, y)) {
                game.setScreen(new NameEntryScreen(game));
            } else if (leaderboardButton.contains(x, y)) {
                game.setScreen(new LeaderboardScreen(game));
            } else if (exitButton.contains(x, y)) {
                Gdx.app.exit();
            }
        }

        private void drawText(SpriteBatch b, BitmapFont f, String text, Rectangle r) {
            f.draw(b, text, r.x + 20f, r.y + r.height / 2f + 5f);
        }

        @Override public void show() {}
        @Override public void resize(int w, int h) { screenWidth = w; screenHeight = h; }
        @Override public void pause() {}
        @Override public void resume() {}
        @Override public void hide() {}
        @Override public void dispose() { batch.dispose(); font.dispose(); shapeRenderer.dispose(); }
    }

    // NAME ENTRY SCREEN
    private static class NameEntryScreen implements Screen {

        private final FruitFlyGame game;
        private SpriteBatch batch;
        private BitmapFont font;
        private StringBuilder nameBuilder;
        private int screenWidth, screenHeight;
        private InputAdapter inputAdapter;

        public NameEntryScreen(FruitFlyGame game) {
            this.game = game;
            batch = new SpriteBatch();
            font = new BitmapFont();
            nameBuilder = new StringBuilder();
            screenWidth = Gdx.graphics.getWidth();
            screenHeight = Gdx.graphics.getHeight();
        }

        @Override
        public void show() {
            inputAdapter = new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.ENTER) {
                        String name = nameBuilder.toString().trim();
                        if (name.isEmpty()) {
                            name = "Player";
                        }
                        game.currentPlayerName = name;
                        Gdx.input.setInputProcessor(null);
                        game.setScreen(new GameScreen(game, name));
                        return true;
                    } else if (keycode == Input.Keys.ESCAPE) {
                        game.currentPlayerName = "Player";
                        Gdx.input.setInputProcessor(null);
                        game.setScreen(new GameScreen(game, "Player"));
                        return true;
                    } else if (keycode == Input.Keys.BACKSPACE) {
                        if (nameBuilder.length() > 0) {
                            nameBuilder.deleteCharAt(nameBuilder.length() - 1);
                        }
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean keyTyped(char character) {
                    if (character == '\r' || character == '\n') return false;
                    if (character < 32) return false; // control chars
                    if (nameBuilder.length() >= 20) return false;
                    nameBuilder.append(character);
                    return true;
                }
            };

            Gdx.input.setInputProcessor(inputAdapter);
        }

        @Override
        public void render(float delta) {
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.begin();

            font.setColor(Color.WHITE);
            font.getData().setScale(1.5f);
            font.draw(batch, "Enter your name:", screenWidth / 2f - 120f, screenHeight - 100f);

            font.getData().setScale(1.2f);
            String nameToShow = nameBuilder.length() == 0 ? "_" : nameBuilder.toString();
            font.draw(batch, nameToShow, screenWidth / 2f - 100f, screenHeight - 150f);

            font.getData().setScale(1f);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "Type your name and press Enter", screenWidth / 2f - 160f, screenHeight - 190f);
            font.draw(batch, "Press Esc to cancel (uses \"Player\")", screenWidth / 2f - 180f, screenHeight - 215f);

            batch.end();
        }

        @Override public void resize(int width, int height) { screenWidth = width; screenHeight = height; }
        @Override public void pause() {}
        @Override public void resume() {}
        @Override public void hide() {
            if (Gdx.input.getInputProcessor() == inputAdapter) {
                Gdx.input.setInputProcessor(null);
            }
        }
        @Override public void dispose() {
            if (Gdx.input.getInputProcessor() == inputAdapter) {
                Gdx.input.setInputProcessor(null);
            }
            batch.dispose();
            font.dispose();
        }
    }

 // GAME SCREEN
 private static class GameScreen implements Screen {

     private static class FlyingFruit {
         float x, y, w, h, vx, vy;
         Rectangle bounds;
         Texture texture;
         int points;

         FlyingFruit(float x, float y, float w, float h,
                     float vx, float vy,
                     Texture texture, int points) {
             this.x = x;
             this.y = y;
             this.w = w;
             this.h = h;
             this.vx = vx;
             this.vy = vy;
             this.texture = texture;
             this.points = points;
             this.bounds = new Rectangle(x, y, w, h);
         }

         void update(float dt, float gravity) {
             vy -= gravity * dt;
             x += vx * dt;
             y += vy * dt;
             bounds.set(x, y, w, h);
         }

         void draw(SpriteBatch batch) {
             batch.draw(texture, x, y, w, h);
         }
     }

     private final FruitFlyGame game;
     private final String playerName;

     private SpriteBatch batch;
     private Texture basket;
     private Texture appleTexture;
     private Texture grapeTexture;
     private Texture watermelonTexture;

     private BitmapFont font, bigFont;
     private float basketX, basketY, basketW, basketH;
     private Rectangle basketRect;

     private Array<FlyingFruit> fruits;
     private float spawnTimer = 0f;
     private float spawnInterval = 1f;
     private float gravity = 800f;
     private int score = 0;
     private int lives = 3;
     private boolean gameOver = false;

     private Random rand = new Random();
     private int screenWidth, screenHeight;

     public GameScreen(FruitFlyGame game, String playerName) {
         this.game = game;
         this.playerName = playerName;

         batch = new SpriteBatch();
         basket = new Texture("basket.png");

         appleTexture      = new Texture("apple.png");
         grapeTexture      = new Texture("grape.png");
         watermelonTexture = new Texture("watermelon.png"); 

         font = new BitmapFont();
         bigFont = new BitmapFont();

         screenWidth = Gdx.graphics.getWidth();
         screenHeight = Gdx.graphics.getHeight();

         basketW = basket.getWidth();
         basketH = basket.getHeight();
         basketX = screenWidth / 2f - basketW / 2f;
         basketY = 40f;
         basketRect = new Rectangle(basketX, basketY, basketW, basketH);

         fruits = new Array<FlyingFruit>();

         for (int i = 0; i < 3; i++) spawnFruit();
     }

     private void spawnFruit() {
         float s = 48f;

         
         float spawnRangeWidth = screenWidth * 0.6f;          
         float minX = (screenWidth - spawnRangeWidth) / 2f;    
         float x = minX + rand.nextFloat() * (spawnRangeWidth - s);

         float y = basketY + basketH + 10f;

         
         float vx = -70f + rand.nextFloat() * 140f; 

         // velocities
         float baseVy = 300f;         
         float extraVy = 120f;         
         float difficulty = 1f + score / 150f;

         float vy = (baseVy + rand.nextFloat() * extraVy) * difficulty;

         
         Texture tex;
         int points;

         int type = rand.nextInt(3); 
         switch (type) {
             case 1:
                 tex = grapeTexture;
                 points = 5;   
                 break;
             case 2:
                 tex = watermelonTexture;
                 points = 15; 
                 break;
             default:
                 tex = appleTexture;
                 points = 10;
                 break;
         }

         fruits.add(new FlyingFruit(x, y, s, s, vx, vy, tex, points));
     }

     @Override
     public void render(float dt) {
         Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

         if (gameOver) {
             batch.begin();
             bigFont.getData().setScale(2f);
             bigFont.draw(batch, "GAME OVER", screenWidth / 2f - 100f, screenHeight / 2f + 20f);
             font.draw(batch, "Click to return to menu", screenWidth / 2f - 120f, screenHeight / 2f - 10f);
             batch.end();

             if (Gdx.input.justTouched()) {
                 game.setScreen(new MainMenuScreen(game));
             }
             return;
         }

         float mouseX = Gdx.input.getX();
         basketX = mouseX - basketW / 2f;
         if (basketX < 0f) basketX = 0f;
         if (basketX > screenWidth - basketW) basketX = screenWidth - basketW;
         basketRect.setPosition(basketX, basketY);

         spawnTimer += dt;
         if (spawnTimer >= spawnInterval) {
             spawnFruit();
             spawnTimer = 0f;
         }

         for (int i = fruits.size - 1; i >= 0; i--) {
             FlyingFruit f = fruits.get(i);
             f.update(dt, gravity);

             if (f.y + f.h < 0f) {
                 fruits.removeIndex(i);
                 lives--;
                 if (lives <= 0) {
                     gameOver = true;
                     DatabaseManager.saveScore(playerName, score);
                 }
                 continue;
             }

             if (f.bounds.overlaps(basketRect)) {
                 score += f.points;
                 fruits.removeIndex(i);
                 if (spawnInterval > 0.4f) spawnInterval -= 0.02f;
             }
         }

         batch.begin();
         for (FlyingFruit f : fruits) {
             f.draw(batch);
         }
         batch.draw(basket, basketX, basketY, basketW, basketH);
         font.draw(batch, "Score: " + score, 20f, screenHeight - 20f);
         font.draw(batch, "Lives: " + lives, screenWidth - 120f, screenHeight - 20f);
         batch.end();
     }

     @Override public void show() {}
     @Override public void resize(int w, int h) { screenWidth = w; screenHeight = h; }
     @Override public void pause() {}
     @Override public void resume() {}
     @Override public void hide() {}

     @Override
     public void dispose() {
         batch.dispose();
         basket.dispose();
         appleTexture.dispose();
         grapeTexture.dispose();
         watermelonTexture.dispose();
         font.dispose();
         bigFont.dispose();
     }
 }

    // LEADERBOARD SCREEN
    private static class LeaderboardScreen implements Screen {

        private final FruitFlyGame game;
        private SpriteBatch batch;
        private BitmapFont font;
        private List<ScoreEntry> top;

        private int screenWidth, screenHeight;

        public LeaderboardScreen(FruitFlyGame game) {
            this.game = game;

            batch = new SpriteBatch();
            font = new BitmapFont();

            screenWidth = Gdx.graphics.getWidth();
            screenHeight = Gdx.graphics.getHeight();

            top = DatabaseManager.getTopScores(10);
        }

        @Override
        public void render(float dt) {
            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if (Gdx.input.justTouched()) {
                game.setScreen(new MainMenuScreen(game));
                return;
            }

            batch.begin();
            font.getData().setScale(2f);
            font.draw(batch, "Leaderboard", screenWidth / 2f - 120f, screenHeight - 60f);
            font.getData().setScale(1f);

            float y = screenHeight - 120f;
            int rank = 1;

            if (top == null || top.isEmpty()) {
                font.setColor(Color.LIGHT_GRAY);
                font.draw(batch, "No scores yet!", screenWidth / 2f - 80f, y);
            } else {
                font.setColor(Color.WHITE);
                for (ScoreEntry s : top) {
                    font.draw(batch, rank + ". " + s.userName + " - " + s.score,
                            screenWidth / 2f - 140f, y);
                    y -= 25f;
                    rank++;
                }
            }

            font.setColor(Color.GRAY);
            font.draw(batch, "Click to return", screenWidth / 2f - 80f, 40f);

            batch.end();
        }

        @Override public void show() {}
        @Override public void resize(int w, int h) { screenWidth = w; screenHeight = h; }
        @Override public void pause() {}
        @Override public void resume() {}
        @Override public void hide() {}
        @Override public void dispose() { batch.dispose(); font.dispose(); }
    }

    // DATABASE MANAGER (MySQL)
    private static class DatabaseManager {

        private static final String URL =
                "jdbc:mysql://localhost:3306/gamedata?useSSL=false&serverTimezone=UTC";

        private static final String USER = "root";       
        private static final String PASS = "";   
        private static Connection conn;

        public static void init() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);

                String sql = "CREATE TABLE IF NOT EXISTS Leaderboard (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "userName VARCHAR(50) NOT NULL," +
                        "score INT NOT NULL" +
                        ");";

                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                stmt.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void saveScore(String name, int score) {
            if (conn == null) return;
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Leaderboard (userName, score) VALUES (?, ?)"
                );
                ps.setString(1, name);
                ps.setInt(2, score);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static List<ScoreEntry> getTopScores(int limit) {
            List<ScoreEntry> list = new ArrayList<ScoreEntry>();
            if (conn == null) return list;

            try {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT userName, score FROM Leaderboard ORDER BY score DESC LIMIT ?"
                );
                ps.setInt(1, limit);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String userName = rs.getString("userName");
                    int score = rs.getInt("score");
                    list.add(new ScoreEntry(userName, score));
                }
                rs.close();
                ps.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        public static void close() {
            try {
                if (conn != null) conn.close();
            } catch (Exception ignored) {}
        }
    }

    private static class ScoreEntry {
        String userName;
        int score;
        ScoreEntry(String u, int s) { userName = u; score = s; }
    }
}
