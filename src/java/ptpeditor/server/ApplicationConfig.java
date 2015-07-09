/*
 *  PTP Online Editor
 *  Developed by Mitch Price under supervision of Dr. Jeff Overbey
 *  Auburn University, 2015.
 *
 *  This project would not be possible without the following open source projects:
 *  CodeMirror Online Editor, by Marijn Haverbeke and others    (codemirror.net)   
 *  jquery-console by chrisdone.    (github.com/chrisdone/jquery-console)
 *  jquery File Tree by Cory LaViska    (abeautifulsite.net)
 */
package ptpeditor.server;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Mitch
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(ptpeditor.server.BuildResource.class);
        resources.add(ptpeditor.server.ProjectResource.class);
        resources.add(ptpeditor.server.WorkspaceResource.class);
    }
    
}
