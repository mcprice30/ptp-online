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

//Global variables
var webSocket;
var console5 = $('<div class="console1">');
var controller5;
var activeFile;
var activeProject;
var numTabsOpened;
var tabList = [];
var activeTab;
var deferred;
var workspaceBase = null;
var userName;

//Will be removed. Displays text in the options bar.
function showRequest(text) {
    options.innerHTML =  text + "<br>";
}

//Gets the base string of the workspace location from a server-side GET.
function getWorkspaceBase() {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState === 4 && xmlhttp.status === 200) {
            workspaceBase = xmlhttp.responseText;
            controller5.report([{msg: "Workspace Ready: " + xmlhttp.responseText, className:"jquery-console-message-value"}]);
            //controller5.report([{msg:"\t[NEW PROJECT]: \n" + xmlhttp.responseText,className:"jquery-console-message-value"}]); 
        }
    };
    var loc = window.location, new_uri;
    new_uri = loc.protocol;
    new_uri += "//" + loc.host;
    new_uri += "/PTPEditor";
    new_uri += "/webresources/workspace";
    xmlhttp.open("GET", new_uri , true);
    xmlhttp.send();
}

//Prompts the user for a file name, which is stored in the fileType and fileName
//fields. This currently allows for only 1 file at a time.
function doSaveAs() {
    var prompt = "Enter a file name.";
    var valid = true, file;
    do {
        valid = true;
        file = window.prompt(prompt);
        var extPos = file.lastIndexOf(".");
        if(extPos > 0 && extPos < file.length - 1) {
            fileName = file.substring(0, extPos);
            fileType = file.substring(extPos + 1, file.length);
                    
            if(fileType === "c") {
                myCodeMirror.setOption("mode", "text/x-csrc");
            } else if (fileType === "java") {
                myCodeMirror.setOption("mode", "text/x-java");
            } else if (fileType === "f90") {
                myCodeMirror.setOption("mode", "text/x-Fortran");
            } else if (fileType === "cpp") {
                myCodeMirror.setOption("mode", "text/x-c++src");
            } else {
                myCodeMirror.setOption("mode", "text/plain");
            }
                    
        } else {
            prompt = "File name invalid! Please try again!";
            valid = false;
        }
    } while(!valid);
    //document.getElementById("editortabs").innerHTML = file;
}

//Prompts the user for a file name and creates the corresponding file. This is
//done by sending a websocket request to LoadServer.
function createFile(file, filePath) {
    var extPos = file.lastIndexOf(".");
    var fileType = file.substring(extPos + 1, file.length);
    activeFile = workspaceBase + "/" +  activeProject + "/";
    activeFile += filePath;
    if(filePath.substring(filePath.length-1, filePath.length) !== "/") {
        activeFile += "/";
    }
    activeFile += file;
            
    if(fileType === "c") {
        myCodeMirror.setOption("mode", "text/x-csrc");
    } else if (fileType === "java") {
        myCodeMirror.setOption("mode", "text/x-java");
    } else if (fileType === "f90") {
        myCodeMirror.setOption("mode", "text/x-Fortran");
    } else if (fileType === "cpp") {
        myCodeMirror.setOption("mode", "text/x-c++src");
    } else {
        myCodeMirror.setOption("mode", "text/plain");
    }
                    
    //document.getElementById("editortabs").innerHTML = file;
    webSocket.send("NEW " + activeFile);
    showProjectDirectory(activeProject);
    openNewTab(activeFile);
}

//Sends the file via a websocket to the server-side to be saved.
//This may need to be revisited for security/efficiency purposes.
function doSave() {
    saveFile();
}

//Adding the console to the bottom of the screen.
$(document).ready(function(){
             /* Fifth console */
         
    $("#cnsl").append(console5);
    controller5  = console5.console({
        promptLabel: 'Console> ',
        commandHandle:function(line){
            if (line) {
                showRequest(line);
                return [{msg:"",className:"jquery-console-message-value"}];
            } else {
                var m = "type a color among (" + this.colors.join(", ") + ")";
                return [{msg:m,className:"jquery-console-message-value"}];
            }
        },
        colors: ["red","blue","green","black","yellow","white","grey"],
        cols: 40,
        completeHandle:function(prefix){
            var colors = this.colors;
            var ret = [];
            for (var i=0;i<colors.length;i++) {
                var color=colors[i];
                if (color.lastIndexOf(prefix,0) === 0) {
                    ret.push(color.substring(prefix.length));
                }
            }   
            return ret;
        }
    });
});

