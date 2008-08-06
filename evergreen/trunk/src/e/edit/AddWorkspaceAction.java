package e.edit;

import e.util.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Opens a dialog where the user can specify the details of a new workspace, which will then be created.
 */
public class AddWorkspaceAction extends AbstractAction {
    public AddWorkspaceAction() {
        GuiUtilities.configureAction(this, "_Add Workspace...", null);
    }
    
    public void actionPerformed(ActionEvent e) {
        WorkspaceProperties properties = new WorkspaceProperties();
        properties.name = "";
        properties.rootDirectory = null;
        properties.buildTarget = null;
        
        if (properties.showWorkspacePropertiesDialog("Add Workspace", "Add") == true) {
            Evergreen.getInstance().createWorkspace(properties);
        }
    }
}
