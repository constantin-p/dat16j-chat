package client.core;

import client.core.section.connect.ConnectController;
import client.core.section.UISection;
import client.core.section.session.SessionController;
import client.core.section.session.SessionWorker;
import client.model.ClientSocketData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import ui.CPTab;
import ui.CPTabList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RootController {
    private List<SessionWorker> sessions = new ArrayList<>();

    @FXML
    private CPTabList tabList;
    private int index = 0;

    public RootController(Stage primaryStage) {
        primaryStage.setOnCloseRequest((windowEvent) -> {
            for (SessionWorker session: sessions) {
                session.disconnect();
            }
        });
    }

    @FXML
    private void initialize() {
        this.loadSection("CONNECT", "＋", new ConnectController(this));
    }

    public void addChatSession(ClientSocketData clientSocketData, String username) {
        int currentIndex = index;
        try {
            SessionWorker session = new SessionWorker(currentIndex, clientSocketData, username);
            this.sessions.add(session);

            String id = "SESSION-" + System.currentTimeMillis();
            this.loadSection(id, session.getSessionName(), new SessionController(session, () -> {
                this.sessions.remove(session);
                this.tabList.removeTab(id);
            }));
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource(controller.getTemplatePath()));

            loader.setController(controller);
            return loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
