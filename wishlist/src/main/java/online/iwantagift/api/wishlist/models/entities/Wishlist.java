package online.iwantagift.api.wishlist.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wishlist")
@Entity
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "createdAt")
    private Instant createdAt;

    @Column(name = "owner_id")
    private UUID ownerId;

    @OneToMany(mappedBy = "wishlist")
    private List<Wish> wishes = Collections.emptyList();
}
