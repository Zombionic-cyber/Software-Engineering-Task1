import java.util.*; // import useful classes (Scanner, Random, Deque, etc.)

public class Main {

    // Game board size
    static final int WIDTH = 20;
    static final int HEIGHT = 12;

    // Possible directions for snake
    enum Dir { UP, DOWN, LEFT, RIGHT }

    // Position class (represents x,y location on grid)
    static class Pos {
        int x, y; // coordinates

        Pos(int x, int y) { // constructor
            this.x = x;
            this.y = y;
        }

        // checks if two positions are equal (used for collision)
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pos)) return false;
            Pos p = (Pos) o;
            return x == p.x && y == p.y;
        }

        // required when using HashSet
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public static void main(String[] args) throws Exception {

        Scanner in = new Scanner(System.in); // read user input
        Random rng = new Random(); // random number generator

        // Snake body (head is first element)
        Deque<Pos> snake = new ArrayDeque<>();

        // Create starting snake in middle
        snake.addFirst(new Pos(WIDTH / 2, HEIGHT / 2));      // head
        snake.addLast(new Pos(WIDTH / 2 - 1, HEIGHT / 2));   // body
        snake.addLast(new Pos(WIDTH / 2 - 2, HEIGHT / 2));   // tail

        Dir dir = Dir.RIGHT; // starting direction

        Pos food = spawnFood(rng, snake); // create first food

        int score = 0;
        int delayMs = 220; // speed (lower = faster)

        // Main game loop (runs until break)
        while (true) {

            clearScreen(); // fake screen refresh
            draw(WIDTH, HEIGHT, snake, food, score, dir); // print board

            System.out.print("Move (W/A/S/D): ");
            String line = in.nextLine().trim().toUpperCase(); // read input

            // Change direction if user typed something
            if (!line.isEmpty()) {
                char c = line.charAt(0);
                Dir newDir = dir;

                if (c == 'W') newDir = Dir.UP;
                else if (c == 'S') newDir = Dir.DOWN;
                else if (c == 'A') newDir = Dir.LEFT;
                else if (c == 'D') newDir = Dir.RIGHT;

                // prevent reversing into yourself
                if (!isOpposite(dir, newDir)) {
                    dir = newDir;
                }
            }

            Pos head = snake.peekFirst(); // current head
            Pos next = nextPos(head, dir); // calculate next head position

            // Check wall collision
            if (next.x < 0 || next.x >= WIDTH || next.y < 0 || next.y >= HEIGHT) {
                gameOver(score, "Hit the wall!");
                break;
            }

            boolean willEat = next.equals(food); // check if food eaten

            // Check if snake hits itself
            if (containsExceptTail(snake, next, willEat)) {
                gameOver(score, "Ran into yourself!");
                break;
            }

            snake.addFirst(next); // move head forward

            if (willEat) {
                score += 10; // increase score
                food = spawnFood(rng, snake); // new food
                delayMs = Math.max(70, delayMs - 10); // increase speed
            } else {
                snake.removeLast(); // remove tail (normal move)
            }

            Thread.sleep(delayMs); // control speed
        }

        in.close(); // close input
    }

    // Calculates next position based on direction
    static Pos nextPos(Pos p, Dir d) {
        if (d == Dir.UP) return new Pos(p.x, p.y - 1);
        if (d == Dir.DOWN) return new Pos(p.x, p.y + 1);
        if (d == Dir.LEFT) return new Pos(p.x - 1, p.y);
        return new Pos(p.x + 1, p.y); // RIGHT
    }

    // Prevents opposite direction movement
    static boolean isOpposite(Dir a, Dir b) {
        return (a == Dir.UP && b == Dir.DOWN) ||
                (a == Dir.DOWN && b == Dir.UP) ||
                (a == Dir.LEFT && b == Dir.RIGHT) ||
                (a == Dir.RIGHT && b == Dir.LEFT);
    }

    // Creates food in random position not inside snake
    static Pos spawnFood(Random rng, Deque<Pos> snake) {
        HashSet<Pos> occupied = new HashSet<>(snake); // store snake positions
        while (true) {
            Pos f = new Pos(rng.nextInt(WIDTH), rng.nextInt(HEIGHT));
            if (!occupied.contains(f)) return f; // return valid position
        }
    }

    // Checks if snake hits itself
    static boolean containsExceptTail(Deque<Pos> snake, Pos target, boolean willEat) {
        Pos tail = snake.peekLast();

        for (Pos p : snake) {
            if (p.equals(target)) {
                // allow stepping into tail if it will move away
                if (!willEat && p.equals(tail)) return false;
                return true;
            }
        }
        return false;
    }

    // Draws board, snake, and food
    static void draw(int w, int h, Deque<Pos> snake, Pos food, int score, Dir dir) {

        char[][] grid = new char[h][w]; // create empty grid
        for (int y = 0; y < h; y++)
            Arrays.fill(grid[y], ' ');

        grid[food.y][food.x] = '*'; // draw food

        boolean first = true;
        for (Pos p : snake) {
            grid[p.y][p.x] = first ? 'O' : 'o'; // head or body
            first = false;
        }

        System.out.println("Score: " + score + " | Direction: " + dir);

        System.out.println("+" + "-".repeat(w) + "+");
        for (int y = 0; y < h; y++) {
            System.out.print("|");
            for (int x = 0; x < w; x++)
                System.out.print(grid[y][x]);
            System.out.println("|");
        }
        System.out.println("+" + "-".repeat(w) + "+");
    }

    static void gameOver(int score, String reason) {
        System.out.println("\n=== GAME OVER ===");
        System.out.println(reason);
        System.out.println("Final Score: " + score);
    }

    // Fake screen clear (just prints many lines)
    static void clearScreen() {
        for (int i = 0; i < 30; i++)
            System.out.println();
    }
}