//On a build request, this sends a GET request to the server, which attempts to
//build the project before sending compilation errors (if any) to the user.
function doBuild() {
    controller5.report([{msg:"[BUILD]: Preparing to build: " + activeFile, className:"jquery-console-message-value"}]);
    webSocket.send("BUILD " + activeFile);
}

//Performing automatic function calls
$(function() {
    bindPopup($('#contact'), $('.pop'));
    $("#ProjectName").on("blur", function() {
        $("#ProjectID").val( function() { 
            //var projectID = $("#ProjectName").val().replace(/\W+/g, '-').toLowerCase());
            var idFromName = $("#ProjectName").val().replace(/\W+/g, '-').toLowerCase();
            if(idFromName.indexOf("-") === 0) {
                idFromName = idFromName.substring(1, idFromName.length-1);
            }
            
            if(idFromName.lastIndexOf("-") === idFromName.length - 1) {
                idFromName = idFromName.substring(0, idFromName.length-1);
            }
            return idFromName;
        });
        
        $("#ProjectName").val($("#ProjectName").val().replace(/\W+/g, ""));
    });
    
    $("#createNewProject").on("click", function() {
        //Close the current popup.
        deselect($('#contact'), $('.pop'));
        //Create a new project directory.
        createNewProject($("#ProjectID").val(), $("#ProjectName").val());
    });
    
    $("#closeCurrentProject").on("click", function() {
       chainCloseAllTabs(false); 
    });
    
});

//Binding and adding functionaliy to the new file creation popup.
$(function(){
    bindPopup($('#new_file_trigger'), $('.newFilePop'));
    $("#FileName").on("blur", function() {
        $("#FileName").val($("#FileName").val().replace(/\s/g, ""));
    });
    
    $("#createNewFile").on("click", function() {
        //Close the current popup.
        deselect($('#new_file_trigger'), $('.newFilePop'));
        //Create a new project directory.
        createFile($("#FileName").val(), $("#FilePath").val());
    });   
});

//This function takes a projectID and Name and sends a get request to the
//ProjectResource server-side component. It then prints the response out to the console.
function createNewProject(ID, Name) {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState === 4 && xmlhttp.status === 200) {
            controller5.report([{msg:"\t[NEW PROJECT]: \n" + xmlhttp.responseText,className:"jquery-console-message-value"}]); 
        }
    };
    var loc = window.location, new_uri;
    new_uri = loc.protocol;
    new_uri += "//" + loc.host;
    new_uri += "/PTPEditor";
    new_uri += "/webresources/project/create";
    new_uri += "/" + ID;
    new_uri += "/" + Name;
    xmlhttp.open("GET", new_uri , true);
    xmlhttp.send();
    setTimeout(function(){
        listWorkspaceProjects();
    }, 100);
    
    setTimeout(function(){
        displayProjectOptions();
    }, 1500);
    
}

//Deselects a popup.
function deselect(launcher, launched) {
  launched.slideFadeToggle(function() {
    launcher.removeClass('selected');
  });    
}

//Binds a popup launcher to the popup itself.
function bindPopup(launcher, launched) {
    try {
    launcher.on('click', function() {
        if($(this).hasClass('selected')) {
          deselect($(this), launched);
        } else {
            $(this).addClass('selected');
            launched.slideFadeToggle();
        }
        return false;
    });
    } catch(e) {
        controller5.report([{msg:"[ERROR]: Error Binding launcher!", className:"jquery-console-message-value"}]);
    }
    launched.find('.close').on('click', function() {
        deselect(launcher, launched);
        return false;
    });
}

//Sends a websocket message to LoadServer
function saveFile() {
    controller5.report([{msg:"[INFO]: Your file is being saved.",className:"jquery-console-message-value"}]);
    webSocket.send("SAVE " + activeFile + " " + myCodeMirror.getValue());
    activeTab.dirty = false;
    $("#" + activeTab.id).removeClass("dirty_tab");
}

//Animates the popup
$.fn.slideFadeToggle = function(easing, callback) {
  return this.animate({ opacity: 'toggle', height: 'toggle' }, 'fast', easing, callback);
};

