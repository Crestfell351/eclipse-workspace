import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class MyAgentTest {

    @Test
    public void testGetCurrentDirection() {
        MyAgent agent = new MyAgent();
        int[] head = {5, 5};
        int[] firstSegment = {5, 4};
        assertEquals(1, agent.getCurrentDirection(head, firstSegment));

        firstSegment = new int[]{5, 6};
        assertEquals(0, agent.getCurrentDirection(head, firstSegment));

        firstSegment = new int[]{4, 5};
        assertEquals(3, agent.getCurrentDirection(head, firstSegment));

        firstSegment = new int[]{6, 5};
        assertEquals(2, agent.getCurrentDirection(head, firstSegment));
    }

    @Test
    public void testPredictZombiePosition() {
        MyAgent agent = new MyAgent();
        int[] zombie = {2, 2};
        int[] target = {5, 2};
        int[] expected = {3, 2};
        assertArrayEquals(expected, agent.predictZombiePosition(zombie, target));

        target = new int[]{2, 5};
        expected = new int[]{2, 3};
        assertArrayEquals(expected, agent.predictZombiePosition(zombie, target));
    }

    @Test
    public void testHeuristic() {
        MyAgent agent = new MyAgent();
        assertEquals(5, agent.heuristic(2, 3, 5, 5));
        assertEquals(0, agent.heuristic(4, 4, 4, 4));
    }

    @Test
    public void testReconstructPath() {
        MyAgent.Node parent = new MyAgent.Node(1, 1, 0, 0, null);
        MyAgent.Node child = new MyAgent.Node(2, 1, 1, 0, parent);
        MyAgent.Node grandChild = new MyAgent.Node(3, 1, 2, 0, child);
        MyAgent agent = new MyAgent();
        assertEquals(3, agent.reconstructPath(grandChild));
    }

    @Test
    public void testOpenSpaceHeuristic() {
        MyAgent agent = new MyAgent();
        Set<String> obstacles = new HashSet<>();
        Map<Integer, List<int[]>> snakes = new HashMap<>();
        List<int[]> zombies = new ArrayList<>();
        assertEquals(4, agent.openSpaceHeuristic(2, 2, 5, 5, obstacles, snakes, zombies));
    }

    @Test
    public void testFloodFill() {
        MyAgent agent = new MyAgent();
        Set<String> obstacles = new HashSet<>();
        Map<Integer, List<int[]>> snakes = new HashMap<>();
        List<int[]> zombies = new ArrayList<>();
        assertTrue(agent.floodFill(2, 2, 5, 5, obstacles, snakes, zombies));
    }

    @Test
    public void testIsCollision() {
        MyAgent agent = new MyAgent();
        Set<String> obstacles = new HashSet<>();
        obstacles.add("2,2");
        Map<Integer, List<int[]>> snakes = new HashMap<>();
        List<int[]> zombies = new ArrayList<>();
        assertTrue(agent.isCollision(2, 2, obstacles, snakes, zombies));
        assertFalse(agent.isCollision(3, 3, obstacles, snakes, zombies));
    }

    @Test
    public void testGetPointsBetween() {
        MyAgent agent = new MyAgent();
        int[] start = {2, 2};
        int[] end = {4, 2};
        List<int[]> points = agent.getPointsBetween(start, end);
        assertEquals(3, points.size());
        assertArrayEquals(new int[]{2, 2}, points.get(0));
        assertArrayEquals(new int[]{3, 2}, points.get(1));
        assertArrayEquals(new int[]{4, 2}, points.get(2));
    }
}