import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import za.ac.wits.snake.DevelopmentAgent;

/**
 * MyAgent is an AI agent for a snake game that extends the DevelopmentAgent class.
 * It uses the Alpha-Beta pruning algorithm to determine the best move for the snake
 * to reach the apple while avoiding obstacles, zombies, and other snakes.
 * 
 * The main method initializes the agent and starts the game loop.
 * 
 * The run method reads the game state from standard input, including the number of snakes,
 * the game board dimensions, the positions of obstacles, zombies, and snakes, and the position
 * of the apple. It then calculates the best move for the snake using the Alpha-Beta pruning algorithm
 * and prints the move to standard output.
 * 
 * The getCurrentDirection method determines the current direction of the snake based on the positions
 * of its head and the first body segment.
 * 
 * The alphaBeta method implements the Alpha-Beta pruning algorithm to calculate the best move for the snake.
 * It recursively evaluates the game state to a specified depth and returns the best move.
 * 
 * The evaluateState method evaluates the game state using a simple heuristic: the Manhattan distance to the apple.
 * 
 * The predictSnakePosition method predicts the future position of a snake based on its current position and direction.
 * 
 * The predictZombiePosition method predicts the future position of a zombie based on its current position and the target position.
 * 
 * The heuristic method calculates the Manhattan distance between two points.
 * 
 * The reconstructPath method reconstructs the path from a given node to the root node and returns the direction of the first move.
 * 
 * The Node class represents a node in the search tree used by the Alpha-Beta pruning algorithm.
 * 
 * The openSpaceHeuristic method calculates the number of open spaces around a given position.
 * 
 * The floodFill method performs a flood fill algorithm to determine if there are enough open spaces around a given position.
 */
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
                Set<String> obstacles = new HashSet<>();
                for (int i = 0; i < 3; i++) {
                    String obs = br.readLine();
                    String[] points = obs.split(" ");
                    for (String point : points) {
                        obstacles.add(point);
                    }
                }

                // Read zombies
                Set<String> zombies = new HashSet<>();
                for (int i = 0; i < 3; i++) {
                    String zom = br.readLine();
                    String[] points = zom.split(" ");
                    for (String point : points) {
                        zombies.add(point);
                    }
                }

                int mySnakeNum = Integer.parseInt(br.readLine());
                Set<String> snakes = new HashSet<>();
                List<int[]> snakeHeads = new ArrayList<>();
                int[] mySnakeHead = null;
                int[] mySnakeFirstSegment = null;
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    if (snakeLine.startsWith("alive")) {
                        String[] parts = snakeLine.split(" ");
                        int headX = Integer.parseInt(parts[3].split(",")[0]);
                        int headY = Integer.parseInt(parts[3].split(",")[1]);
                        snakeHeads.add(new int[]{headX, headY});
                        if (i == mySnakeNum) {
                            mySnakeHead = new int[]{headX, headY};
                            if (parts.length > 4) {
                                int firstSegmentX = Integer.parseInt(parts[4].split(",")[0]);
                                int firstSegmentY = Integer.parseInt(parts[4].split(",")[1]);
                                mySnakeFirstSegment = new int[]{firstSegmentX, firstSegmentY};
                            }
                        }
                        for (int j = 3; j < parts.length; j++) {
                            snakes.add(parts[j]);
                        }
                    }
                }

                // Determine the current direction of the snake
                int currentDirection = getCurrentDirection(mySnakeHead, mySnakeFirstSegment);

                // Calculate move using Alpha-Beta Pruning
                int move = alphaBeta(mySnakeHead, appleX, appleY, obstacles, zombies, snakes, snakeHeads, width, height, currentDirection, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
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

    private int alphaBeta(int[] mySnakeHead, int appleX, int appleY, Set<String> obstacles, Set<String> zombies, Set<String> snakes, List<int[]> snakeHeads, int width, int height, int currentDirection, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0) {
            return evaluateState(mySnakeHead, appleX, appleY, obstacles, zombies, snakes, width, height);
        }

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Up, Down, Left, Right
        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            int bestMove = currentDirection;
            for (int i = 0; i < directions.length; i++) {
                int newX = mySnakeHead[0] + directions[i][0];
                int newY = mySnakeHead[1] + directions[i][1];
                String newPos = newX + "," + newY;

                if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || snakes.contains(newPos) || zombies.contains(newPos)) {
                    continue;
                }

                // Predict future snake positions
                boolean futureSnakeCollision = false;
                for (int[] snakeHead : snakeHeads) {
                    int[] futureSnakePos = predictSnakePosition(snakeHead, directions[i]);
                    if (futureSnakePos[0] == newX && futureSnakePos[1] == newY) {
                        futureSnakeCollision = true;
                        break;
                    }
                }
                if (futureSnakeCollision) {
                    continue;
                }

                // Predict future zombie positions
                boolean futureZombieCollision = false;
                for (String zombie : zombies) {
                    int[] zombieCoords = Arrays.stream(zombie.split(",")).mapToInt(Integer::parseInt).toArray();
                    int[] futureZombiePos = predictZombiePosition(zombieCoords, mySnakeHead);
                    if (futureZombiePos[0] == newX && futureZombiePos[1] == newY) {
                        futureZombieCollision = true;
                        break;
                    }
                }
                if (futureZombieCollision) {
                    continue;
                }

                // Avoid apple collision with other snakes
                if (newX == appleX && newY == appleY) {
                    boolean appleCollision = false;
                    for (int[] snakeHead : snakeHeads) {
                        if (snakeHead[0] == appleX && snakeHead[1] == appleY) {
                            appleCollision = true;
                            break;
                        }
                    }
                    if (appleCollision) {
                        continue;
                    }
                }

                int[] newHead = {newX, newY};
                int eval = alphaBeta(newHead, appleX, appleY, obstacles, zombies, snakes, snakeHeads, width, height, i, depth - 1, alpha, beta, false);
                if (eval > maxEval) {
                    maxEval = eval;
                    bestMove = i;
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestMove;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < directions.length; i++) {
                int newX = mySnakeHead[0] + directions[i][0];
                int newY = mySnakeHead[1] + directions[i][1];
                String newPos = newX + "," + newY;

                if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || snakes.contains(newPos) || zombies.contains(newPos)) {
                    continue;
                }

                // Predict future snake positions
                boolean futureSnakeCollision = false;
                for (int[] snakeHead : snakeHeads) {
                    int[] futureSnakePos = predictSnakePosition(snakeHead, directions[i]);
                    if (futureSnakePos[0] == newX && futureSnakePos[1] == newY) {
                        futureSnakeCollision = true;
                        break;
                    }
                }
                if (futureSnakeCollision) {
                    continue;
                }

                // Predict future zombie positions
                boolean futureZombieCollision = false;
                for (String zombie : zombies) {
                    int[] zombieCoords = Arrays.stream(zombie.split(",")).mapToInt(Integer::parseInt).toArray();
                    int[] futureZombiePos = predictZombiePosition(zombieCoords, mySnakeHead);
                    if (futureZombiePos[0] == newX && futureZombiePos[1] == newY) {
                        futureZombieCollision = true;
                        break;
                    }
                }
                if (futureZombieCollision) {
                    continue;
                }

                // Avoid apple collision with other snakes
                if (newX == appleX && newY == appleY) {
                    boolean appleCollision = false;
                    for (int[] snakeHead : snakeHeads) {
                        if (snakeHead[0] == appleX && snakeHead[1] == appleY) {
                            appleCollision = true;
                            break;
                        }
                    }
                    if (appleCollision) {
                        continue;
                    }
                }

                int[] newHead = {newX, newY};
                int eval = alphaBeta(newHead, appleX, appleY, obstacles, zombies, snakes, snakeHeads, width, height, i, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private int evaluateState(int[] mySnakeHead, int appleX, int appleY, Set<String> obstacles, Set<String> zombies, Set<String> snakes, int width, int height) {
        // Simple evaluation function: Manhattan distance to the apple
        return -heuristic(mySnakeHead[0], mySnakeHead[1], appleX, appleY);
    }

    private int[] predictSnakePosition(int[] snakeHead, int[] direction) {
        return new int[]{snakeHead[0] + direction[0], snakeHead[1] + direction[1]};
    }

    private int[] predictZombiePosition(int[] zombie, int[] target) {
        int dx = target[0] - zombie[0];
        int dy = target[1] - zombie[1];
        if (Math.abs(dx) > Math.abs(dy)) {
            return new int[]{zombie[0] + Integer.signum(dx), zombie[1]};
        } else {
            return new int[]{zombie[0], zombie[1] + Integer.signum(dy)};
        }
    }

    private int heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2); // Manhattan distance
    }

    private int reconstructPath(Node node) {
        Node current = node;
        while (current.parent != null && current.parent.parent != null) {
            current = current.parent;
        }
        if (current.x > current.parent.x) return 3; // Right
        if (current.x < current.parent.x) return 2; // Left
        if (current.y > current.parent.y) return 1; // Down
        if (current.y < current.parent.y) return 0; // Up
        return -1; // Unknown direction
    }

    private static class Node {
        int x, y, g, f;
        Node parent;

        Node(int x, int y, int g, int h, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = g + h;
            this.parent = parent;
        }
    }

    private int openSpaceHeuristic(int x, int y, int width, int height, Set<String> obstacles, Set<String> snakes, Set<String> zombies) {
        int openSpaces = 0;
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Up, Down, Left, Right
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            String newPos = newX + "," + newY;
            if (newX >= 0 && newX < width && newY >= 0 && newY < height && !obstacles.contains(newPos) && !snakes.contains(newPos) && !zombies.contains(newPos)) {
                openSpaces++;
            }
        }
        return openSpaces;
    }

    private boolean floodFill(int x, int y, int width, int height, Set<String> obstacles, Set<String> snakes, Set<String> zombies) {
        boolean[][] visited = new boolean[width][height];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{x, y});
        int openSpaces = 0;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int curX = current[0];
            int curY = current[1];
            if (curX < 0 || curX >= width || curY < 0 || curY >= height || visited[curX][curY]) {
                continue;
            }
            String pos = curX + "," + curY;
            if (obstacles.contains(pos) || snakes.contains(pos) || zombies.contains(pos)) {
                continue;
            }
            visited[curX][curY] = true;
            openSpaces++;
            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Up, Down, Left, Right
            for (int[] dir : directions) {
                queue.add(new int[]{curX + dir[0], curY + dir[1]});
            }
        }

        return openSpaces > 10; // Arbitrary threshold to avoid small enclosed areas
    }
}