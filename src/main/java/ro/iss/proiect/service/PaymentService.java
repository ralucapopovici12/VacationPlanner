package ro.iss.proiect.service;

import ro.iss.proiect.model.Vacation;
import ro.iss.proiect.model.VacationActivity;
import ro.iss.proiect.repository.VacationRepository;

import java.util.List;
import java.util.Optional;

public class PaymentService {
    private final VacationRepository vacationRepo = new VacationRepository();

    public Double calculateTotalCost(Long vacationId) {
        Optional<Vacation> vacOpt = vacationRepo.findById(vacationId);
        if (vacOpt.isEmpty()) return 0.0;
        
        Vacation vacation = vacOpt.get();
        double total = 0;
        if (vacation.getSelectedPackage() != null) {
            total += vacation.getSelectedPackage().getPrice();
        }
        
        List<VacationActivity> activities = vacationRepo.findActivitiesByVacationId(vacationId);
        for (VacationActivity act : activities) {
            total += act.getPrice();
        }
        
        // Let's say agency fee is 10%
        total = total + (total * 0.10);
        return total;
    }

    public String payVacation(Long vacationId, boolean force) {
        Optional<Vacation> vacOpt = vacationRepo.findById(vacationId);
        if (vacOpt.isEmpty()) return "Vacation not found";
        
        Vacation vacation = vacOpt.get();
        Double totalCost = calculateTotalCost(vacationId);
        
        if (totalCost > vacation.getBudget() && !force) {
            double difference = totalCost - vacation.getBudget();
            return String.format("2.E1 Budget Exceeded. Total cost exceeds budget by %.2f$. Please remove activities or change package.", difference);
        }
        
        // Simulam plata cu succes
        vacation.setStatus("Fully Paid");
        vacationRepo.update(vacation);
        return "SUCCESS";
    }
}
