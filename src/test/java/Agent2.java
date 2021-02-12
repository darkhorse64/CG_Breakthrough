import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Agent2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) 
        {
/*
            for(int i = 0;i<8;i++)
            {
                String input = scanner.nextLine();
            }
*/
            String my_player = scanner.nextLine();
            
            int N = scanner.nextInt();
            scanner.nextLine();
            
            ArrayList<String> Moves = new ArrayList<String>();
            for(int i = 0;i<N;i++)
            {
            	Moves.add(scanner.nextLine());
            }
            
            System.out.println(Moves.get(0));
        }
    }
}