//Closes the editor, removing the codemirror display and
//replacing it with a background image.
function closeEditor() {
    $("#noEditorContainer").removeClass("inactive_editor_container");
    $("#noEditorContainer").addClass("active_editor_container");
    $("#editorContainer").removeClass("active_editor_container");
    $("#editorContainer").addClass("inactive_editor_container");
    $(".file_action").addClass("file_action_inactive");
    $(".file_action").css( 'pointer-events', 'none' );
}

//Opens the editor, adding the codemirror display over the background image.
function openEditor() {
    $("#noEditorContainer").removeClass("active_editor_container");
    $("#noEditorContainer").addClass("inactive_editor_container");
    $("#editorContainer").removeClass("inactive_editor_container");
    $("#editorContainer").addClass("active_editor_container");

    $(".file_action").removeClass("file_action_inactive");
    $(".file_action").css( 'pointer-events', 'auto' );
}

//This provides a list of links to every directory within the user's workspace.
//Each of the various links will direct the user to a project and invoke the
// showProjectDirectory function.
//This function is called upon closing the active project.
function listWorkspaceProjects() {
    closeEditor();
    //chainCloseAllTabs();
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState === 4 && xmlhttp.status === 200) {
            //controller5.report([{msg:"\t[PROJECT LIST]: \n" + xmlhttp.responseText,className:"jquery-console-message-value"}]); 
                  
            $("#fileTree_header").html("<h4>Your Projects:<h4>");
            $("#fileTree_container").html("");
            var projectListing = xmlhttp.responseText.split(/\n/);
            //var project;
            for (var i = 0; i < projectListing.length - 1; i++) {
                $("#fileTree_container").append("<p><a href='#' class='project_list_element'>" + projectListing[i] + "</a></p>");
            }
            $(".project_list_element").on('click', function() {
                showProjectDirectory($(this).text());
            });
        }
    };
    var loc = window.location, new_uri;
    new_uri = loc.protocol;
    new_uri += "//" + loc.host;
    new_uri += "/PTPEditor";
    new_uri += "/webresources/project/list";
    xmlhttp.open("GET", new_uri , true);
    xmlhttp.send();
    closeWebSocket();
    myCodeMirror.setOption("mode", "text/plain");
    myCodeMirror.setOption("value", "");
    numTabsOpened = 0;
    
    $(".project_action").css( 'pointer-events', 'none' );
    $(".project_action").addClass("project_action_inactive");
    displayEditorOptions();
}

//This uses the jquery file tree project to display the file contents of a
//particular project. This function is the primary one called to open a project.
function showProjectDirectory(projectName) {
    $("#fileTree_header").html("<h4>" + projectName + "<h4>");
    //alert("C:\\Users\\Mitch\\Documents\\PTPworkspace\\" + projectName);
    $("#fileTree_container").fileTree({root: workspaceBase + "/" + projectName,
                                       script: "resources/connectors/jqueryFileTree.jsp"},
                                       function(file) {
        //openFile(file);
        activeFile = file;
        openNewTab(file);
    });
    
    var xmlhttp = new XMLHttpRequest();
    var loc = window.location, new_uri;
    new_uri = loc.protocol;
    new_uri += "//" + loc.host;
    new_uri += "/PTPEditor";
    new_uri += "/webresources/project/setactive/";
    new_uri += projectName;
    xmlhttp.open("POST", new_uri, true);
    xmlhttp.send();
    
    openWebSocket(projectName);
    activeProject = projectName;
    $(".project_action").css( 'pointer-events', 'auto' );
    $(".project_action").removeClass("project_action_inactive");
}

//This function begins the process of closing every open tab in a project, one
//at a time.
function chainCloseAllTabs(logoutWhenDone) {
    if(tabList.length === 0) {
        listWorkspaceProjects();
        if(logoutWhenDone) {
            setTimeout(function(){
                leaveSite();
            }, 150);
        }
    } else {
        var closePromise = beginCloseProcess();
        if(closePromise === undefined) {
            chainCloseAllTabs(logoutWhenDone);
        } else {
            closePromise.done(function(data){
                chainCloseAllTabs(logoutWhenDone);
            });
            closePromise.fail(function(data) {
                return;
            });
        }
        
        
        //closePromise();
        
    }
}

