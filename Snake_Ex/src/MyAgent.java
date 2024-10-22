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
                        }
                        for (int j = 3; j < parts.length; j++) {
                            snakes.add(parts[j]);
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

    private int calculateMove(int[] mySnakeHead, int appleX, int appleY, Set<String> obstacles, Set<String> zombies, Set<String> snakes, int width, int height, int currentDirection) {
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

                if (newX < 0 || newX >= width || newY < 0 || newY >= height || obstacles.contains(newPos) || zombies.contains(newPos) || snakes.contains(newPos) || closedSet.contains(newPos)) {
                    continue;
                }

                int tentativeG = current.g + 1;
                Node neighbor = new Node(newX, newY, tentativeG, heuristic(newX, newY, appleX, appleY), current);
                openSet.add(neighbor);
            }
        }

        return currentDirection; // Default to current direction if no path found
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
}