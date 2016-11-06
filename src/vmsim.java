/**
 *
 * @author Joe Meszar
 * CS1550 Fall 2016
 * VM Simulator: Page Replacement Algorithms
 * 
 */

public class vmsim {
    public static void main(String[] args) {
        int numFrames = 0;
        int refresh = 0;
        int tau = 0;
        String algorithm, traceFile;
        
        try {
            for (int i=0; i < args.length; i++) {
                System.out.println(String.format("args[%d]: %s", i, args[i]));
                
                switch (args[i]) {
                    case "-n": // get number of frames
                        numFrames = Integer.parseInt(args[i+1]);
                        break;
                        
                    case "-a": // get the algorithm type to use
                        switch (args[i+1]) {
                            case "aging":   // aging algorithm that approximates LRU with 8bit counter
                            case "clock:":  // better implementation of the (FiFo) 2nd chance algorithm
                            case "opt":     // optimal page replacement
                            case "work":    // TBA 
                                break;
                                
                            default:
                                throw new Exception("Invalid algorithm type specified");
                        }
                        
                    case "-r": // get refresh (aging)
                        refresh = Integer.parseInt(args[i+1]);
                        
                    case "-t": // get working set clock
                        tau = Integer.parseInt(args[i+1]);
                        
                    default: // bad input
                        traceFile = args[i];
                }  
            }            
        } catch (Exception e) {
            System.out.println(e.getMessage() + 
                    "\n\n./vmsim –n <numframes> ‐a <opt|clock|aging|work> [‐r <refresh>] [‐t <tau>] <tracefile>\n");
        }
        
        String asdf;
        asdf = "asdf";
    }
}
