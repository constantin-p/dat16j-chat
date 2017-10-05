package client.core;

import client.core.section.connect.ConnectController;
import client.core.section.UISection;
import client.core.section.session.SessionController;
import client.core.section.session.SessionWorker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import ui.CPTab;
import ui.CPTabList;

import java.io.IOException;
import java.net.Socket;

public class RootController {

    @FXML
    private CPTabList tabList;
    private int index = 0;

    public RootController(Stage primaryStage) { }

    @FXML
    private void initialize() {
        this.loadSection("CONNECT", "＋", new ConnectController(this));
    }

    public void addChatSession(Socket socket, String username) {
        int currentIndex = index;
        try {
            SessionWorker session = new SessionWorker(socket, username, currentIndex);
            this.loadSection("SESSION-" + System.currentTimeMillis(), session.getSessionName(), new SessionController(this, session));
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            index++;
        }
    }

    // Helpers
    private void loadSection(String id, String displayName, UISection controller) {
        // Load the template
        Node layout = loadSectionContent(controller);

        CPTab sectionTab = new CPTab(id, displayName);
        this.tabList.addTab(sectionTab, layout);
    }

    private Node loadSectionContent(UISection controller) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            FXMLLoader loader = new FXMLLoader(classLoader
                    .getResource(controller.getTemplatePath()));

            loader.setController(controller);
            Parent layout = loader.load();

            return layout;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
