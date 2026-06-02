package online.iwantagift.api.profile.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "nickname", nullable = false, length = 64)
    private String nickname;

    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "avatar_id")
    private Avatar avatar;
}
