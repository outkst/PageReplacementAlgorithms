
/**
 *
 * @author Joe Meszar
 * CS1550 Fall 2016
 * VM Simulator: Page Replacement Algorithms
 */
public class vmsim {
    public static void main(String[] args) {
        int numFrames = 0;
        try {
            if (!args[0].equals("-n")) {
                throw new Exception("improper parameter");
                
            } else {
                numFrames = Integer.parseInt(args[1]);
            }
        } catch (Exception e) {
            System.out.println("fuck");
        }
        System.out.println("Hello");
    }
}
