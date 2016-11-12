
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Joe Meszar
 * CS1550 Fall 2016
 * VM Simulator: Page Replacement Algorithms
 * 
 */

public class vmsim {
    static int numFrames = -1;
    static int refresh = -1;
    static int tau = -1;
    static String algorithm="", traceFile="";
    
    public static void main(String[] args) {
        //getCommandLineArgs(args);
        
        numFrames = 8;
        refresh = 3;
        algorithm = "clock";
        traceFile = "gcc.trace";
        
        ClockPageTable clockRAM = new ClockPageTable(numFrames);
        AgingPageTable agingRAM = new AgingPageTable(numFrames, refresh);
        
        String address = null;
        boolean isRead = true;
        
        BufferedReader reader = null;
        try {
            File file = new File(traceFile);
            reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                address = line.substring(0, 8);
                if (line.substring(9, 10).equals("R")) {
                    clockRAM.read(address);
                    agingRAM.read(address);
                } else {
                    clockRAM.write(address);
                    agingRAM.write(address);
                }
            }
            
            System.out.println("Aging:\n" + agingRAM + "\n");
            System.out.println("Clock:\n" + clockRAM + "\n");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
    
    private static void getCommandLineArgs(String[] args) {
        try {
            for (int i=0; i < args.length; i++) {
                
                switch (args[i]) {
                    case "-n": // get number of frames
                        numFrames = Integer.parseInt(args[i+1]);
                        break;
                        
                    case "-a": // get the algorithm type to use
                        switch (args[i+1]) {
                            case "aging":   // aging algorithm that approximates LRU with 8bit counter
                            case "clock":   // better implementation of the (FiFo) 2nd chance algorithm
                            case "opt":     // optimal page replacement
                            case "work":    // TBA 
                                algorithm = args[i+1];
                                break;
                                
                            default:
                                throw new Exception("Invalid algorithm type specified");
                        }
                        break;
                        
                    case "-r": // get refresh (aging)
                        refresh = Integer.parseInt(args[i+1]);
                        break;
                        
                    case "-t": // get working set clock
                        tau = Integer.parseInt(args[i+1]);
                        break;
                        
                    default: // bad input
                        traceFile = args[i];
                }  
            }            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(String.format("\n\njava vmsim –n <numframes> ‐a <opt|clock|aging|work> [‐r <refresh>] [‐t <tau>] <tracefile>\n"));
        }
    }
}
