import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import za.ac.wits.snake.DevelopmentAgent;

public class MyAgent extends DevelopmentAgent {

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
            int width = Integer.parseInt(temp[1]);
            int height = Integer.parseInt(temp[2]);

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                String apple = line;
                int appleX = Integer.parseInt(apple.split(" ")[0]);
                int appleY = Integer.parseInt(apple.split(" ")[1]);

                // Read obstacles
                List<int[]> obstacles = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    String obs = br.readLine();
                    String[] points = obs.split(" ");
                    for (String point : points) {
                        String[] coords = point.split(",");
                        obstacles.add(new int[]{Integer.parseInt(coords[0]), Integer.parseInt(coords[1])});
                    }
                }

                // Read zombies
                List<int[]> zombies = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    String zom = br.readLine();
                    String[] points = zom.split(" ");
                    for (String point : points) {
                        String[] coords = point.split(",");
                        zombies.add(new int[]{Integer.parseInt(coords[0]), Integer.parseInt(coords[1])});
                    }
                }

                int mySnakeNum = Integer.parseInt(br.readLine());
                List<int[]> snakes = new ArrayList<>();
                int[] mySnakeHead = null;
                int[] mySnakeFirstSegment = null;
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    if (snakeLine.startsWith("alive")) {
                        String[] parts = snakeLine.split(" ");
                        int headX = Integer.parseInt(parts[3].split(",")[0]);
                        int headY = Integer.parseInt(parts[3].split(",")[1]);
                        if (i == mySnakeNum) {
                            mySnakeHead = new int[]{headX, headY};
                            if (parts.length > 4) {
                                int firstSegmentX = Integer.parseInt(parts[4].split(",")[0]);
                                int firstSegmentY = Integer.parseInt(parts[4].split(",")[1]);
                                mySnakeFirstSegment = new int[]{firstSegmentX, firstSegmentY};
                            }
                        } else {
                            snakes.add(new int[]{headX, headY});
                        }
                    }
                }

                // Determine the current direction of the snake
                int currentDirection = getCurrentDirection(mySnakeHead, mySnakeFirstSegment);

                // Calculate move
                int move = calculateMove(mySnakeHead, appleX, appleY, obstacles, zombies, snakes, width, height, currentDirection);
                System.out.println(move);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getCurrentDirection(int[] head, int[] firstSegment) {
        if (firstSegment == null) {
            return -1; // Default direction if there's no body segment
        }
        int dx = head[0] - firstSegment[0];
        int dy = head[1] - firstSegment[1];
        if (dx == 1) return 3; // Right
        if (dx == -1) return 2; // Left
        if (dy == 1) return 1; // Down
        if (dy == -1) return 0; // Up
        return -1; // Unknown direction
    }

    private int calculateMove(int[] mySnakeHead, int appleX, int appleY, List<int[]> obstacles, List<int[]> zombies, List<int[]> snakes, int width, int height, int currentDirection) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Up, Down, Left, Right
        int[] moveScores = new int[4];
        Arrays.fill(moveScores, Integer.MAX_VALUE);

        for (int i = 0; i < directions.length; i++) {
            int newX = mySnakeHead[0] + directions[i][0];
            int newY = mySnakeHead[1] + directions[i][1];
            if (isValidMove(newX, newY, width, height, obstacles, zombies, snakes)) {
                moveScores[i] = Math.abs(newX - appleX) + Math.abs(newY - appleY); // Manhattan distance to apple
            }
        }

        int bestMove = 0;
        int minScore = moveScores[0];
        for (int i = 1; i < moveScores.length; i++) {
            if (moveScores[i] < minScore) {
                minScore = moveScores[i];
                bestMove = i;
            }
        }

        return bestMove;
    }

    private boolean isValidMove(int x, int y, int width, int height, List<int[]> obstacles, List<int[]> zombies, List<int[]> snakes) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false; // Out of bounds
        }
        for (int[] obstacle : obstacles) {
            if (obstacle[0] == x && obstacle[1] == y) {
                return false; // Collision with obstacle
            }
        }
        for (int[] zombie : zombies) {
            if (zombie[0] == x && zombie[1] == y) {
                return false; // Collision with zombie
            }
        }
        for (int[] snake : snakes) {
            if (snake[0] == x && snake[1] == y) {
                return false; // Collision with other snake
            }
        }
        return true;
    }
}