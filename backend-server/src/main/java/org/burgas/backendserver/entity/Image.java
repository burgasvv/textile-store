package org.burgas.backendserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;

@Entity
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Image extends AbstractEntity implements Serializable {

    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "format")
    private String format;

    @Column(name = "size")
    private Long size;

    @JsonIgnore
    @Column(name = "data")
    private byte[] data;

    @Column(name = "preview")
    private Boolean preview;

    @JsonIgnore
    @OneToOne(mappedBy = "image", cascade = {PERSIST, MERGE, REFRESH, DETACH})
    private Identity identity;
}
