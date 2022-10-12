package lt.aigen.geles.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "CONFIGURATION",uniqueConstraints =
@UniqueConstraint(name = "UNIQUE_CONFIG_KEYS", columnNames = {"key"}))
@Getter @Setter
public class Configuration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @NotNull
    private String key;
    @NotNull
    private String value;
}
