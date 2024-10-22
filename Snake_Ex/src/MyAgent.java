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
                    System.err.println("Invalid apple coordinates: " + apple);
                    continue;
                }

                // Read obstacles
                Set<String> obstacles = new HashSet<>();
                for (int i = 0; i < 3; i++) {
                    String obs = br.readLine();
                    String[] parts = obs.split(" ");
                    for (String part : parts) {
                        obstacles.add(part);
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

                int currentDirection = getCurrentDirection(mySnakeHead, mySnakeFirstSegment);

                int move = calculateMove(mySnakeHead, appleX, appleY, obstacles, zombies, snakePositions, snakes, width, height, currentDirection);
                System.out.println(move);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getCurrentDirection(int[] head, int[] firstSegment) {
        if (firstSegment == null) {
            return -1;
        }
        int dx = head[0] - firstSegment[0];
        int dy = head[1] - firstSegment[1];
        if (dx == 1) return 3;
        if (dx == -1) return 2;
        if (dy == 1) return 1;
        if (dy == -1) return 0;
        return -1;
    }

    private int calculateMove(int[] mySnakeHead, int appleX, int appleY, Set<String> obstacles, List<int[]> zombies, Set<String> snakePositions, Map<Integer, List<int[]>> snakes, int width, int height, int currentDirection) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
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

                if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || closedSet.contains(newPos) || snakePositions.contains(newPos)) {
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

                int tentativeG = current.g + 1;
                if (gScoreMap.containsKey(newPos) && tentativeG >= gScoreMap.get(newPos)) {
                    continue;
                }

                gScoreMap.put(newPos, tentativeG);
                Node neighbor = new Node(newX, newY, tentativeG, heuristic(newX, newY, appleX, appleY), current);
                openSet.add(neighbor);
            }
        }

        int bestMove = currentDirection;
        int maxOpenSpaces = -1;
        for (int i = 0; i < directions.length; i++) {
            int newX = mySnakeHead[0] + directions[i][0];
            int newY = mySnakeHead[1] + directions[i][1];
            String newPos = newX + "," + newY;

            if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || snakePositions.contains(newPos)) {
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
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private int reconstructPath(Node node) {
        Node current = node;
        while (current.parent != null && current.parent.parent != null) {
            current = current.parent;
        }
        if (current.x > current.parent.x) return 3;
        if (current.x < current.parent.x) return 2;
        if (current.y > current.parent.y) return 1;
        if (current.y < current.parent.y) return 0;
        return -1;
    }

    private int openSpaceHeuristic(int x, int y, int width, int height, Set<String> obstacles, Map<Integer, List<int[]>> snakes, List<int[]> zombies) {
        int openSpaces = 0;
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
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
        int maxProximity = 5;
    
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int curX = current[0];
            int curY = current[1];
            if (curX < 0 || curX >= width || curY < 0 || curY >= height || visited[curX][curY]) {
                continue;
            }
            if (isCollision(curX, curY, obstacles, snakes, zombies)) {
                continue;
            }
    
            visited[curX][curY] = true;
            openSpaces++;
            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] dir : directions) {
                int newX = curX + dir[0];
                int newY = curY + dir[1];
                if (newX >= 0 && newX < width && newY >= 0 && newY < height && Math.abs(newX - x) <= maxProximity && Math.abs(newY - y) <= maxProximity) {
                    queue.add(new int[]{newX, newY});
                }
            }
        }
    
        return openSpaces > 5;
    }
    
    private boolean isCollision(int x, int y, Set<String> obstacles, Map<Integer, List<int[]>> snakes, List<int[]> zombies) {
        String pos = x + "," + y;
        if (obstacles.contains(pos)) {
            return true;
        }
        for (List<int[]> snakeBody : snakes.values()) {
            for (int i = 0; i < snakeBody.size() - 1; i++) {
                int[] start = snakeBody.get(i);
                int[] end = snakeBody.get(i + 1);
                for (int[] point : getPointsBetween(start, end)) {
                    if (point[0] == x && point[1] == y) {
                        return true;
                    }
                }
            }
        }
        for (int i = 0; i < zombies.size() - 1; i++) {
            int[] start = zombies.get(i);
            int[] end = zombies.get(i + 1);
            for (int[] point : getPointsBetween(start, end)) {
                if (point[0] == x && point[1] == y) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private List<int[]> getPointsBetween(int[] start, int[] end) {
        List<int[]> points = new ArrayList<>();
        int x1 = start[0], y1 = start[1];
        int x2 = end[0], y2 = end[1];
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int[] lastPoint = new int[]{x1, y1};
    
        while (true) {
            int[] newPoint = new int[]{x1, y1};
            if (!points.isEmpty() && Arrays.equals(points.get(points.size() - 1), newPoint)) {
                break; // Prevent turning back on itself
            }
            points.add(newPoint);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (x1 == x2 && y1 == y2) {
                newPoint = new int[]{x1, y1};
                if (!Arrays.equals(lastPoint, newPoint)) {
                    points.add(newPoint);
                }
                break;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
            lastPoint = new int[]{x1, y1};
        }
        return points;
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
}