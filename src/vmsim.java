
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
    static int memAccesses = 0;
    static String algorithm="", traceFile="";
    
    public static void main(String[] args) {
        //getCommandLineArgs(args);
        
        numFrames = 2;
        refresh = 3;
        tau = 2;
        algorithm = "opt";
        traceFile = "testTrace.trace";
        
        Object RAM;

        ClockPageTable clockRAM = new ClockPageTable(numFrames);
        AgingPageTable agingRAM = new AgingPageTable(numFrames, refresh);
        WorkingSetClockPageTable WSClockRAM = new WorkingSetClockPageTable(numFrames, refresh, tau);
        OptimalPageTable optimalRAM = new OptimalPageTable(numFrames, traceFile);
        
        String address = null;
        boolean isRead = true;
        
        BufferedReader reader = null;
        try {
            File file = new File(traceFile);
            reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                memAccesses++;
                
                address = line.split(" ")[0];
                if (line.split(" ")[1].equals("R")) {
                    switch (algorithm) {
                        case "aging":   // aging algorithm that approximates LRU with 8bit counter
                            agingRAM.read(address);
                            break;
                        case "clock":   // better implementation of the (FiFo) 2nd chance algorithm
                            clockRAM.read(address);
                            break;
                        case "opt":     // optimal page replacement
                            optimalRAM.read(address);
                            break;
                        case "work":    // working set clock (aging + clock)
                            WSClockRAM.read(address);
                            break;
                    }
                } else {
                    switch (algorithm) {
                        case "aging":   // aging algorithm that approximates LRU with 8bit counter
                            agingRAM.write(address);
                            break;
                        case "clock":   // better implementation of the (FiFo) 2nd chance algorithm
                            clockRAM.write(address);
                            break;
                        case "opt":     // optimal page replacement
                            optimalRAM.write(address);
                            break;
                        case "work":    // working set clock (aging + clock) 
                            WSClockRAM.write(address);
                            break;
                    }
                }
            }
            
            System.out.println(String.format("Algorithm:             %s", algorithm));
            System.out.println(String.format("Number of frames:      %d", numFrames));
            System.out.println(String.format("Total memory accesses: %d", memAccesses));
            
            // print out statistics for the algorithm
            switch (algorithm) {
                case "aging":   // aging algorithm that approximates LRU with 8bit counter
                    System.out.println(String.format("Total page faults:     %d", agingRAM.getNumPageFaults()));
                    System.out.println(String.format("Total writes to disk:  %d", agingRAM.getNumWritesToDisk()));
                    break;
                    
                case "clock":   // better implementation of the (FiFo) 2nd chance algorithm
                    System.out.println(String.format("Total page faults:     %d", clockRAM.getNumPageFaults()));
                    System.out.println(String.format("Total writes to disk:  %d", clockRAM.getNumWritesToDisk()));
                    break;
                    
                case "opt":     // optimal page replacement
                    System.out.println(String.format("Total page faults:     %d", optimalRAM.getNumPageFaults()));
                    System.out.println(String.format("Total writes to disk:  %d", optimalRAM.getNumWritesToDisk()));
                    break;
                    
                case "work":    // TBA 
                    System.out.println(String.format("Total page faults:     %d", WSClockRAM.getNumPageFaults()));
                    System.out.println(String.format("Total writes to disk:  %d", WSClockRAM.getNumWritesToDisk()));
                    break;
            }
            

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
