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
                int appleX = 0;
                int appleY = 0;
                try {
                    appleX = Integer.parseInt(apple.split(" ")[0]);
                    appleY = Integer.parseInt(apple.split(" ")[1]);
                } catch (NumberFormatException e) {
                    // Handle error: log or ignore invalid data
                    System.err.println("Invalid apple coordinates: " + apple);
                    continue;
                }

                // Read obstacles
                Set<String> obstacles = new HashSet<>();
                for (int i = 0; i < 3; i++) {
                    String obs = br.readLine();
                    String[] parts = obs.split(" ");
                    for (String part : parts) {
                        try {
                            obstacles.add(part);
                        } catch (Exception e) {
                            // Handle error: log or ignore invalid data
                            System.err.println("Invalid obstacle data: " + part);
                        }
                    }
                }

                // Read zombies
                List<int[]> zombies = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    String zom = br.readLine();
                    String[] parts = zom.split(" ");
                    for (String part : parts) {
                        try {
                            int x = Integer.parseInt(part.split(",")[0]);
                            int y = Integer.parseInt(part.split(",")[1]);
                            zombies.add(new int[]{x, y});
                        } catch (NumberFormatException e) {
                            // Handle error: log or ignore invalid data
                            System.err.println("Invalid zombie coordinates: " + part);
                        }
                    }
                }

                int mySnakeNum = Integer.parseInt(br.readLine());
                Map<Integer, List<int[]>> snakes = new HashMap<>();
                Set<String> snakePositions = new HashSet<>();
                int[] mySnakeHead = null;
                int[] mySnakeFirstSegment = null;
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    if (snakeLine.startsWith("alive")) {
                        String[] parts = snakeLine.split(" ");
                        List<int[]> snakeBody = new ArrayList<>();
                        for (int j = 3; j < parts.length; j++) {
                            try {
                                int x = Integer.parseInt(parts[j].split(",")[0]);
                                int y = Integer.parseInt(parts[j].split(",")[1]);
                                snakeBody.add(new int[]{x, y});
                                snakePositions.add(x + "," + y);
                            } catch (NumberFormatException e) {
                                // Handle error: log or ignore invalid data
                                System.err.println("Invalid snake coordinates: " + parts[j]);
                            }
                        }
                        snakes.put(i, snakeBody);
                        if (i == mySnakeNum) {
                            mySnakeHead = snakeBody.get(0);
                            if (snakeBody.size() > 1) {
                                mySnakeFirstSegment = snakeBody.get(1);
                            }
                        }
                    }
                }

                // Predict future positions of the snake's head and body segments
                Set<String> futureSnakePositions = new HashSet<>(snakePositions);
                for (List<int[]> snakeBody : snakes.values()) {
                    for (int[] segment : snakeBody) {
                        for (int[] dir : new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}}) {
                            int futureX = segment[0] + dir[0];
                            int futureY = segment[1] + dir[1];
                            futureSnakePositions.add(futureX + "," + futureY);
                        }
                    }
                }

                // Log current state
                System.err.println("Apple: (" + appleX + ", " + appleY + ")");
                System.err.println("My Snake Head: (" + mySnakeHead[0] + ", " + mySnakeHead[1] + ")");
                System.err.println("Obstacles: " + obstacles);
                System.err.println("Zombies: " + zombiesToString(zombies));
                System.err.println("Snake Positions: " + snakePositions);

                // Determine the current direction of the snake
                int currentDirection = getCurrentDirection(mySnakeHead, mySnakeFirstSegment);

                // Calculate move using A* algorithm
                int move = calculateMove(mySnakeHead, appleX, appleY, obstacles, zombies, futureSnakePositions, snakes, width, height, currentDirection);
                System.err.println("Calculated Move: " + move);
                if (move == -1) {
                    // If no valid move is found, default to moving straight
                    move = currentDirection;
                }
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

    private int calculateMove(int[] mySnakeHead, int appleX, int appleY, Set<String> obstacles, List<int[]> zombies, Set<String> futureSnakePositions, Map<Integer, List<int[]>> snakes, int width, int height, int currentDirection) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Up, Down, Left, Right
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<String> closedSet = new HashSet<>();
        Map<String, Integer> gScoreMap = new HashMap<>();
        openSet.add(new Node(mySnakeHead[0], mySnakeHead[1], 0, heuristic(mySnakeHead[0], mySnakeHead[1], appleX, appleY), null));
        gScoreMap.put(mySnakeHead[0] + "," + mySnakeHead[1], 0);

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

                if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || closedSet.contains(newPos) || futureSnakePositions.contains(newPos)) {
                    continue;
                }

                boolean collision = false;
                for (int[] zombie : zombies) {
                    int[] futureZombiePos = predictZombiePosition(zombie, mySnakeHead);
                    System.err.println("Zombie: (" + zombie[0] + ", " + zombie[1] + ") -> Future: (" + futureZombiePos[0] + ", " + futureZombiePos[1] + ")");
                    if (futureZombiePos[0] == newX && futureZombiePos[1] == newY) {
                        collision = true;
                        break;
                    }
                }
                if (collision) continue;

                int tentativeG = current.g + 1;
                if (gScoreMap.containsKey(newPos) && tentativeG >= gScoreMap.get(newPos)) {
                    continue;
                }

                gScoreMap.put(newPos, tentativeG);
                Node neighbor = new Node(newX, newY, tentativeG, heuristic(newX, newY, appleX, appleY), current);
                openSet.add(neighbor);
            }
        }

        int bestMove = -1;
        int maxOpenSpaces = -1;
        for (int i = 0; i < directions.length; i++) {
            int newX = mySnakeHead[0] + directions[i][0];
            int newY = mySnakeHead[1] + directions[i][1];
            String newPos = newX + "," + newY;

            if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || futureSnakePositions.contains(newPos)) {
                continue;
            }

            boolean collision = false;
            for (int[] zombie : zombies) {
                int[] futureZombiePos = predictZombiePosition(zombie, mySnakeHead);
                if (futureZombiePos[0] == newX && futureZombiePos[1] == newY) {
                    collision = true;
                    break;
                }
            }
            if (collision) continue;

            int openSpaces = openSpaceHeuristic(newX, newY, width, height, obstacles, snakes, zombies);
            if (openSpaces > maxOpenSpaces && floodFill(newX, newY, width, height, obstacles, snakes, zombies)) {
                maxOpenSpaces = openSpaces;
                bestMove = i;
            }
        }

        return bestMove;
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

    private int openSpaceHeuristic(int x, int y, int width, int height, Set<String> obstacles, Map<Integer, List<int[]>> snakes, List<int[]> zombies) {
        int openSpaces = 0;
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Up, Down, Left, Right
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            String newPos = newX + "," + newY;
            if (newX >= 0 && newX < width && newY >= 0 && newY < height && !obstacles.contains(newPos)) {
                boolean collision = false;
                for (List<int[]> snakeBody : snakes.values()) {
                    for (int[] segment : snakeBody) {
                        if (segment[0] == newX && segment[1] == newY) {
                            collision = true;
                            break;
                        }
                    }
                    if (collision) break;
                }
                if (!collision) {
                    for (int[] zombie : zombies) {
                        if (zombie[0] == newX && zombie[1] == newY) {
                            collision = true;
                            break;
                        }
                    }
                }
                if (!collision) {
                    openSpaces++;
                }
            }
        }
        return openSpaces;
    }

    private boolean floodFill(int x, int y, int width, int height, Set<String> obstacles, Map<Integer, List<int[]>> snakes, List<int[]> zombies) {
        boolean[][] visited = new boolean[width][height];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{x, y});
        int openSpaces = 0;
        int maxProximity = 5; // Limit the proximity to walls or boundaries

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int curX = current[0];
            int curY = current[1];
            if (curX < 0 || curX >= width || curY < 0 || curY >= height || visited[curX][curY]) {
                continue;
            }
            String pos = curX + "," + curY;
            if (obstacles.contains(pos)) {
                continue;
            }
            boolean collision = false;
            for (List<int[]> snakeBody : snakes.values()) {
                for (int[] segment : snakeBody) {
                    if (segment[0] == curX && segment[1] == curY) {
                        collision = true;
                        break;
                    }
                }
                if (collision) break;
            }
            if (collision) continue;

            for (int[] zombie : zombies) {
                if (zombie[0] == curX && zombie[1] == curY) {
                    collision = true;
                    break;
                }
            }
            if (collision) continue;

            visited[curX][curY] = true;
            openSpaces++;
            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // Up, Down, Left, Right
            for (int[] dir : directions) {
                int newX = curX + dir[0];
                int newY = curY + dir[1];
                if (Math.abs(newX - x) <= maxProximity && Math.abs(newY - y) <= maxProximity) {
                    queue.add(new int[]{newX, newY});
                }
            }
        }

        return openSpaces > 5; // Reduced threshold to avoid small enclosed areas
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

    private String zombiesToString(List<int[]> zombies) {
        StringBuilder sb = new StringBuilder();
        for (int[] zombie : zombies) {
            sb.append("[").append(zombie[0]).append(",").append(zombie[1]).append("] ");
        }
        return sb.toString();
    }
}