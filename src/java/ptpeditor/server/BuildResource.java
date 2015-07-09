/* 
 *  PTP Online Editor
 *   Developed by Mitch Price under supervision of Dr. Jeff Overbey
 *   Auburn University, 2015.
 *   
 *   TODO: find an appropriate license and insert it here.
 *
 *   This project would not be possible without the following open source projects:
 *   CodeMirror Online Editor, by Marijn Haverbeke and others    (codemirror.net)   
 *   jquery-console by chrisdone.    (github.com/chrisdone/jquery-console)
 *   jquery File Tree by Cory LaViska    (abeautifulsite.net)
 */
package ptpeditor.server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * REST Web Service
 * NOTE: This class is deprecated.
 * @author Mitch
 */
@Path("build/{fileType}/{fileName}")
public class BuildResource {

    /**
     * Creates a new instance of BuildResource
     */
    public BuildResource() {
    }

    /**
     * Retrieves representation of an instance of ptpeditor.server.BuildResource
     * @return an instance of java.lang.String
     * @param fileType The file's extension, as determined by the path.
     * @param fileName The name of the file, as determined by the path.
     */
    @GET
    @Produces("text/plain")
    public String getText(@PathParam("fileType")String fileType, @PathParam("fileName")String fileName) {
        
        String workspace = "C:\\Users\\Mitch\\Documents\\PTPworkspace\\";
        
        String file = fileName + "." + fileType;
        
        if(fileType.equals("java")) {
        
            try {
                Process p = Runtime.getRuntime().exec("javac " + workspace + file);
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String output = "", line;
                while((line = in.readLine()) != null) {
                    System.out.println("line");
                    output += line + "\n";
                }
                if(output.equals("")) {
                    return "BUILD SUCCESS!";
                } else {
                    return output;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "An error occurred!";
            }
        } else {
            return "File Type currently unsupported.";
        }
    }
}
