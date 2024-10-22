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

                // Calculate move
                int move = calculateMove(mySnakeHead, appleX, appleY, obstacles, zombies, snakes, snakeHeads, width, height, currentDirection);
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

    private int calculateMove(int[] mySnakeHead, int appleX, int appleY, Set<String> obstacles, Set<String> zombies, Set<String> snakes, List<int[]> snakeHeads, int width, int height, int currentDirection) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Up, Down, Left, Right
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<String> closedSet = new HashSet<>();
        openSet.add(new Node(mySnakeHead[0], mySnakeHead[1], 0, heuristic(mySnakeHead[0], mySnakeHead[1], appleX, appleY), null));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.x == appleX && current.y == appleY) {
                return reconstructPath(current);
            }

            closedSet.add(current.x + "," + current.y);

            for (int i = 0; i < directions.length; i++) {
                int newX = current.x + directions[i][0];
                int newY = current.y + directions[i][1];
                String newPos = newX + "," + newY;

                if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || snakes.contains(newPos) || zombies.contains(newPos) || closedSet.contains(newPos)) {
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

                int tentativeG = current.g + 1;
                Node neighbor = new Node(newX, newY, tentativeG, heuristic(newX, newY, appleX, appleY), current);
                openSet.add(neighbor);
            }
        }

        // If no path found, use Open Space Heuristic and Flood Fill
        int bestMove = currentDirection;
        int maxOpenSpaces = -1;
        for (int i = 0; i < directions.length; i++) {
            int newX = mySnakeHead[0] + directions[i][0];
            int newY = mySnakeHead[1] + directions[i][1];
            String newPos = newX + "," + newY;

            if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || snakes.contains(newPos) || zombies.contains(newPos)) {
                continue;
            }

            int openSpaces = openSpaceHeuristic(newX, newY, width, height, obstacles, snakes, zombies);
            if (openSpaces > maxOpenSpaces && floodFill(newX, newY, width, height, obstacles, snakes, zombies)) {
                maxOpenSpaces = openSpaces;
                bestMove = i;
            }
        }

        return bestMove;
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