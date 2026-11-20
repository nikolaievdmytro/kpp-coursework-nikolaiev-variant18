package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Порушення: /violations.
 * GET повертає список у форматі JSON; POST виконує дію з параметра action (add, update, transition, delete).
 * Servlet не містить SQL і предметних правил: він лише читає параметри й викликає Service.
 */
public class ViolationServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ViolationServlet.class.getName());

    private final ViolationService service = new ViolationService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = req.getParameter("action");
            if ("add".equals(action)) {
                service.add(longParam(req, "observationId"));
            } else if ("update".equals(action)) {
                service.update(longParam(req, "id"), longParam(req, "observationId"));
            } else if ("transition".equals(action)) {
                // Чи дозволений перехід, вирішує ViolationService, а не Servlet
                service.changeState(longParam(req, "id"), stateParam(req));
            } else if ("delete".equals(action)) {
                service.delete(longParam(req, "id"));
            } else {
                throw new IllegalArgumentException("Невідома операція");
            }
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    private ViolationState stateParam(HttpServletRequest req) {
        String value = req.getParameter("state");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Не задано стан порушення");
        }
        try {
            return ViolationState.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан порушення: " + value);
        }
    }

    private void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    /** Предметні помилки -> 400, непередбачені -> 500. У відповіді завжди JSON {"error": "..."}. */
    private void writeError(HttpServletResponse resp, Exception e) throws IOException {
        int status;
        String message;
        if (e instanceof IllegalArgumentException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            message = e.getMessage() == null ? "Некоректний запит" : e.getMessage();
        } else if (e instanceof SQLIntegrityConstraintViolationException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            message = "Операцію неможливо виконати: існують пов'язані записи";
        } else {
            LOG.log(Level.SEVERE, "Внутрішня помилка сервера", e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            message = "Внутрішня помилка сервера";
        }
        resp.setStatus(status);
        writeJson(resp, Map.of("error", message));
    }

    private long longParam(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        try {
            return Long.parseLong(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + name + " має бути цілим числом");
        }
    }
}
