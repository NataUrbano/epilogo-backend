package com.epilogo.epilogo.audit;

import com.epilogo.epilogo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_logs")
@MappedSuperclass
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id", nullable = false)
    private Long auditId;

    @Column(name = "affected_table", nullable = false, length = 50)
    private String affectedTable;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    @Column(name = "modified_data")
    @JdbcTypeCode(SqlTypes.JSON)
    private String modifiedData;
}