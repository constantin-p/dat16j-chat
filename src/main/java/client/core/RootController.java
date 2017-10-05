package client.core;

import client.core.section.ConnectController;
import client.core.section.UISection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import ui.CPTab;
import ui.CPTabList;

import java.io.IOException;

public class RootController {

    @FXML
    private CPTabList tabList;

    public RootController(Stage primaryStage) { }

    @FXML
    private void initialize() {
        this.loadSection("CONNECT", "＋", new ConnectController(this));
    }

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
