import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
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

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }

                String apple1 = line;
                
                //do stuff with apples

                // read in obstacles and do something with them!
                int nObstacles = 3;
                for (int obstacle = 0; obstacle < nObstacles; obstacle++) {
                    String obs = br.readLine();
                    /// do something with obs
                }

                // read in zombies and do something with them!
                int nZombies = 3;
                for (int zombie = 0; zombie < nZombies; zombie++) {
                    String zom = br.readLine();
                    /// do something with zom
                }
            

                int mySnakeNum = Integer.parseInt(br.readLine());
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    if (i == mySnakeNum) {
                        //hey! That's me :)
                    }
                    //do stuff with other snakes
                }
                //finished reading, calculate move:
                int move = new Random().nextInt(4);
                System.out.println(move);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}