//Upon being given a string containing the absolute path to a file, sends a
//request to the server prefixed by the load request header and containing
//the file's path.
function openFile(file) {
    webSocket.send("LOAD " + file);
    
    var fileExtension = file.substring(file.lastIndexOf('.') + 1, file.length);
    //controller5.report([{msg:"[DEBUG]: file type is: " + fileExtension, className:"jquery-console-message-value"}]);
        
    
    if(fileExtension === "c") {
        myCodeMirror.setOption("mode", "text/x-csrc");
    } else if (fileExtension === "cpp") {
        myCodeMirror.setOption("mode", "text/x-c++src");
    } else if (fileExtension === "java") {
        myCodeMirror.setOption("mode", "text/x-java");
    } else if (fileExtension === "f90"){
        myCodeMirror.setOption("mode", "text/x-Fortran");
    } else {
        myCodeMirror.setOption("mode", "text/plain");
    }
    
    //var lastSlashIndex = Math.max(file.lastIndexOf("/"), file.lastIndexOf("\\"));
    //var shortFile = file.substring(lastSlashIndex + 1, file.length);
    //document.getElementById("editortabs").innerHTML = shortFile;
    
}

//Closes the active websocket. This is supposed to be called
//when closing/deleting a project.
function closeWebSocket() {
    if(webSocket !== undefined) {
        webSocket.close();
    }
}

//Opens the websocket to the LoadServer class. This is to be called when 
//opening/creating a project.
function openWebSocket(projectName) {
    if (webSocket === undefined || webSocket.readyState === WebSocket.CLOSED) {
        // Create a new instance of the websocket
        var loc = window.location, new_uri;
        if (loc.protocol === "https:") {
            new_uri = "wss:";
        } else {
            new_uri = "ws:";
        }
        new_uri += "//" + loc.host;
        new_uri += "/PTPEditor";
        new_uri += "/load";
        new_uri += "/" + projectName;
        webSocket = new WebSocket(new_uri);
        webSocket.onopen = function(event){
                    // For reasons I can't determine, onopen gets called twice
                    // and the first time event.data is undefined.
                    // Leave a comment if you know the answer.
            if(event.data === undefined) {
                return;
            }
        };
 
        //Respond to a message from the server in various ways. If the response
        //begins with the LOAD response prefix, load the response message into 
        //the editor. If beginning with the SAVE response prefix, print the output
        //from the console. If the response prefix is not recognized, this
        //prints an error message to the console.
        webSocket.onmessage = function(event){
            var responseType = event.data.substring(0,1);
            var response = event.data.substring(1, event.data.length);
            if(responseType === "L") {
                myCodeMirror.setOption("value", response);
                activeTab.contents = response;
            } else if (responseType === "S" ) {
                controller5.report([{msg:"[SAVE]: " + response, className:"jquery-console-message-value"}]);
            } else if (responseType === "B") {
                controller5.report([{msg:"[BUILD]: " + response , className:"jquery-console-message-value"}]);
            } else if (responseType === "N") {
                controller5.report([{msg:"[FILE CREATION]: " + response, className:"jquery-console-message-value"}]);
            } else if (responseType === "D"){
                controller5.report([{msg:"[DELETION]: " + response, className:"jquery-console-message-value"}]);
                showProjectDirectory(activeProject);
            } else if (responseType === "P") {
                controller5.report([{msg:"[PROJECT]: " + response, className:"jquery-console-message-value"}]);
            } else if (responseType === "G") {
                controller5.report([{msg:"[SETTINGS]: " + response, className:"jquery-console-message-value"}]);
            } else if (responseType === "E"){
                controller5.report([{msg:"[ERROR]: " + response, className:"jquery-console-message-value"}]);
            } else if (responseType === "R"){
                var options = response.split('\u00BB');
                $("#TargetIP").val(options[0]);
                $("#TargetUsername").val(options[1]);
                $("#TargetPassword").val(options[2]);
                $("#TargetDirectory").val(options[3]);
            } else {
                controller5.report([{msg:"[ERROR]: Could not resolve server response!", className:"jquery-console-message-value"}]);
            }  
            //controller5.report([{msg:"[INFO]: " + event.data,className:"jquery-console-message-value"}]);
        };
        webSocket.onclose = function(event){};
    }
}

