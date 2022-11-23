package org.ttn.ecommerce.entities.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ttn.ecommerce.entities.Customer;
import org.ttn.ecommerce.entities.UserEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ActivateUserToken {

    @Id
    @SequenceGenerator(name="activate_sequence",sequenceName = "activate_sequence",initialValue = 1,allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "activate_sequence")
    private Long id;

    @Column(name="token")
    private String token;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="expire_At")
    private LocalDateTime expireAt;

    @Column(name="activated_at")
    private LocalDateTime activatedAt;

    @ManyToOne
    @JoinColumn(name="user_id")
    private UserEntity userEntity;

    public ActivateUserToken(Long id, String token, LocalDateTime createdAt, LocalDateTime expireAt, LocalDateTime activatedAt, UserEntity userEntity) {
        this.id = id;
        this.token = token;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.activatedAt = activatedAt;
        this.userEntity = userEntity;
    }
}
