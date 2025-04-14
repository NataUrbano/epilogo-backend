package com.epilogo.epilogo.audit;


import com.epilogo.epilogo.model.User;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;

public class AuditUserListener {
    @PostPersist
    @PostUpdate
    public void postPersist(User user) {
        System.out.println(user.toString());
    }

    @PreRemove
    public void preRemove(User user) {
        System.out.println(user.toString());
    }


}