//Switches to a new active tab.
function setActiveTab(tab) {
    for(var i = 0; i < tabList.length; i++) {
        $("#" + tabList[i].id).removeClass("active_tab");
        $("#" + tabList[i].id).addClass("inactive_tab");
    }
    activeTab.contents = myCodeMirror.getValue();
    //var oldTab = getTabByFile(activeFile); 
    //oldTab.contents = myCodeMirror.getValue();
    myCodeMirror.setOption("mode", tab.getMode());
    myCodeMirror.setOption("value", tab.contents);
    $("#" + tab.id).removeClass("inactive_tab");
    $("#" + tab.id).addClass("active_tab");
    activeTab = tab;
    activeFile = tab.file;
}

//Opens a new tab. This function is called when opening a file previously not
//opened, or when creating a new file.
function openNewTab(file) {
    var unopened = true;
    var matchIndex = 0;
    for(var i = 0; i < tabList.length; i++) {
        if(tabList[i].file === file) {
            unopened = false;
            matchIndex = i;
        }
    }
    
    if(unopened) {
        openFile(file);
        if(tabList.length === 0) {
            numTabsOpened = 0;
            openEditor();
        } else {
            numTabsOpened++;
        }
    
        var newTab = new Tab(myCodeMirror.getValue(), file, ("editor_tab_" + numTabsOpened.toString()));
        $("#editortabs").append(newTab.genHTML());
        tabList.push(newTab);
        $("#" + newTab.id).on('click', function() {
            //alert("Registered!");
            //alert("#" + newTab.id + " " + tabList.length + " " + getTabById(newTab.id).contents);
            setActiveTab(getTabById(newTab.id));
        });
        if(tabList.length === 1) {
            activeTab = newTab;
        } else {
            setActiveTab(newTab);
        }
    } else {
        setActiveTab(tabList[matchIndex]);
    }
    
    activeFile = file;
    
    $(".close_button").off("click");
    $(".close_button").on("click", function(){
        var id = this.id;
        id = id.substring(0, id.length-1);
        setActiveTab(getTabById(id));
        setTimeout(function() {
            //alert("Begun!");
            beginCloseProcess();
        }, 200);
    });
}

//Given a particular ID, this function returns the tab that has this unique id.
function getTabById(id) {
    for(var i = 0; i < tabList.length; i++) {
        if(tabList[i].id === id) {
            return tabList[i];
        }
    }
    return undefined;
}

//Given an absolute path to a file, this function returns the tab that represents
//the file to display through the tile.
function getTabByFile(file) {
    for(var i = 0; i < tabList.length; i++) {
        if(tabList[i].file === file) {
            return tabList[i];
        }
    }
    return undefined;
}

//Begins the process of closing the active tab. What happens from this point on
//is solely event driven.
function beginCloseProcess() {
    //alert("closing!");
    if(activeTab.dirty === true) {
        deferred = $.Deferred();
        $(".verifyFileSave").slideFadeToggle();
        return deferred.promise();
    } else {
        closeActiveTab();
    }
}

//Binding events to the action buttons in the save confirmation popup.
$(function(){
    $("#closeCurrentTab").on("click", function(){
        beginCloseProcess();
    });
    
    $("#confirmFileSave").on("click", function() {
        doSave();
        setTimeout(closeActiveTab(), 3000);
        
        $(".verifyFileSave").slideFadeToggle();
        deferred.resolve("File saved.");
    });
    
    $("#dontDoFileSave").on("click", function() {
        closeActiveTab();
        $(".verifyFileSave").slideFadeToggle();
        deferred.resolve("File not saved.");
    });
    
    $("#cancelFileClose").on("click", function() {
        $(".verifyFileSave").slideFadeToggle();
        deferred.reject("Process canceled");
    });
});

//Binding events to the action buttons in the file deletion confirmation popup.
$(function(){
   
   bindPopup($("#deleteCurrentFile"), $(".confirmDeleteFile"));
   
   $("#confirmDelete").on("click", function(){
       controller5.report([{msg:"Preparing to delete: " + activeFile, className:"jquery-console-message-value"}]);
       var fileToDelete = activeFile;
       closeActiveTab();
       deleteFile(fileToDelete);
       deselect($("#deleteCurrentFile"), $(".confirmDeleteFile"));
   });
});

