package lt.aigen.geles.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Entity
@Getter @Setter
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String username;

    @CreationTimestamp
    private Date executionDate;

    @Column(name="request_url")
    private String requestURL;

    private String httpMethodName;

    private String methodName;

    @Column(columnDefinition = "boolean default false")
    private boolean isAdmin;

    public RequestLog() {

    }

    public RequestLog(String username, String requestURL, String httpMethodName, String methodName, boolean isAdmin) {
        this.username = username;
        this.requestURL = requestURL;
        this.httpMethodName = httpMethodName;
        this.methodName = methodName;
        this.isAdmin = isAdmin;
    }
}
