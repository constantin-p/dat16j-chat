package client.core.section.session;

import client.core.RootController;
import client.core.section.SectionBaseController;
import client.core.section.UISection;
import javafx.fxml.FXML;

public class SessionController extends SectionBaseController implements UISection {
    private static final String TEMPLATE_PATH = "templates/section/session.fxml";

    private RootController rootController;
    private SessionWorker sessionWorker;

    public SessionController(RootController rootController, SessionWorker sessionWorker) {
        this.rootController = rootController;
        this.sessionWorker = sessionWorker;
    }

    @FXML
    public void initialize() {
        super.initialize();

        this.sessionWorker.start();
    }

    public String getTemplatePath() {
        return TEMPLATE_PATH;
    }
}