//This function will set the tab with the given ID to be active.
function bindTabById(ID) {
    $("#" + ID).on("click", function() {
        //alert("Registered!");
        setActiveTab(getTabById(ID));
    });
}

//Closes whatever tab is active. If attempting to close an unsaved file, the user
//will be asked whether or not they wish to save the file.
function closeActiveTab() {
    var tabIndex = 0;
    $("#editortabs").empty();
    for(var i = 0; i < tabList.length; i++) {
        if(tabList[i].id === activeTab.id) {
            tabIndex = i;
        } else {
            $("#editortabs").append(tabList[i].genHTML());
            bindTabById(tabList[i].id);
            //alert("Bound: " + tabList[i].id);
        }
    }
    //alert(tabIndex + " " + tabList.length);
    
    if(tabIndex === 0) {
        if(tabList.length === 1) {
            activeTab = undefined;
            myCodeMirror.setOption("mode", "text/plain");
            myCodeMirror.setOption("value", "");
            closeEditor();
        } else {
            setActiveTab(tabList[tabList.length-1]);
        }
    } else {
        setActiveTab(tabList[tabIndex - 1]);
    }
    tabList.splice(tabIndex, 1);
    //alert(tabIndex + " " + tabList.length);
    $(".close_button").on("click", function(){
        var id = this.id;
        id = id.substring(0, id.length-1);
        setActiveTab(getTabById(id));
        setTimeout(function() {
            //alert("Begun!");
            beginCloseProcess();
        }, 200);
    });
}

//Constructor for the Tab pseudo-class. This contains the following fields and functions.
// contents     :   The text content of the file.
// file         :   The absolute path to the file.
// dirty        :   A boolean field indicating whether the file has changed.
// active       :   A boolean field indicating whether the tab is the file currently being displayed.
// getName()    :   A function that returns the text highlighting mode that this file type corresponds to.
// genHtml()    :   A function that returns the HTML content corresponding to this tab.
function Tab (contents, file, id) {
    this.contents = contents;
    this.file = file;
    this.dirty = false;
    this.active = true;
    this.id = id;
    
    this.getName = function() {
        var lastDelimIndex = Math.max(file.lastIndexOf("/"), file.lastIndexOf("\\"));
        return file.substring(lastDelimIndex + 1, file.length);
    };
    
    this.getMode = function() {
        var extIndex = file.lastIndexOf(".");
        var ext = file.substring(extIndex + 1, file.length);
        
        if(ext === "java") {
            return "text/x-java";
        } else if (ext === "cpp") {
            return "text/x-c++src";
        } else if (ext === "c") {
            return "text/x-csrc";
        } else if (ext === "f90") {
            return "text/x-Fortran";
        } else {
            return "text/plain";
        }
    };
    
    this.genHTML = function() {
        var output = "<div class='editor_tab ";
        if(this.active) {
            output += "active_tab' ";
        } else {
            output += "inactive_tab' ";
        }
        output += "id='" + this.id + "' ";
        //output += "href ='#'>";
        output += "><a href='#'>";
        output += this.getName();
        output += "</a><img id='" + this.id +"D'";
        output += " class='close_button' src='resources/images/close.png'";
        output += "</div>";
        return output;
    };
}

//Marks whatever tab is active as dirty, meaning that it must be saved before being closed.
function markActiveDirty() {
    if(activeTab.dirty === false) {
        activeTab.dirty = true;
        $("#" + activeTab.id).addClass("dirty_tab");
    }
}

//This function will send a request to the server to delete a given file.
function deleteFile(fileName) {
    webSocket.send("DELETE " + fileName);
}

//This function will send a request to the server to delete a given project directory.
function deleteActiveProject() {
    webSocket.send("DELETE_PROJECT");
    setTimeout(function() {
        chainCloseAllTabs(false);
    }, 750);
    
}

//Binding the delete project confirmation popup and adding functionality to it.
$(function(){
   
   bindPopup($("#deleteCurrentProject"), $(".confirmDeleteProject"));
   
   $("#confirmDeleteProject").on("click", function(){
       controller5.report([{msg:"Preparing to delete: " + activeProject, className:"jquery-console-message-value"}]);
       //var fileToDelete = activeFile;
       //closeActiveTab();
       //deleteFile(fileToDelete);
       deleteActiveProject();
       deselect($("#deleteCurrentProject"), $(".confirmDeleteProject"));
   });
});


