import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import za.ac.wits.snake.DevelopmentAgent;

class Snake {
    boolean alive;
    int length;
    int kills;
    int headX;
    int headY;
    Set<String> body;

    public Snake(boolean alive, int length, int kills, int headX, int headY, Set<String> body) {
        this.alive = alive;
        this.length = length;
        this.kills = kills;
        this.headX = headX;
        this.headY = headY;
        this.body = body;
    }
}

class Zombie {
    Set<String> body;

    public Zombie(Set<String> body) {
        this.body = body;
    }
}

class Apple {
    int x;
    int y;

    public Apple(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Obstacle {
    Set<String> points;

    public Obstacle(Set<String> points) {
        this.points = points;
    }
}

public class MyAgent extends DevelopmentAgent {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private static int lastMove = -1;

    public static void main(String args[]) {
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                // Parse apple position
                String[] applePos = line.split(" ");
                Apple apple = new Apple(Integer.parseInt(applePos[0]), Integer.parseInt(applePos[1]));

                // Read in obstacles
                Set<Obstacle> obstacles = new HashSet<>();
                int nObstacles = 3;
                for (int obstacle = 0; obstacle < nObstacles; obstacle++) {
                    String obs = br.readLine();
                    String[] obsPoints = obs.split(" ");
                    Set<String> points = new HashSet<>();
                    for (String point : obsPoints) {
                        points.add(point);
                    }
                    obstacles.add(new Obstacle(points));
                }

                // Read in zombies
                Set<Zombie> zombies = new HashSet<>();
                int nZombies = 3;
                for (int zombie = 0; zombie < nZombies; zombie++) {
                    String zom = br.readLine();
                    String[] zomPoints = zom.split(" ");
                    Set<String> points = new HashSet<>();
                    for (String point : zomPoints) {
                        points.add(point);
                    }
                    zombies.add(new Zombie(points));
                }

                // Read in snakes
                Set<Snake> snakes = new HashSet<>();
                int mySnakeNum = Integer.parseInt(br.readLine());
                Snake mySnake = null;
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    String[] snakeParts = snakeLine.split(" ");
                    boolean alive = snakeParts[0].equals("alive");
                    int length = Integer.parseInt(snakeParts[1]);
                    int kills = Integer.parseInt(snakeParts[2]);
                    int[] headPos = parsePosition(snakeParts[3]);
                    int headX = headPos[0];
                    int headY = headPos[1];
                    Set<String> body = new HashSet<>();
                    for (int j = 4; j < snakeParts.length; j++) { // Fix: Start from index 4
                        body.add(snakeParts[j]);
                    }
                    Snake snake = new Snake(alive, length, kills, headX, headY, body);
                    snakes.add(snake);
                    if (i == mySnakeNum) {
                        mySnake = snake;
                    }
                }

                // Calculate move
                try {
                    if (mySnake != null) {
                        int move = calculateBestMove(mySnake, apple, obstacles, snakes, zombies);

                        // If no safe move found, choose a random move
                        if (move == -1) {
                            log("No safe move found, choosing random move.");
                            move = new Random().nextInt(4);
                        }

                        if (move != lastMove) {
                            log("Move chosen: " + move);
                            lastMove = move;
                        }

                        System.out.println(move);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    log("Error: ArrayIndexOutOfBounds at move calculation: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateBestMove(Snake mySnake, Apple apple, Set<Obstacle> obstacles, Set<Snake> snakes, Set<Zombie> zombies) {
        int[] possibleMoves = {0, 1, 2, 3}; // Up, Down, Left, Right
        int bestMove = -1;
        int minDistanceToApple = Integer.MAX_VALUE;

        for (int move : possibleMoves) {
            if (move >= 0 && move < possibleMoves.length) { // Ensure move is within bounds
                int[] newHeadPos = getNewHeadPosition(mySnake.headX, mySnake.headY, move);
                log("Trying move: " + move + " -> new position: (" + newHeadPos[0] + "," + newHeadPos[1] + ")");
                if (!willCollide(newHeadPos[0], newHeadPos[1], obstacles, snakes, zombies)) {
                    int distanceToApple = Math.abs(newHeadPos[0] - apple.x) + Math.abs(newHeadPos[1] - apple.y);
                    log("Move: " + move + " -> distance to apple: " + distanceToApple);
                    if (distanceToApple < minDistanceToApple) {
                        minDistanceToApple = distanceToApple;
                        bestMove = move;
                        log("Best move updated to: " + bestMove);
                    }
                } else {
                    log("Collision detected for move: " + move);
                }
            }
        }

        return bestMove;
    }

    private static boolean willCollide(int x, int y, Set<Obstacle> obstacles, Set<Snake> snakes, Set<Zombie> zombies) {
        // Check for boundary collision
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return true;
        }

        String pos = x + "," + y;
        for (Obstacle obstacle : obstacles) {
            if (obstacle.points.contains(pos)) {
                return true;
            }
        }
        for (Snake snake : snakes) {
            if (snake.body.contains(pos) || (snake.headX == x && snake.headY == y)) {
                return true;
            }
        }
        for (Zombie zombie : zombies) {
            if (zombie.body.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    private static int[] getNewHeadPosition(int x, int y, int move) {
        switch (move) {
            case 0: return new int[]{x, y - 1}; // Up
            case 1: return new int[]{x, y + 1}; // Down
            case 2: return new int[]{x - 1, y}; // Left
            case 3: return new int[]{x + 1, y}; // Right
            default: return new int[]{x, y};
        }
    }

    private static int[] parsePosition(String pos) {
        String[] parts = pos.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid position format: " + pos);
        }
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    private static void log(String message) {
        System.err.println("log " + message);
    }
}