package org.net.webcoreservice.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "DeviceGroup")
public class DeviceGroup {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String title;

    @OneToMany(mappedBy = "deviceGroup", cascade = CascadeType.ALL)
    private List<Device> device;

}