//Binding actions to the save and build buttons.
$(function(){
    $("#save_file_trigger").on("click", function() {
        doSave();
    });
    $("#build_file_trigger").on("click", function(){
        doBuild();
    });
});

//Binding the File, Build, and Options menu tab buttons to their respective
//menus.
$(function() {
    $("#open_file_menu").on("click", function() {
        $(".taskbar_menu").addClass("menu_inactive");
        $(".menu_tab").removeClass("menu_tab_active");
        $("#file_menu").removeClass("menu_inactive");
        $("#open_file_menu").addClass("menu_tab_active");
    });
    $("#open_build_menu").on("click", function() {
        $(".taskbar_menu").addClass("menu_inactive");
        $(".menu_tab").removeClass("menu_tab_active");
        $("#build_menu").removeClass("menu_inactive");
        $("#open_build_menu").addClass("menu_tab_active");
    });
    $("#open_options_menu").on("click", function() {
        $(".taskbar_menu").addClass("menu_inactive");
        $(".menu_tab").removeClass("menu_tab_active");
        $("#options_menu").removeClass("menu_inactive");
        $("#open_options_menu").addClass("menu_tab_active");
    });
});

//Displays options pertaining to the project, such as target IP.
function displayProjectOptions() {
    $("#project_options").removeClass("options_inactive");
    $("#editor_options").addClass("options_inactive");
    webSocket.send("READ_SETTINGS");
}

//Displays options pertaining to the editor, such as the theme used.
function displayEditorOptions() {
    $("#project_options").addClass("options_inactive");
    $("#editor_options").removeClass("options_inactive");
}

//Binding functions to the options tab.
$(function(){
    $("#project_options_trigger").on("click", function() {
        displayProjectOptions();
    });
    
    $("#editor_options_trigger").on("click", function() {
        displayEditorOptions();
    });
});

//This method is called when updating the editor options.
function updateEditorOptions() {
    var selectionBox = document.getElementById("editor_theme_box");
    var chosenTheme = selectionBox.options[selectionBox.selectedIndex].value;
    myCodeMirror.setOption("theme", chosenTheme);
    
    if(isNaN(Number($("#tabWidthOption").val()))) {
        $("#tabWidthOption").val("4");
    }
    
    myCodeMirror.setOption("tabSize", Number($("#tabWidthOption").val()));
    myCodeMirror.setOption("indentUnit", Number($("#tabWidthOption").val()));
    myCodeMirror.setOption("smartIndent", document.getElementById("smartIndentOption").checked);
    myCodeMirror.setOption("lineNumbers", document.getElementById("lineNumbersOption").checked);
}

//Binding the save editor options button the updateEditorOptions function.
$(function(){
    $("#save_editor_options").on("click", function(){
        updateEditorOptions();
    });
});

//This method is called when updating the project options.
function updateProjectOptions() {
    var ip = $("#TargetIP").val();
    if(ip === null || ip === "")
        ip = "NO_IP_ENTERED";
    var username = $("#TargetUsername").val();
    if(username === null || username === "")
        username = "NO_USERNAME_ENTERED";
    var password = $("#TargetPassword").val();
    if(password === null || password === "")
        password = "NO_PASSWORD_ENTERED";
    var directory = $("#TargetDirectory").val();
    if(directory === null || directory === "")
        directory = "workspace";
    var message = ip + '\u00BB' + username + '\u00BB' + password + '\u00BB' + directory;
    webSocket.send("SETTINGS " + message);
    //controller5.report([{msg: message, className: "jquery-console-message-value"}]);
}

//Binding the save project options button to the updateProjectOptions function.
$(function() {
    $("#save_project_options").on("click", function(){
        updateProjectOptions();
    });
});

//Enter the site.
function enterSite() {
    $(".greetingLogin").addClass("login_inactive");
    userName = $("#username").val();
}

$(function() {
    $("#username_confirm_button").on("click", function(){
        if($("#username").val() !== "" && $("#username").val().search(/\W+/g) === -1) {
            enterSite();
        } else {
            alert("Invalid Username!");
        }
    });
});

function leaveSite() {
    $(".greetingLogin").removeClass("login_inactive");
    userName = "";
}

$(function(){
    $(".exit_tab").on("click", function(){
        chainCloseAllTabs(true);
    });
});