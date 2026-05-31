package ro.iss.proiect.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vacation_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacationPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ex: "Sea", "Mountain", "Business", "Romantic"
    @Column(nullable = false)
    private String type;

    // ex: "Flight+Hotel", "Train+Hotel", "Cabin only"
    @Column(nullable = false)
    private String bundleName;

    // ex: "Alps", "Nice", "Vienna"
    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private Double price;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String imageUrl;
}
