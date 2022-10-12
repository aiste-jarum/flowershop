package lt.aigen.geles.components;

import lombok.Getter;
import lombok.Setter;
import lt.aigen.geles.models.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter @Setter
public class CurrentUser {
    private User currentUser;
    public User get() { return getCurrentUser();}
}
