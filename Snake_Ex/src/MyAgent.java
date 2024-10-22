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
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    if (snakeLine.startsWith("alive")) {
                        String[] parts = snakeLine.split(" ");
                        int headX = Integer.parseInt(parts[3].split(",")[0]);
                        int headY = Integer.parseInt(parts[3].split(",")[1]);
                        if (i == mySnakeNum) {
                            mySnakeHead = new int[]{headX, headY};
                        } else {
                            snakes.add(new int[]{headX, headY});
                        }
                    }
                }

                // Calculate move
                int move = calculateMove(mySnakeHead, appleX, appleY, obstacles, zombies, snakes, width, height);
                System.out.println(move);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateMove(int[] mySnakeHead, int appleX, int appleY, List<int[]> obstacles, List<int[]> zombies, List<int[]> snakes, int width, int height) {
        // Implement logic to avoid obstacles, zombies, and other snakes
        // For now, just move randomly
        Random random = new Random();
        return random.nextInt(4);
    }
}