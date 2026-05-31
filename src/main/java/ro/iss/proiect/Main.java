package ro.iss.proiect;

import io.javalin.Javalin;
import ro.iss.proiect.service.AuthService;
import ro.iss.proiect.service.DataSeeder;
import ro.iss.proiect.service.PaymentService;
import ro.iss.proiect.service.VacationService;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Run seeder
        new DataSeeder().seedData();

        AuthService authService = new AuthService();
        VacationService vacationService = new VacationService();
        PaymentService paymentService = new PaymentService();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(8080);

        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).result(e.toString() + " | Cause: " + (e.getCause() != null ? e.getCause().toString() : "none"));
        });

        app.get("/", ctx -> ctx.result("Vacation Planner API is running"));

        // Auth
        app.post("/api/auth/login", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String email = String.valueOf(body.get("email"));
            String password = String.valueOf(body.get("password"));

            if (authService.authenticate(email, password)) {
                ctx.json(Map.of("message", "Autentificare reusita!", "success", true));
            } else {
                ctx.status(401).json(Map.of("message", "Email sau parola incorecta!", "success", false));
            }
        });

        app.post("/api/auth/register", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String name = String.valueOf(body.get("name"));
            String email = String.valueOf(body.get("email"));
            String password = String.valueOf(body.get("password"));

            if (authService.registerUser(name, email, password)) {
                ctx.json(Map.of("message", "Cont creat cu succes!", "success", true));
            } else {
                ctx.status(400).json(Map.of("message", "Email-ul este deja folosit!", "success", false));
            }
        });

        // Vacations
        app.post("/api/vacations", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Integer persons = null;
            if (body.get("numberOfPeople") != null && !String.valueOf(body.get("numberOfPeople")).isBlank()) {
                persons = Integer.parseInt(String.valueOf(body.get("numberOfPeople")));
            }
            var vac = vacationService.createVacation(
                    String.valueOf(body.get("email")),
                    String.valueOf(body.get("type")),
                    String.valueOf(body.get("startDate")),
                    String.valueOf(body.get("endDate")),
                    Double.parseDouble(String.valueOf(body.get("budget"))),
                    persons
            );
            if (vac != null) ctx.json(Map.of("success", true, "vacation", vac));
            else ctx.status(400).json(Map.of("success", false));
        });

        app.delete("/api/vacations/{id}", ctx -> {
            vacationService.deleteVacation(Long.parseLong(ctx.pathParam("id")));
            ctx.json(Map.of("success", true));
        });

        app.get("/api/vacations/user/{email}", ctx -> {
            ctx.json(vacationService.getUserVacations(ctx.pathParam("email")));
        });

        app.get("/api/vacations/{id}", ctx -> {
            var vac = vacationService.getVacationById(Long.parseLong(ctx.pathParam("id")));
            if(vac.isPresent()) ctx.json(vac.get());
            else ctx.status(404);
        });

        // Packages
        app.get("/api/packages", ctx -> {
            String type = ctx.queryParam("type");
            ctx.json(vacationService.getPackagesByType(type));
        });

        app.post("/api/vacations/{id}/package", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            boolean ok = vacationService.linkPackage(Long.parseLong(ctx.pathParam("id")), Long.parseLong(String.valueOf(body.get("packageId"))));
            ctx.json(Map.of("success", ok));
        });

        // Activities
        app.get("/api/activities/suggestions", ctx -> {
            String type = ctx.queryParam("type");
            ctx.json(vacationService.getActivitySuggestions(type));
        });

        app.get("/api/vacations/{id}/activities", ctx -> {
            ctx.json(vacationService.getVacationActivities(Long.parseLong(ctx.pathParam("id"))));
        });

        app.post("/api/vacations/{id}/activities", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String desc = body.get("description") != null ? String.valueOf(body.get("description")) : "";
            String imageUrl = body.get("imageUrl") != null ? String.valueOf(body.get("imageUrl")) : "";
            vacationService.addActivity(
                    Long.parseLong(ctx.pathParam("id")),
                    String.valueOf(body.get("title")),
                    desc,
                    Double.parseDouble(String.valueOf(body.get("price"))),
                    imageUrl
            );
            ctx.json(Map.of("success", true));
        });

        // Payment
        app.get("/api/vacations/{id}/summary", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            var vac = vacationService.getVacationById(id).orElse(null);
            if(vac != null) {
                ctx.json(Map.of(
                    "vacation", vac,
                    "activities", vacationService.getVacationActivities(id),
                    "totalCost", paymentService.calculateTotalCost(id)
                ));
            } else {
                ctx.status(404);
            }
        });

        app.post("/api/vacations/{id}/pay", ctx -> {
            boolean force = "true".equals(ctx.queryParam("force"));
            String result = paymentService.payVacation(Long.parseLong(ctx.pathParam("id")), force);
            if (result.equals("SUCCESS")) {
                ctx.json(Map.of("success", true));
            } else {
                ctx.status(400).json(Map.of("success", false, "message", result));
            }
        });
    }
}