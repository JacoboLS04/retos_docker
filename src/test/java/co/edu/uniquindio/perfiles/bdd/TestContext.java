package co.edu.uniquindio.perfiles.bdd;

import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

@Component
public class TestContext {
    private Long currentUserId;
    private ResultActions resultActions;

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public ResultActions getResultActions() {
        return resultActions;
    }

    public void setResultActions(ResultActions resultActions) {
        this.resultActions = resultActions;
    }

    public void reset() {
        this.currentUserId = null;
        this.resultActions = null;
    }
}
