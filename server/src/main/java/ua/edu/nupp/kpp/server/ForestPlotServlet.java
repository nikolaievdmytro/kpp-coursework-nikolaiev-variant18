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
 * Лісові ділянки: /plots.
 * GET повертає список у форматі JSON; POST виконує дію з параметра action (add, update, delete).
 * Servlet не містить SQL і предметних правил: він лише читає параметри й викликає Service.
 */
public class ForestPlotServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ForestPlotServlet.class.getName());

    private final ForestPlotService service = new ForestPlotService();
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
                service.add(readPlot(req, null));
            } else if ("update".equals(action)) {
                service.update(readPlot(req, longParam(req, "id")));
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

    private ForestPlot readPlot(HttpServletRequest req, Long id) {
        return new ForestPlot(id,
                req.getParameter("code"),
                req.getParameter("name"),
                req.getParameter("forestry"),
                doubleParam(req, "areaHa"),
                doubleParam(req, "sensitivityScore"));
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

    private double doubleParam(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        double result;
        try {
            result = Double.parseDouble(value == null ? "" : value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + name + " має бути числом");
        }
        if (Double.isInfinite(result)) {
            throw new IllegalArgumentException("Параметр " + name + " має бути скінченним числом");
        }
        return result;
    }
}
