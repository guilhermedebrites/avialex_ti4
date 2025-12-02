package com.avialex.api.model.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

import com.avialex.api.model.StringListConverter;
import jakarta.persistence.Convert;

import com.avialex.api.model.enums.ProcessStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Process {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User clientId;

    @Column(nullable = false)
	private String name;

    @Column(nullable = false)
    @Convert(converter = StringListConverter.class)
    private List<String> involvedParties;

    @Column(nullable = false)
    private Integer processNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessStatus status;

    @Column
    private LocalDateTime creationDate;

    @Column
    private LocalDateTime lastModifiedDate;

    @Column(precision = 19, scale = 2)
    private BigDecimal recoveredValue;

    @Column
    private Boolean won;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
    }

}