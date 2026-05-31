package ro.iss.proiect.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivitySuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ex: "Mountain", "Sea"
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(length = 500)
    private String imageUrl;
}
