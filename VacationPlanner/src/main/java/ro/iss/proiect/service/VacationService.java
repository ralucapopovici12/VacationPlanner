package ro.iss.proiect.service;

import ro.iss.proiect.model.*;
import ro.iss.proiect.repository.*;

import java.util.List;
import java.util.Optional;

public class VacationService {
    private final VacationRepository vacationRepo = new VacationRepository();
    private final PackageRepository packageRepo = new PackageRepository();
    private final ActivitySuggestionRepository activityRepo = new ActivitySuggestionRepository();
    private final UserRepository userRepo = new UserRepository();

    public Vacation createVacation(String email, String type, String startDate, String endDate, Double budget, Integer numberOfPeople) {
        Optional<User> userOpt = userRepo.getUserByEmail(email);
        if (userOpt.isEmpty()) return null;

        Vacation vacation = Vacation.builder()
                .user(userOpt.get())
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .budget(budget)
                .numberOfPeople(numberOfPeople)
                .status("Draft")
                .build();
        
        return vacationRepo.save(vacation);
    }

    public List<Vacation> getUserVacations(String email) {
        Optional<User> userOpt = userRepo.getUserByEmail(email);
        return userOpt.map(user -> vacationRepo.findByUserId(user.getId())).orElse(null);
    }
    
    public List<VacationPackage> getPackagesByType(String type) {
        return packageRepo.findByType(type);
    }

    public boolean linkPackage(Long vacationId, Long packageId) {
        Optional<Vacation> vacOpt = vacationRepo.findById(vacationId);
        Optional<VacationPackage> pkgOpt = packageRepo.findById(packageId);

        if (vacOpt.isPresent() && pkgOpt.isPresent()) {
            Vacation vacation = vacOpt.get();
            VacationPackage pkg = pkgOpt.get();
            vacation.setSelectedPackage(pkg);
            vacation.setDestination(pkg.getDestination());
            vacation.setStatus("Reserved/Pending");
            vacationRepo.update(vacation);
            return true;
        }
        return false;
    }

    public List<ActivitySuggestion> getActivitySuggestions(String type) {
        return activityRepo.findByType(type);
    }

    public void addActivity(Long vacationId, String title, String desc, Double price, String imageUrl) {
        Optional<Vacation> vacOpt = vacationRepo.findById(vacationId);
        if (vacOpt.isPresent()) {
            VacationActivity activity = VacationActivity.builder()
                    .vacation(vacOpt.get())
                    .title(title)
                    .description(desc)
                    .price(price)
                    .imageUrl(imageUrl)
                    .build();
            vacationRepo.saveActivity(activity);
        }
    }
    
    public List<VacationActivity> getVacationActivities(Long vacationId) {
        return vacationRepo.findActivitiesByVacationId(vacationId);
    }

    public Optional<Vacation> getVacationById(Long id) {
        return vacationRepo.findById(id);
    }

    public void deleteVacation(Long id) {
        vacationRepo.delete(id);
    }
}
